package com.faisal.protoolkit.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import com.faisal.protoolkit.BuildConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfExportUtil {
    private static final String TAG = "PdfExportUtil";
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static File createPdfFromBitmapsWithA4Size(List<Bitmap> bitmaps, String fileName, Context context) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            return null;
        }
        
        // A4 page size in pixels at 72 DPI (standard PDF resolution)
        int pageWidth = 595;  // A4 width in points
        int pageHeight = 842; // A4 height in points
        
        PdfDocument pdfDocument = new PdfDocument();
        
        try {
            for (int i = 0; i < bitmaps.size(); i++) {
                Bitmap bitmap = bitmaps.get(i);
                
                // Create a page description
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
                
                // Start a page
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                
                try {
                    Canvas canvas = page.getCanvas();
                    android.graphics.Paint paint = new android.graphics.Paint();
                    
                    // Convert hardware bitmap to software bitmap if needed
                    Bitmap softwareBitmap = bitmap;
                    if (bitmap.getConfig() == null) {
                        // If bitmap config is null, create a new ARGB_8888 bitmap
                        softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    } else if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                        // Create a new bitmap with ARGB_8888 config for compatibility
                        softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    
                    // Calculate scaling to fit bitmap to page while maintaining aspect ratio
                    float scaleWidth = ((float) pageWidth) / softwareBitmap.getWidth();
                    float scaleHeight = ((float) pageHeight) / softwareBitmap.getHeight();
                    float scale = Math.min(scaleWidth, scaleHeight);
                    
                    // Calculate position to center the bitmap on the page
                    float scaledWidth = softwareBitmap.getWidth() * scale;
                    float scaledHeight = softwareBitmap.getHeight() * scale;
                    float x = (pageWidth - scaledWidth) / 2;
                    float y = (pageHeight - scaledHeight) / 2;
                    
                    // Draw the bitmap onto the canvas
                    canvas.drawBitmap(softwareBitmap, null, 
                        new android.graphics.RectF(x, y, x + scaledWidth, y + scaledHeight), paint);
                } finally {
                    // Make sure to finish the page even if an exception occurs during drawing
                    pdfDocument.finishPage(page);
                }
            }
            
            // Create the output file
            File documentsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ScannedDocs");
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }
            
            File pdfFile = new File(documentsDir, fileName);
            
            // Write the document content
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            fos.close();
            
            return pdfFile;
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        } finally {
            pdfDocument.close();
        }
    }
    
    public static Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
        );
    }
    
    public static boolean deleteFile(File file) {
        return file != null && file.exists() && file.delete();
    }
}