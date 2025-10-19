package com.faisal.protoolkit.data.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.faisal.protoolkit.data.entities.PageEntity;
import java.util.List;

@Dao
public interface PageDao {
    @Query("SELECT * FROM pages WHERE document_id = :documentId ORDER BY `index` ASC")
    LiveData<List<PageEntity>> getPagesByDocument(String documentId);

    @Query("SELECT * FROM pages WHERE document_id = :documentId ORDER BY `index` ASC")
    List<PageEntity> getPagesByDocumentSync(String documentId);

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    PageEntity getPageById(String id);

    @Insert
    void insertPage(PageEntity page);

    @Update
    void updatePage(PageEntity page);

    @Delete
    void deletePage(PageEntity page);

    @Query("DELETE FROM pages WHERE document_id = :documentId")
    void deletePagesByDocument(String documentId);

    @Query("UPDATE pages SET `index` = :newIndex WHERE id = :pageId")
    void updatePageIndex(String pageId, int newIndex);

    @Query("UPDATE pages SET edit_ops_json = :editOpsJson, updated_at = :updatedAt WHERE id = :pageId")
    void updatePageEditOps(String pageId, String editOpsJson, long updatedAt);

    @Query("UPDATE pages SET uri_render = :renderUri WHERE id = :pageId")
    void updatePageRenderUri(String pageId, String renderUri);

    // Swap indices for reordering
    @Query("UPDATE pages SET `index` = :index2 WHERE id = :id1")
    void updatePageIndexById(String id1, int index2);

    @Query("UPDATE pages SET `index` = :index1 WHERE id = :id2")
    void updatePageIndexById2(String id2, int index1);

    @Query("SELECT * FROM pages WHERE document_id = :documentId AND `index` = :pageIndex LIMIT 1")
    PageEntity getPageByDocumentAndIndex(String documentId, int pageIndex);
    
    @Query("SELECT MAX(`index`) FROM pages WHERE document_id = :documentId")
    Integer getMaxPageIndex(String documentId);
}