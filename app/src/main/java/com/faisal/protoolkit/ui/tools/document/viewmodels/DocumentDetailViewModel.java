package com.faisal.protoolkit.ui.tools.document.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.DocumentEntity;
import com.faisal.protoolkit.data.entities.PageEntity;
import java.util.List;

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

    public void reorderPage(int fromIndex, int toIndex) {
        // In a real implementation, you would update the database
        // For now, just reload the pages
        String currentDocId = this.documentId.getValue();
        if (currentDocId != null) {
            loadPages(currentDocId);
        }
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