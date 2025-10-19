package com.faisal.protoolkit.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "exports")
public class ExportEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String document_id;

    @NonNull
    public String type; // PDF, JPGZIP, etc.

    @NonNull
    public String uri_file;

    public String settings_json;

    public long created_at;

    public ExportEntity(@NonNull String id, @NonNull String document_id, @NonNull String type,
                        @NonNull String uri_file, String settings_json, long created_at) {
        this.id = id;
        this.document_id = document_id;
        this.type = type;
        this.uri_file = uri_file;
        this.settings_json = settings_json;
        this.created_at = created_at;
    }
}