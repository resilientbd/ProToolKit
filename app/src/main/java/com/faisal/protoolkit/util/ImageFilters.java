package com.faisal.protoolkit.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

public class ImageFilters {
    
    /**
     * Applies contrast and brightness adjustments to a bitmap
     */
    public static Bitmap applyContrastBrightness(Bitmap source, float contrast, float brightness) {
        if (source == null || source.isRecycled()) {
            return source;
        }
        
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        
        // Create color matrix for contrast and brightness
        ColorMatrix colorMatrix = new ColorMatrix();
        
        // Apply contrast and brightness in one operation
        // This applies contrast scaling and brightness translation properly
        float brightnessTranslation = brightness * 255.0f;
        
        colorMatrix.set(new float[] {
                contrast, 0, 0, 0, brightnessTranslation,    // Red: contrast scaling + brightness
                0, contrast, 0, 0, brightnessTranslation,    // Green: contrast scaling + brightness
                0, 0, contrast, 0, brightnessTranslation,     // Blue: contrast scaling + brightness
                0, 0, 0, 1, 0                                  // Alpha: no change
        });
        
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0, 0, paint);
        
        return result;
    }
    
    /**
     * Converts bitmap to grayscale
     */
    public static Bitmap toGrayscale(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return source;
        }
        
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f); // Remove saturation to get grayscale
        
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0, 0, paint);
        
        return result;
    }
    
    /**
     * Converts bitmap to black and white using a simple threshold
     */
    public static Bitmap toBlackAndWhite(Bitmap source, float threshold) {
        Log.d("ImageFilters", "toBlackAndWhite called with threshold: " + threshold);
        if (source == null || source.isRecycled()) {
            Log.d("ImageFilters", "  source is null or recycled, returning source");
            return source;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        Log.d("ImageFilters", "  Processing bitmap: " + width + "x" + height + " config: " + source.getConfig());
        
        // Create a result bitmap with a known good configuration
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        
        int whiteCount = 0, blackCount = 0;
        
        int thresholdValue = (int)(threshold * 255);
        Log.d("ImageFilters", "  Using threshold value: " + thresholdValue + " (threshold param: " + threshold + ")");
        
        int minGray = 255, maxGray = 0; // Track min/max grays for debugging
        
        // Calculate average for thresholding
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // Calculate luminance
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            
            // Track min/max for debugging
            if (gray < minGray) minGray = gray;
            if (gray > maxGray) maxGray = gray;
            
            if (gray > thresholdValue) {
                pixels[i] = 0xFFFFFFFF; // White with full alpha
                whiteCount++;
            } else {
                pixels[i] = 0xFF000000; // Black with full alpha
                blackCount++;
            }
        }
        
        Log.d("ImageFilters", "  Gray value range: " + minGray + " - " + maxGray);
        
        Log.d("ImageFilters", "  Black pixels: " + blackCount + ", White pixels: " + whiteCount + ", Total: " + pixels.length);
        Log.d("ImageFilters", "  Black/White ratio: " + (blackCount * 100.0 / pixels.length) + "% are black");
        
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        Log.d("ImageFilters", "  toBlackAndWhite completed");
        return result;
    }
    
    /**
     * Applies basic sharpening filter
     */
    public static Bitmap applySharpen(Bitmap source, float amount) {
        // Basic sharpening using a convolution matrix
        // This is a simplified approach - for better results, OpenCV would be needed
        
        if (source == null || source.isRecycled()) {
            return source;
        }
        
        if (amount <= 0) return source.copy(source.getConfig(), true); // No sharpening needed, return copy
        
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        Canvas canvas = new Canvas(result);
        
        // Apply a simple sharpening effect using contrast
        float scale = 1.0f + (0.1f * amount); // Increase contrast for sharpening effect
        float translate = -128.0f * (scale - 1.0f); // Adjust brightness
        
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(scale, scale, scale, 1.0f);
        
        // Apply brightness using ColorMatrix multiplication with a separate matrix
        ColorMatrix brightnessMatrix = new ColorMatrix();
        brightnessMatrix.set(new float[] {
                1, 0, 0, 0, translate,  // Red
                0, 1, 0, 0, translate,  // Green
                0, 0, 1, 0, translate,  // Blue
                0, 0, 0, 1, 0           // Alpha
        });
        
        colorMatrix.postConcat(brightnessMatrix);
        
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        
        canvas.drawBitmap(source, 0, 0, paint);
        
        return result;
    }
    
    /**
     * Color boost filter - enhances colors
     */
    public static Bitmap applyColorBoost(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return source;
        }
        
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        
        ColorMatrix colorMatrix = new ColorMatrix();
        
        // Enhance saturation
        colorMatrix.setSaturation(1.5f);
        
        // Slightly increase contrast
        colorMatrix.postConcat(new ColorMatrix(new float[]{
            1.1f, 0, 0, 0, 0,      // Red component
            0, 1.1f, 0, 0, 0,      // Green component
            0, 0, 1.1f, 0, 0,      // Blue component
            0, 0, 0, 1, 0           // Alpha component
        }));
        
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0, 0, paint);
        
        return result;
    }
    
    /**
     * Applies all filter operations based on EditOps
     */
    public static Bitmap applyFilter(Bitmap source, com.faisal.protoolkit.model.EditOps editOps) {
        Log.d("ImageFilters", "applyFilter called");
        if (editOps == null || editOps.filter == null) {
            Log.d("ImageFilters", "  editOps or editOps.filter is null, returning source");
            return source;
        }
        
        Log.d("ImageFilters", "  Filter mode: " + editOps.filter.mode);
        Log.d("ImageFilters", "  Contrast: " + editOps.filter.contrast);
        Log.d("ImageFilters", "  Brightness: " + editOps.filter.brightness);
        Log.d("ImageFilters", "  Sharpen: " + editOps.filter.sharpen);
        
        Bitmap result = source;
        
        // Apply the selected filter mode
        switch (editOps.filter.mode) {
            case "GRAY":
                Log.d("ImageFilters", "  Applying GRAY filter");
                result = toGrayscale(result);
                break;
            case "BW":
                Log.d("ImageFilters", "  Applying BW filter with threshold 0.5f");
                result = toBlackAndWhite(result, 0.5f); // 50% threshold
                break;
            case "COLOR_BOOST":
                Log.d("ImageFilters", "  Applying COLOR_BOOST filter");
                result = applyColorBoost(result);
                break;
            case "ORIGINAL":
            default:
                Log.d("ImageFilters", "  Keeping ORIGINAL (no filter)");
                // Do nothing, keep original
                break;
        }
        
        // Apply contrast and brightness adjustments
        if (editOps.filter.contrast != 1.0f || editOps.filter.brightness != 0.0f) {
            Log.d("ImageFilters", "  Applying contrast/brightness: " + editOps.filter.contrast + ", " + editOps.filter.brightness);
            result = applyContrastBrightness(result, editOps.filter.contrast, editOps.filter.brightness);
        }
        
        // Apply sharpening
        if (editOps.filter.sharpen > 0.0f) {
            Log.d("ImageFilters", "  Applying sharpen: " + editOps.filter.sharpen);
            result = applySharpen(result, editOps.filter.sharpen);
        }
        
        Log.d("ImageFilters", "  Returning result: " + result.getWidth() + "x" + result.getHeight());
        return result;
    }
}