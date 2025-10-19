package com.faisal.protoolkit.data.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.faisal.protoolkit.data.entities.FolderEntity;
import java.util.List;

@Dao
public interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    LiveData<List<FolderEntity>> getAllFolders();

    @Query("SELECT * FROM folders WHERE parent_id = :parentId ORDER BY name ASC")
    LiveData<List<FolderEntity>> getSubFolders(String parentId);

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    FolderEntity getFolderById(String id);

    @Insert
    void insertFolder(FolderEntity folder);

    @Update
    void updateFolder(FolderEntity folder);

    @Delete
    void deleteFolder(FolderEntity folder);

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    void updateFolderName(String id, String name);
}