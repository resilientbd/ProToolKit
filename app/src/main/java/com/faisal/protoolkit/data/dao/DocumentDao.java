package com.faisal.protoolkit.data.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.faisal.protoolkit.data.entities.DocumentEntity;
import java.util.List;

@Dao
public interface DocumentDao {
    @Query("SELECT * FROM documents WHERE status != 'TRASHED' ORDER BY updated_at DESC")
    LiveData<List<DocumentEntity>> getAllDocuments();

    @Query("SELECT * FROM documents WHERE folder_id = :folderId AND status != 'TRASHED' ORDER BY updated_at DESC")
    LiveData<List<DocumentEntity>> getDocumentsByFolder(String folderId);

    @Query("SELECT * FROM documents WHERE status = 'TRASHED' ORDER BY updated_at DESC")
    LiveData<List<DocumentEntity>> getTrashedDocuments();

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    DocumentEntity getDocumentById(String id);

    @Insert
    void insertDocument(DocumentEntity document);

    @Update
    void updateDocument(DocumentEntity document);

    @Delete
    void deleteDocument(DocumentEntity document);

    @Query("UPDATE documents SET title = :title WHERE id = :id")
    void updateDocumentTitle(String id, String title);

    @Query("UPDATE documents SET folder_id = :folderId WHERE id = :id")
    void updateDocumentFolder(String id, String folderId);

    @Query("UPDATE documents SET status = 'TRASHED' WHERE id = :id")
    void trashDocument(String id);

    @Query("UPDATE documents SET status = 'ACTIVE' WHERE id = :id")
    void restoreDocument(String id);

    @Query("DELETE FROM documents WHERE id = :id")
    void purgeDocument(String id);

    @Query("UPDATE documents SET page_count = :pageCount, cover_page_index = :coverPageIndex, updated_at = :updatedAt WHERE id = :id")
    void updateDocumentMetadata(String id, int pageCount, int coverPageIndex, long updatedAt);
}