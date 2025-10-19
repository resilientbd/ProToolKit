package com.faisal.protoolkit.data.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.faisal.protoolkit.data.entities.ExportEntity;
import java.util.List;

@Dao
public interface ExportDao {
    @Query("SELECT * FROM exports WHERE document_id = :documentId ORDER BY created_at DESC")
    LiveData<List<ExportEntity>> getExportsByDocument(String documentId);

    @Query("SELECT * FROM exports WHERE id = :id LIMIT 1")
    ExportEntity getExportById(String id);

    @Insert
    void insertExport(ExportEntity export);

    @Update
    void updateExport(ExportEntity export);

    @Delete
    void deleteExport(ExportEntity export);

    @Query("DELETE FROM exports WHERE document_id = :documentId")
    void deleteExportsByDocument(String documentId);
}