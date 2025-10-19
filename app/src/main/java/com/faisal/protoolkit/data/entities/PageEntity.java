package com.faisal.protoolkit.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "pages")
public class PageEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String document_id;

    public int index;

    @NonNull
    public String uri_original;

    public String uri_render;

    public String edit_ops_json;

    public int width;

    public int height;

    public int dpi;

    public String ocr_lang;

    public boolean ocr_done;
    
    public long updated_at;

    public PageEntity(@NonNull String id, @NonNull String document_id, int index,
                      @NonNull String uri_original, String uri_render, String edit_ops_json,
                      int width, int height, int dpi, String ocr_lang, boolean ocr_done) {
        this.id = id;
        this.document_id = document_id;
        this.index = index;
        this.uri_original = uri_original;
        this.uri_render = uri_render;
        this.edit_ops_json = edit_ops_json;
        this.width = width;
        this.height = height;
        this.dpi = dpi;
        this.ocr_lang = ocr_lang;
        this.ocr_done = ocr_done;
        this.updated_at = System.currentTimeMillis();
    }
}