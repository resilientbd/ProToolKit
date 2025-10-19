package com.faisal.protoolkit.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageFilters {
    
    /**
     * Applies contrast and brightness adjustments to a bitmap
     */
    public static Bitmap applyContrastBrightness(Bitmap source, float contrast, float brightness) {
        if (source == null || source.isRecycled()) {
            return source;
        }
        
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        
        ColorMatrix colorMatrix = new ColorMatrix();
        
        // Apply contrast - scale RGB values
        float scale = contrast;
        float translate = brightness * 255.0f;
        
        // Apply contrast using setScale
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
        if (source == null || source.isRecycled()) {
            return source;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        
        // Calculate average for thresholding
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // Calculate luminance
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            
            if (gray > threshold * 255) {
                pixels[i] = 0xFFFFFFFF; // White
            } else {
                pixels[i] = 0xFF000000; // Black
            }
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height);
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
        if (editOps == null || editOps.filter == null) {
            return source;
        }
        
        Bitmap result = source;
        
        // Apply the selected filter mode
        switch (editOps.filter.mode) {
            case "GRAY":
                result = toGrayscale(result);
                break;
            case "BW":
                result = toBlackAndWhite(result, 0.5f); // 50% threshold
                break;
            case "COLOR_BOOST":
                result = applyColorBoost(result);
                break;
            case "ORIGINAL":
            default:
                // Do nothing, keep original
                break;
        }
        
        // Apply contrast and brightness adjustments
        if (editOps.filter.contrast != 1.0f || editOps.filter.brightness != 0.0f) {
            result = applyContrastBrightness(result, editOps.filter.contrast, editOps.filter.brightness);
        }
        
        // Apply sharpening
        if (editOps.filter.sharpen > 0.0f) {
            result = applySharpen(result, editOps.filter.sharpen);
        }
        
        return result;
    }
}