package com.faisal.protoolkit.ui.tools.document;

import android.graphics.Bitmap;

/**
 * Model class representing a scanned document page
 */
public class DocumentItem {
    private String title;
    private Bitmap bitmap;
    private long timestamp;

    public DocumentItem(String title, Bitmap bitmap, long timestamp) {
        this.title = title;
        this.bitmap = bitmap;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}