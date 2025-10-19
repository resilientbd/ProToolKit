package com.faisal.protoolkit.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "documents")
public class DocumentEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String title;

    public String folder_id;

    public int page_count;

    public int cover_page_index;

    public String labels_json;

    public long created_at;

    public long updated_at;

    @NonNull
    public String status; // DRAFT, ACTIVE, TRASHED

    public DocumentEntity(@NonNull String id, @NonNull String title, String folder_id,
                          int page_count, int cover_page_index, String labels_json,
                          long created_at, long updated_at, @NonNull String status) {
        this.id = id;
        this.title = title;
        this.folder_id = folder_id;
        this.page_count = page_count;
        this.cover_page_index = cover_page_index;
        this.labels_json = labels_json;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.status = status;
    }
}