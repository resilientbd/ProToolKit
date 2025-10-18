package com.faisal.protoolkit.data.file;

import android.app.Application;
import android.os.Environment;
import android.os.StatFs;

import androidx.annotation.NonNull;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.domain.model.SuggestionItem;
import com.faisal.protoolkit.util.FormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides comprehensive file and storage management tools.
 */
public class FileToolsRepository {

    private final Application application;

    public FileToolsRepository(@NonNull Application application) {
        this.application = application;
    }

    // Storage summary methods
    public String getStorageSummary() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        long availableBlocks = statFs.getAvailableBlocksLong();
        long totalBytes = blockSize * totalBlocks;
        long freeBytes = blockSize * availableBlocks;
        return application.getString(R.string.device_info_storage_template,
                FormatUtils.formatBytes(freeBytes),
                FormatUtils.formatBytes(totalBytes));
    }
    
    public int getStorageProgress() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getAbsolutePath());
        long totalBlocks = statFs.getBlockCountLong();
        long availableBlocks = statFs.getAvailableBlocksLong();
        long usedBlocks = totalBlocks - availableBlocks;
        
        if (totalBlocks == 0) return 0;
        
        return (int) ((usedBlocks * 100) / totalBlocks);
    }

    // Detailed storage breakdown methods
    public String getAppsDataSize() {
        // This would typically involve checking the app's private data directory
        // For now, we'll return a realistic placeholder value
        long size = 5000L * 1024 * 1024; // 5GB for apps and data
        return FormatUtils.formatBytes(size);
    }
    
    public String getImagesSize() {
        // Check standard image directories
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        long size = getDirectorySize(picturesDir);
        return FormatUtils.formatBytes(size);
    }
    
    public String getVideosSize() {
        // Check standard video directories
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        long size = getDirectorySize(moviesDir);
        return FormatUtils.formatBytes(size);
    }
    
    public String getAudioSize() {
        // Check standard audio directories
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        long size = getDirectorySize(musicDir);
        return FormatUtils.formatBytes(size);
    }
    
    public String getDocumentsSize() {
        // Check standard documents directories
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        long size = getDirectorySize(documentsDir);
        return FormatUtils.formatBytes(size);
    }
    
    public String getDownloadsSize() {
        // Check downloads directory
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        long size = getDirectorySize(downloadsDir);
        return FormatUtils.formatBytes(size);
    }
    
    // Cleanup suggestions
    public List<SuggestionItem> getSuggestions() {
        List<SuggestionItem> items = new ArrayList<>();
        
        // Analyze storage and provide real suggestions based on actual files
        long cacheSize = getCacheSize();
        long downloadsSize = getDownloadsSizeValue();
        long mediaSize = getMediaSize();
        
        if (cacheSize > 100 * 1024 * 1024) { // More than 100MB
            items.add(new SuggestionItem(
                    application.getString(R.string.file_suggestion_cache_title_review),
                    application.getString(R.string.file_suggestion_cache_description_with_size, FormatUtils.formatBytes(cacheSize)),
                    R.drawable.ic_tool_file));
        }
        
        if (downloadsSize > 50 * 1024 * 1024) { // More than 50MB
            items.add(new SuggestionItem(
                    application.getString(R.string.file_suggestion_downloads_title_clean),
                    application.getString(R.string.file_suggestion_downloads_description_with_size, FormatUtils.formatBytes(downloadsSize)),
                    R.drawable.ic_tool_file));
        }
        
        if (mediaSize > 1000 * 1024 * 1024) { // More than 1GB
            items.add(new SuggestionItem(
                    application.getString(R.string.file_suggestion_media_title_backup),
                    application.getString(R.string.file_suggestion_media_description_with_size, FormatUtils.formatBytes(mediaSize)),
                    R.drawable.ic_tool_file));
        }
        
        // Add more suggestions if list is still empty
        if (items.isEmpty()) {
            items.add(new SuggestionItem(
                    R.string.file_suggestion_cache_title_review,
                    R.string.file_suggestion_cache_description,
                    R.drawable.ic_tool_file));
            items.add(new SuggestionItem(
                    R.string.file_suggestion_downloads_title_clean,
                    R.string.file_suggestion_downloads_description,
                    R.drawable.ic_tool_file));
            items.add(new SuggestionItem(
                    R.string.file_suggestion_media_title_backup,
                    R.string.file_suggestion_media_description,
                    R.drawable.ic_tool_file));
        }
        
        return Collections.unmodifiableList(items);
    }
    
    // Action methods for file management
    public void cleanCache() {
        // Clean app cache
        File cacheDir = application.getCacheDir();
        if (cacheDir != null) {
            deleteDirectory(cacheDir);
        }
        
        File externalCacheDir = application.getExternalCacheDir();
        if (externalCacheDir != null) {
            deleteDirectory(externalCacheDir);
        }
    }
    
    public void clearDownloads() {
        // Clear downloads directory
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir != null && downloadsDir.exists()) {
            // Note: In a real implementation, we would be more selective about what to delete
            // For demonstration purposes, we're just showing the concept
            deleteDirectory(downloadsDir);
        }
    }
    
    public void backupMedia() {
        // Backup media files
        // In a real implementation, this would connect to cloud storage services
        // For demonstration purposes, we're just showing the concept
    }
    
    // Helper methods
    private long getDirectorySize(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }
        
        if (directory.isFile()) {
            return directory.length();
        }
        
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += getDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }
        
        return size;
    }
    
    private long getCacheSize() {
        try {
            long totalSize = 0;
            File cacheDir = application.getCacheDir();
            if (cacheDir != null) {
                totalSize += getDirectorySize(cacheDir);
            }
            
            File externalCacheDir = application.getExternalCacheDir();
            if (externalCacheDir != null) {
                totalSize += getDirectorySize(externalCacheDir);
            }
            
            return totalSize;
        } catch (Exception e) {
            return 0; // Return 0 if unable to calculate
        }
    }
    
    private long getDownloadsSizeValue() {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (downloadsDir != null && downloadsDir.exists()) {
                return getDirectorySize(downloadsDir);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long getMediaSize() {
        try {
            long totalSize = 0;
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (picturesDir != null && picturesDir.exists()) {
                totalSize += getDirectorySize(picturesDir);
            }
            
            File videosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            if (videosDir != null && videosDir.exists()) {
                totalSize += getDirectorySize(videosDir);
            }
            
            File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            if (musicDir != null && musicDir.exists()) {
                totalSize += getDirectorySize(musicDir);
            }
            
            return totalSize;
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Helper method to delete directory contents
    private void deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
    }
}