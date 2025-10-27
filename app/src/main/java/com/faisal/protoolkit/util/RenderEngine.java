package com.faisal.protoolkit.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;

import com.faisal.protoolkit.model.EditOps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RenderEngine {
    private static final String TAG = "RenderEngine";
    private final FileManager fileManager;
    private final ExecutorService executor;
    
    public RenderEngine(Context context) {
        this.fileManager = new FileManager(context);
        this.executor = Executors.newFixedThreadPool(4); // Adjust based on device cores
    }
    
    /**
     * Renders a preview bitmap for a page at target width
     */
    public void renderPreview(@NonNull String documentId, int pageIndex, int targetWidthPx, 
                             @NonNull RenderCallback callback) {
        executor.execute(() -> {
            try {
                Bitmap bitmap = renderPreviewInternal(documentId, pageIndex, targetWidthPx);
                callback.onRenderComplete(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error rendering preview for doc " + documentId + " page " + pageIndex, e);
                callback.onRenderError(e);
            }
        });
    }
    
    /**
     * Renders the final output for export
     */
    public void renderFinal(@NonNull String documentId, int pageIndex, int quality0to100, 
                           @NonNull RenderCallback callback) {
        executor.execute(() -> {
            try {
                Bitmap bitmap = renderFinalInternal(documentId, pageIndex, quality0to100);
                callback.onRenderComplete(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error rendering final for doc " + documentId + " page " + pageIndex, e);
                callback.onRenderError(e);
            }
        });
    }
    
    /**
     * Renders and saves the final output to the render file
     */
    public void renderAndSave(@NonNull String documentId, int pageIndex, int quality0to100) {
        executor.execute(() -> {
            try {
                Bitmap bitmap = renderFinalInternal(documentId, pageIndex, quality0to100);
                if (bitmap != null) {
                    File renderFile = fileManager.getRenderedImageFile(documentId, pageIndex);
                    saveBitmapToFile(bitmap, renderFile, quality0to100);
                    bitmap.recycle();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error rendering and saving for doc " + documentId + " page " + pageIndex, e);
            }
        });
    }
    
    private Bitmap renderPreviewInternal(@NonNull String documentId, int pageIndex, int targetWidthPx) throws IOException {
        File originalFile = fileManager.getOriginalImageFile(documentId, pageIndex);
        if (!originalFile.exists()) {
            throw new IOException("Original file does not exist: " + originalFile.getAbsolutePath());
        }
        
        // Load bitmap with appropriate sample size to avoid OOM
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, targetWidthPx, 0);
        options.inJustDecodeBounds = false;
        
        Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);
        if (bitmap == null) {
            throw new IOException("Failed to decode bitmap: " + originalFile.getAbsolutePath());
        }
        
        // Load edit operations
        EditOps editOps = loadEditOps(documentId, pageIndex);
        
        // Apply edits
        bitmap = applyEditOps(bitmap, editOps, false); // Preview quality is lower
        
        return bitmap;
    }
    
    private Bitmap renderFinalInternal(@NonNull String documentId, int pageIndex, int quality0to100) throws IOException {
        File originalFile = fileManager.getOriginalImageFile(documentId, pageIndex);
        if (!originalFile.exists()) {
            throw new IOException("Original file does not exist: " + originalFile.getAbsolutePath());
        }
        
        Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
        if (bitmap == null) {
            throw new IOException("Failed to decode bitmap: " + originalFile.getAbsolutePath());
        }
        
        // Load edit operations
        EditOps editOps = loadEditOps(documentId, pageIndex);
        
        // Apply edits
        bitmap = applyEditOps(bitmap, editOps, true); // Final render with full quality
        
        return bitmap;
    }
    
    private EditOps loadEditOps(@NonNull String documentId, int pageIndex) {
        File editFile = fileManager.getEditOpsFile(documentId, pageIndex);
        if (!editFile.exists()) {
            return new EditOps(); // Return default if no edit file exists
        }
        
        try {
            String editJson = readTextFile(editFile);
            return EditOpsUtil.deserialize(editJson);
        } catch (Exception e) {
            Log.e(TAG, "Error loading edit ops for doc " + documentId + " page " + pageIndex, e);
            return new EditOps(); // Return default on error
        }
    }

    public Bitmap applyFilters(Bitmap originalBitmap, EditOps editOps) {
        Log.d("RenderEngine", "applyFilters called");
        if (originalBitmap == null) {
            Log.d("RenderEngine", "  originalBitmap is null");
            return null;
        }

        if (originalBitmap.isRecycled()) {
            Log.d("RenderEngine", "  originalBitmap is recycled");
            return null;
        }

        Log.d("RenderEngine", "  originalBitmap size: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

        if (editOps == null) {
            Log.d("RenderEngine", "  editOps is null, returning copy of original");
            return originalBitmap.copy(originalBitmap.getConfig(), true);
        }

        // Clone the bitmap to avoid modifying the original
        Bitmap bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        Log.d("RenderEngine", "  Created bitmap copy: " + bitmap.getWidth() + "x" + bitmap.getHeight());

        Bitmap result = bitmap; // Keep original reference to recycle later

        try {
            // Apply rotation
            if (editOps.rotate != 0) {
                Log.d("RenderEngine", "  Applying rotation: " + editOps.rotate + " degrees");
                result = rotateBitmap(result, editOps.rotate);
                Log.d("RenderEngine", "  After rotation: " + result.getWidth() + "x" + result.getHeight());
            }

            // Apply filter operations
            Log.d("RenderEngine", "  Applying filters");
            if (editOps.filter != null) {
                Log.d("RenderEngine", "    Filter mode: " + editOps.filter.mode);
                Log.d("RenderEngine", "    Contrast: " + editOps.filter.contrast);
                Log.d("RenderEngine", "    Brightness: " + editOps.filter.brightness);
                Log.d("RenderEngine", "    Sharpen: " + editOps.filter.sharpen);
            }
            result = ImageFilters.applyFilter(result, editOps);
            Log.d("RenderEngine", "  After applying filters: " + result.getWidth() + "x" + result.getHeight());

            // Apply crop operations if available
            if (editOps.hasCrop()) {
                Log.d("RenderEngine", "  Applying crop");
                result = cropBitmap(result, editOps);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Return original bitmap if processing fails
            // But first check if the original bitmap is still valid
            if (result != bitmap) {
                // If we created a new bitmap in processing, recycle it
                try {
                    if (result != null && !result.isRecycled()) {
                        result.recycle();
                    }
                } catch (Exception recycleException) {
                    // Ignore recycling exceptions
                }
            }

            // Try to return a copy of the original bitmap if it's still valid
            if (originalBitmap != null && !originalBitmap.isRecycled()) {
                try {
                    Log.d("RenderEngine", "  Returning copy of original bitmap due to exception");
                    return originalBitmap.copy(originalBitmap.getConfig(), true);
                } catch (Exception copyException) {
                    // If copying fails, return null
                    Log.d("RenderEngine", "  Failed to copy original bitmap: " + copyException.getMessage());
                    return null;
                }
            } else {
                // Original bitmap is invalid, return null
                Log.d("RenderEngine", "  Original bitmap is invalid, returning null");
                return null;
            }
        }

        // If the result is not the original bitmap we copied, recycle the original copy
        if (result != bitmap) {
            try {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Exception e) {
                // Ignore recycling exceptions
            }
        }

        Log.d("RenderEngine", "  Returning result bitmap: " + result.getWidth() + "x" + result.getHeight());
        return result;
    }
    
    private Bitmap applyEditOps(Bitmap bitmap, EditOps editOps, boolean isFinalRender) {
        if (editOps == null) {
            return bitmap;
        }
        
        Bitmap result = bitmap;
        
        // Apply rotation
        if (editOps.rotate != 0) {
            result = rotateBitmap(result, editOps.rotate);
        }
        
        // Apply filter operations
        result = ImageFilters.applyFilter(result, editOps);
        
        // TODO: Implement warp operation (requires OpenCV or custom implementation)
        // For now, we'll skip warp in basic implementation
        
        // TODO: Implement crop operation
        if (editOps.hasCrop()) {
            result = cropBitmap(result, editOps);
        }
        
        // TODO: Implement deskew operation
        // For now, we'll skip deskew in basic implementation
        
        // TODO: Implement denoise operation
        // For now, we'll skip denoise in basic implementation
        
        return result;
    }
    
    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0) return bitmap;
        
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        
        try {
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return rotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            // Return original bitmap if rotation fails due to memory issues
            return bitmap;
        }
        // Note: We don't recycle the original bitmap here because the caller is responsible for it
    }
    
    private Bitmap cropBitmap(Bitmap bitmap, EditOps editOps) {
        // Basic crop implementation - in a real app, you'd want to implement this properly
        // based on the crop coordinates in editOps.crop
        
        // For now, if we have crop points, we'll just return the bitmap as is
        // TODO: Implement proper cropping based on editOps.crop coordinates
        return bitmap;
    }
    
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    
    private void saveBitmapToFile(Bitmap bitmap, File file, int quality) throws IOException {
        // Ensure parent directory exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!fileManager.ensureDirExists(parentDir)) {
                throw new IOException("Failed to create parent directory: " + parentDir.getAbsolutePath());
            }
        }
        
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }
    }
    
    private String readTextFile(File file) throws IOException {
        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        java.io.InputStreamReader isr = new java.io.InputStreamReader(fis);
        java.io.BufferedReader reader = new java.io.BufferedReader(isr);
        
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        
        reader.close();
        isr.close();
        fis.close();
        
        return sb.toString().trim();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    public interface RenderCallback {
        void onRenderComplete(Bitmap bitmap);
        void onRenderError(Exception error);
    }
}