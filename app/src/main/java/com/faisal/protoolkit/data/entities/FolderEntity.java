package com.faisal.protoolkit.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "folders")
public class FolderEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String name;

    public String parent_id;

    public FolderEntity(@NonNull String id, @NonNull String name, String parent_id) {
        this.id = id;
        this.name = name;
        this.parent_id = parent_id;
    }
}