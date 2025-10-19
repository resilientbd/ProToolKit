package com.faisal.protoolkit.ui.tools.document.viewmodels;

import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.model.EditOps;
import com.faisal.protoolkit.util.RenderEngine;

public class EditorViewModel extends ViewModel {
    private final MutableLiveData<String> documentId = new MutableLiveData<>();
    private final MutableLiveData<Integer> pageIndex = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> pagePreview = new MutableLiveData<>();
    private EditOps currentEditOps;
    private final AppDatabase database;
    private final RenderEngine renderEngine;

    public EditorViewModel(AppDatabase database, RenderEngine renderEngine) {
        this.database = database;
        this.renderEngine = renderEngine;
    }

    public void setDocumentId(String id) {
        documentId.setValue(id);
    }

    public void setPageIndex(int index) {
        pageIndex.setValue(index);
        loadCurrentEdits(index);
    }

    private void loadCurrentEdits(int pageIndex) {
        // Load current edit operations for this page from database
        // For now, we'll start with default edit ops
        currentEditOps = new EditOps();
    }

    public LiveData<Bitmap> getPagePreview() {
        return pagePreview;
    }

    public void updateFilterMode(String mode) {
        if (currentEditOps == null) currentEditOps = new EditOps();
        if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
        currentEditOps.filter.mode = mode;
        updatePreview();
    }

    public void updateContrast(float contrast) {
        if (currentEditOps == null) currentEditOps = new EditOps();
        if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
        currentEditOps.filter.contrast = contrast;
        updatePreview();
    }

    public void updateBrightness(float brightness) {
        if (currentEditOps == null) currentEditOps = new EditOps();
        if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
        currentEditOps.filter.brightness = brightness;
        updatePreview();
    }

    public void updateSharpen(float sharpen) {
        if (currentEditOps == null) currentEditOps = new EditOps();
        if (currentEditOps.filter == null) currentEditOps.filter = new EditOps.Filter();
        currentEditOps.filter.sharpen = sharpen;
        updatePreview();
    }

    public void rotateLeft() {
        if (currentEditOps == null) currentEditOps = new EditOps();
        // Rotate 90 degrees counter-clockwise
        currentEditOps.rotate = (currentEditOps.rotate - 90 + 360) % 360;
        updatePreview();
    }

    public void rotateRight() {
        if (currentEditOps == null) currentEditOps = new EditOps();
        // Rotate 90 degrees clockwise
        currentEditOps.rotate = (currentEditOps.rotate + 90) % 360;
        updatePreview();
    }

    private void updatePreview() {
        // In a real implementation, you would render the preview with the current edits
        String docId = documentId.getValue();
        Integer pageNum = pageIndex.getValue();
        
        if (docId != null && pageNum != null) {
            // Render the preview with current edit ops
            // This would be done on a background thread
        }
    }

    public void saveEdits() {
        // Save current edit operations to database and file
        String docId = documentId.getValue();
        Integer pageNum = pageIndex.getValue();
        
        if (docId != null && pageNum != null && currentEditOps != null) {
            // Save to database
            String editOpsJson = com.faisal.protoolkit.util.EditOpsUtil.serialize(currentEditOps);
            
            // In a real implementation, you would update the database
            // and save the edit ops to the file system
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AppDatabase database;
        private final RenderEngine renderEngine;

        public Factory(AppDatabase database, RenderEngine renderEngine) {
            this.database = database;
            this.renderEngine = renderEngine;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(EditorViewModel.class)) {
                return (T) new EditorViewModel(database, renderEngine);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}