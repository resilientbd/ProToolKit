package com.faisal.protoolkit.ui.tools.document.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.DocumentEntity;
import com.faisal.protoolkit.data.entities.PageEntity;
import java.util.List;
import java.util.Collections;

public class DocumentDetailViewModel extends ViewModel {
    private final MutableLiveData<String> documentId = new MutableLiveData<>();
    private final MutableLiveData<DocumentEntity> document = new MutableLiveData<>();
    private final MutableLiveData<List<PageEntity>> pages = new MutableLiveData<>();
    private final AppDatabase database;

    public DocumentDetailViewModel(AppDatabase database) {
        this.database = database;
    }

    public void setDocumentId(String id) {
        documentId.setValue(id);
        loadDocument(id);
        loadPages(id);
    }

    private void loadDocument(String documentId) {
        // Load document from database
        new Thread(() -> {
            DocumentEntity doc = database.documentDao().getDocumentById(documentId);
            document.postValue(doc);
        }).start();
    }

    private void loadPages(String documentId) {
        // Load pages for this document from database
        new Thread(() -> {
            List<PageEntity> pageList = database.pageDao().getPagesByDocumentSync(documentId);
            pages.postValue(pageList);
        }).start();
    }

    public LiveData<DocumentEntity> getDocument() {
        return document;
    }

    public LiveData<List<PageEntity>> getPages() {
        return pages;
    }

    public void refresh() {
        String id = documentId.getValue();
        if (id != null) {
            loadDocument(id);
            loadPages(id);
        }
    }

    public void reorderPage(int fromIndex, int toIndex) {
        // Update indices in database
        new Thread(() -> {
            String docId = documentId.getValue();
            if (docId != null) {
                List<PageEntity> pageList = database.pageDao().getPagesByDocumentSync(docId);
                
                if (fromIndex >= 0 && fromIndex < pageList.size() && 
                    toIndex >= 0 && toIndex < pageList.size()) {
                    
                    // Get the page to move
                    PageEntity pageToMove = pageList.get(fromIndex);
                    
                    // Update all page indices as needed
                    if (fromIndex < toIndex) {
                        // Moving down: shift items between fromIndex+1 and toIndex up by 1
                        for (int i = fromIndex + 1; i <= toIndex; i++) {
                            PageEntity page = pageList.get(i);
                            page.index = page.index - 1;
                            database.pageDao().updatePage(page);
                        }
                    } else {
                        // Moving up: shift items between toIndex and fromIndex-1 down by 1
                        for (int i = toIndex; i < fromIndex; i++) {
                            PageEntity page = pageList.get(i);
                            page.index = page.index + 1;
                            database.pageDao().updatePage(page);
                        }
                    }
                    
                    // Update the moved page
                    pageToMove.index = toIndex;
                    database.pageDao().updatePage(pageToMove);
                    
                    // Reload pages to update UI
                    loadPages(docId);
                }
            }
        }).start();
    }

    public void deletePage(PageEntity page) {
        new Thread(() -> {
            database.pageDao().deletePage(page);
            
            // Update indices of remaining pages
            String docId = documentId.getValue();
            if (docId != null) {
                List<PageEntity> remainingPages = database.pageDao().getPagesByDocumentSync(docId);
                for (int i = 0; i < remainingPages.size(); i++) {
                    PageEntity updatedPage = remainingPages.get(i);
                    if (updatedPage.index != i) {
                        updatedPage.index = i;
                        database.pageDao().updatePage(updatedPage);
                    }
                }
                
                // Update document page count
                database.documentDao().updateDocumentMetadata(
                    docId,
                    remainingPages.size(),
                    0, // cover index
                    System.currentTimeMillis()
                );
            }
            
            loadPages(docId);
        }).start();
    }

    public void addPageToDocument(String documentId, String imagePath, int index) {
        new Thread(() -> {
            String pageId = java.util.UUID.randomUUID().toString();
            
            PageEntity page = new PageEntity(
                pageId,
                documentId,
                index,
                imagePath,
                null, // uri_render
                null, // edit_ops_json
                0, // width - will be populated later
                0, // height - will be populated later
                300, // dpi
                null, // ocr_lang
                false // ocr_done
            );
            
            database.pageDao().insertPage(page);
            
            // Update document page count
            List<PageEntity> allPages = database.pageDao().getPagesByDocumentSync(documentId);
            database.documentDao().updateDocumentMetadata(
                documentId,
                allPages.size(),
                0, // cover index
                System.currentTimeMillis()
            );
            
            loadPages(documentId);
        }).start();
    }

    public void updatePage(PageEntity page) {
        new Thread(() -> {
            page.updated_at = System.currentTimeMillis();
            database.pageDao().updatePage(page);
        }).start();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AppDatabase database;

        public Factory(AppDatabase database) {
            this.database = database;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(DocumentDetailViewModel.class)) {
                return (T) new DocumentDetailViewModel(database);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
