package com.faisal.protoolkit.ui.tools.document.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.FolderEntity;
import java.util.List;

public class FoldersViewModel extends ViewModel {
    private final MutableLiveData<List<FolderEntity>> folders = new MutableLiveData<>();
    private final AppDatabase database;

    public FoldersViewModel(AppDatabase database) {
        this.database = database;
        loadFolders();
    }

    private void loadFolders() {
        // Load all folders from database
        new Thread(() -> {
            List<FolderEntity> folderList = database.folderDao().getAllFolders().getValue();
            folders.postValue(folderList);
        }).start();
    }

    public LiveData<List<FolderEntity>> getFolders() {
        return folders;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AppDatabase database;

        public Factory(AppDatabase database) {
            this.database = database;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FoldersViewModel.class)) {
                return (T) new FoldersViewModel(database);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}