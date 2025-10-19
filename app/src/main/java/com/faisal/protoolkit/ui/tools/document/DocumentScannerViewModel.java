package com.faisal.protoolkit.ui.tools.document;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

/**
 * ViewModel for the document scanner feature
 */
public class DocumentScannerViewModel extends AndroidViewModel {
    private final MutableLiveData<List<DocumentItem>> scannedDocuments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>();

    public DocumentScannerViewModel(@NonNull Application application) {
        super(application);
        // Initialize with empty list
        scannedDocuments.setValue(null);
        isProcessing.setValue(false);
    }

    public LiveData<List<DocumentItem>> getScannedDocuments() {
        return scannedDocuments;
    }

    public LiveData<Boolean> getIsProcessing() {
        return isProcessing;
    }

    public void addDocument(DocumentItem document) {
        List<DocumentItem> currentList = scannedDocuments.getValue();
        if (currentList == null) {
            currentList = new java.util.ArrayList<>();
        }
        currentList.add(document);
        scannedDocuments.setValue(currentList);
    }

    public void removeDocument(int position) {
        List<DocumentItem> currentList = scannedDocuments.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            scannedDocuments.setValue(currentList);
        }
    }

    public void setIsProcessing(boolean processing) {
        isProcessing.setValue(processing);
    }

    public void clearAllDocuments() {
        scannedDocuments.setValue(new java.util.ArrayList<>());
    }
}