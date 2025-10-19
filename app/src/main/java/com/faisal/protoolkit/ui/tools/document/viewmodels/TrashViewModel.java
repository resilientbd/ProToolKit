package com.faisal.protoolkit.ui.tools.document.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.DocumentEntity;
import java.util.List;

public class TrashViewModel extends ViewModel {
    private final MutableLiveData<List<DocumentEntity>> trashedDocuments = new MutableLiveData<>();
    private final AppDatabase database;

    public TrashViewModel(AppDatabase database) {
        this.database = database;
        loadTrashedDocuments();
    }

    private void loadTrashedDocuments() {
        // Load trashed documents from database
        new Thread(() -> {
            List<DocumentEntity> docs = database.documentDao().getTrashedDocuments().getValue();
            trashedDocuments.postValue(docs);
        }).start();
    }

    public LiveData<List<DocumentEntity>> getTrashedDocuments() {
        return trashedDocuments;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AppDatabase database;

        public Factory(AppDatabase database) {
            this.database = database;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TrashViewModel.class)) {
                return (T) new TrashViewModel(database);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}