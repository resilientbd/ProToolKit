package com.example.protoolkit.ui.tools.file;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.protoolkit.data.file.FileToolsRepository;
import com.example.protoolkit.domain.model.SuggestionItem;
import com.example.protoolkit.ui.base.BaseViewModel;
import com.example.protoolkit.util.AppExecutors;

import java.util.List;

/**
 * Provides comprehensive file and storage management tools.
 */
public class FileToolsViewModel extends BaseViewModel {

    private final FileToolsRepository repository;
    
    // Storage summary
    private final MutableLiveData<String> storageSummary = new MutableLiveData<>("");
    private final MutableLiveData<Integer> storageProgress = new MutableLiveData<>(0);
    
    // Detailed storage breakdown
    private final MutableLiveData<String> appsDataSize = new MutableLiveData<>("0 MB");
    private final MutableLiveData<String> imagesSize = new MutableLiveData<>("0 MB");
    private final MutableLiveData<String> videosSize = new MutableLiveData<>("0 MB");
    private final MutableLiveData<String> audioSize = new MutableLiveData<>("0 MB");
    private final MutableLiveData<String> documentsSize = new MutableLiveData<>("0 MB");
    private final MutableLiveData<String> downloadsSize = new MutableLiveData<>("0 MB");
    
    // Cleanup suggestions
    private final MutableLiveData<List<SuggestionItem>> suggestions = new MutableLiveData<>();

    public FileToolsViewModel(@NonNull FileToolsRepository repository) {
        this.repository = repository;
        loadData();
    }

    // Getters for storage summary
    public LiveData<String> getStorageSummary() { return storageSummary; }
    public LiveData<Integer> getStorageProgress() { return storageProgress; }
    
    // Getters for detailed storage breakdown
    public LiveData<String> getAppsDataSize() { return appsDataSize; }
    public LiveData<String> getImagesSize() { return imagesSize; }
    public LiveData<String> getVideosSize() { return videosSize; }
    public LiveData<String> getAudioSize() { return audioSize; }
    public LiveData<String> getDocumentsSize() { return documentsSize; }
    public LiveData<String> getDownloadsSize() { return downloadsSize; }
    
    // Getters for suggestions
    public LiveData<List<SuggestionItem>> getSuggestions() { return suggestions; }

    public void loadData() {
        setLoading(true);
        AppExecutors.io().execute(() -> {
            try {
                // Load storage summary
                storageSummary.postValue(repository.getStorageSummary());
                storageProgress.postValue(repository.getStorageProgress());
                
                // Load detailed storage breakdown
                appsDataSize.postValue(repository.getAppsDataSize());
                imagesSize.postValue(repository.getImagesSize());
                videosSize.postValue(repository.getVideosSize());
                audioSize.postValue(repository.getAudioSize());
                documentsSize.postValue(repository.getDocumentsSize());
                downloadsSize.postValue(repository.getDownloadsSize());
                
                // Load cleanup suggestions
                suggestions.postValue(repository.getSuggestions());
            } catch (Exception e) {
                postError("Failed to load storage data: " + e.getMessage());
            } finally {
                setLoading(false);
            }
        });
    }
    
    // Action methods for file management
    public void cleanCache() {
        setLoading(true);
        AppExecutors.io().execute(() -> {
            try {
                repository.cleanCache();
                // Refresh data after cleaning
                loadData();
            } catch (Exception e) {
                postError("Cache cleaning failed: " + e.getMessage());
                setLoading(false);
            }
        });
    }
    
    public void clearDownloads() {
        setLoading(true);
        AppExecutors.io().execute(() -> {
            try {
                repository.clearDownloads();
                // Refresh data after clearing
                loadData();
            } catch (Exception e) {
                postError("Downloads clearing failed: " + e.getMessage());
                setLoading(false);
            }
        });
    }
    
    public void backupMedia() {
        setLoading(true);
        AppExecutors.io().execute(() -> {
            try {
                repository.backupMedia();
                // Refresh data after backup
                loadData();
            } catch (Exception e) {
                postError("Media backup failed: " + e.getMessage());
                setLoading(false);
            }
        });
    }
}