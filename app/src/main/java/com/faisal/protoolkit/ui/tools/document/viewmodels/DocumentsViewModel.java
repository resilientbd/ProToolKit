package com.faisal.protoolkit.ui.tools.document.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.DocumentEntity;
import java.util.List;

public class DocumentsViewModel extends ViewModel {
    private final LiveData<List<DocumentEntity>> documents;
    private final AppDatabase database;

    public DocumentsViewModel(AppDatabase database) {
        this.database = database;
        // The DAO returns LiveData that automatically observes database changes
        this.documents = database.documentDao().getAllDocuments();
    }

    public LiveData<List<DocumentEntity>> getDocuments() {
        return documents;
    }

    public void refreshDocuments() {
        // Room automatically updates LiveData when database changes, so no manual refresh needed
        // But we can potentially trigger a refresh by calling the DAO again if needed
    }
    
    public static class Factory implements ViewModelProvider.Factory {
        private final AppDatabase database;

        public Factory(AppDatabase database) {
            this.database = database;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(DocumentsViewModel.class)) {
                return (T) new DocumentsViewModel(database);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}