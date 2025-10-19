package com.faisal.protoolkit.ui.tools.document;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for creating PDF files from bitmaps
 */
public class PdfUtils {
    
    /**
     * Creates a PDF file from a list of bitmaps with A4 page size
     * @param bitmaps List of bitmaps to include in the PDF
     * @param fileName Name of the output PDF file
     * @param context Application context
     * @return File object of the created PDF or null if failed
     */
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
                
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                
                // Calculate scaling to fit bitmap to page while maintaining aspect ratio
                float scaleWidth = ((float) pageWidth) / bitmap.getWidth();
                float scaleHeight = ((float) pageHeight) / bitmap.getHeight();
                float scale = Math.min(scaleWidth, scaleHeight);
                
                // Calculate position to center the bitmap on the page
                float scaledWidth = bitmap.getWidth() * scale;
                float scaledHeight = bitmap.getHeight() * scale;
                float x = (pageWidth - scaledWidth) / 2;
                float y = (pageHeight - scaledHeight) / 2;
                
                // Draw the bitmap onto the canvas
                canvas.drawBitmap(bitmap, null, 
                    new android.graphics.RectF(x, y, x + scaledWidth, y + scaledHeight), paint);
                
                // Finish the page
                pdfDocument.finishPage(page);
            }
            
            // Create the output file
            File documentsDir = new File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), "ScannedDocs");
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }
            
            File pdfFile = new File(documentsDir, fileName);
            
            // Write the document content
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
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
    
    /**
     * Creates a PDF file from a list of bitmaps with letter page size
     * @param bitmaps List of bitmaps to include in the PDF
     * @param fileName Name of the output PDF file
     * @param context Application context
     * @return File object of the created PDF or null if failed
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static File createPdfFromBitmapsWithLetterSize(List<Bitmap> bitmaps, String fileName, Context context) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            return null;
        }
        
        // Letter page size in pixels at 72 DPI (standard PDF resolution)
        int pageWidth = 612;  // Letter width in points
        int pageHeight = 792; // Letter height in points
        
        PdfDocument pdfDocument = new PdfDocument();
        
        try {
            for (int i = 0; i < bitmaps.size(); i++) {
                Bitmap bitmap = bitmaps.get(i);
                
                // Create a page description
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
                
                // Start a page
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                
                // Calculate scaling to fit bitmap to page while maintaining aspect ratio
                float scaleWidth = ((float) pageWidth) / bitmap.getWidth();
                float scaleHeight = ((float) pageHeight) / bitmap.getHeight();
                float scale = Math.min(scaleWidth, scaleHeight);
                
                // Calculate position to center the bitmap on the page
                float scaledWidth = bitmap.getWidth() * scale;
                float scaledHeight = bitmap.getHeight() * scale;
                float x = (pageWidth - scaledWidth) / 2;
                float y = (pageHeight - scaledHeight) / 2;
                
                // Draw the bitmap onto the canvas
                canvas.drawBitmap(bitmap, null, 
                    new android.graphics.RectF(x, y, x + scaledWidth, y + scaledHeight), paint);
                
                // Finish the page
                pdfDocument.finishPage(page);
            }
            
            // Create the output file
            File documentsDir = new File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), "ScannedDocs");
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }
            
            File pdfFile = new File(documentsDir, fileName);
            
            // Write the document content
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
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
}