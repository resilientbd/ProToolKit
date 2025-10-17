package com.example.protoolkit.util;

import android.app.Application;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.example.protoolkit.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides enhanced file tools functionality including storage analysis and cleanup suggestions.
 */
public class FileToolsHelper {

    private final Application application;

    public FileToolsHelper(@NonNull Application application) {
        this.application = application;
    }

    /**
     * Gets comprehensive storage breakdown information.
     */
    @NonNull
    public StorageBreakdown getStorageBreakdown() {
        // Get internal storage info
        File internalStorage = Environment.getDataDirectory();
        long internalTotalBytes = getDirectorySize(internalStorage);
        long internalFreeBytes = getDirectoryFreeSpace(internalStorage);

        // Get external storage info if available
        long externalTotalBytes = 0;
        long externalFreeBytes = 0;
        if (Environment.getExternalStorageDirectory() != null) {
            File externalStorage = Environment.getExternalStorageDirectory();
            externalTotalBytes = getDirectorySize(externalStorage);
            externalFreeBytes = getDirectoryFreeSpace(externalStorage);
        }

        // Get specific directory sizes
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        long downloadsSize = getDirectorySize(downloadsDir);
        long picturesSize = getDirectorySize(picturesDir);
        long moviesSize = getDirectorySize(moviesDir);
        long musicSize = getDirectorySize(musicDir);
        long documentsSize = getDirectorySize(documentsDir);

        return new StorageBreakdown(
                new StorageInfo(internalFreeBytes, internalTotalBytes),
                new StorageInfo(externalFreeBytes, externalTotalBytes),
                new DirectoryInfo(downloadsDir.getName(), downloadsSize),
                new DirectoryInfo(picturesDir.getName(), picturesSize),
                new DirectoryInfo(moviesDir.getName(), moviesSize),
                new DirectoryInfo(musicDir.getName(), musicSize),
                new DirectoryInfo(documentsDir.getName(), documentsSize)
        );
    }

    /**
     * Gets cleanup suggestions based on storage analysis.
     */
    @NonNull
    public List<CleanupSuggestion> getCleanupSuggestions() {
        List<CleanupSuggestion> suggestions = new ArrayList<>();

        // Check cache size
        long cacheSize = getDirectorySize(application.getCacheDir());
        if (cacheSize > AppConstants.CACHE_SIZE_THRESHOLD) {
            suggestions.add(new CleanupSuggestion(
                    R.string.file_suggestion_cache_title_review,
                    R.string.file_suggestion_cache_description_with_size,
                    cacheSize,
                    CleanupSuggestion.Type.CACHE
            ));
        }

        // Check downloads folder size
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        long downloadsSize = getDirectorySize(downloadsDir);
        if (downloadsSize > AppConstants.DOWNLOADS_SIZE_THRESHOLD) {
            suggestions.add(new CleanupSuggestion(
                    R.string.file_suggestion_downloads_title_clean,
                    R.string.file_suggestion_downloads_description_with_size,
                    downloadsSize,
                    CleanupSuggestion.Type.DOWNLOADS
            ));
        }

        // Check media files size
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        long mediaSize = getDirectorySize(picturesDir) + getDirectorySize(moviesDir);
        if (mediaSize > AppConstants.MEDIA_SIZE_THRESHOLD) {
            suggestions.add(new CleanupSuggestion(
                    R.string.file_suggestion_media_title_backup,
                    R.string.file_suggestion_media_description_with_size,
                    mediaSize,
                    CleanupSuggestion.Type.MEDIA
            ));
        }

        return suggestions;
    }

    /**
     * Calculates the total size of a directory recursively.
     */
    private long getDirectorySize(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
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

    /**
     * Gets the free space available in a directory.
     */
    private long getDirectoryFreeSpace(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }
        return directory.getUsableSpace();
    }

    /**
     * Cleans the application cache.
     */
    public long cleanCache() {
        File cacheDir = application.getCacheDir();
        return deleteDirectoryContents(cacheDir);
    }

    /**
     * Clears the downloads folder.
     */
    public long clearDownloads() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return deleteDirectoryContents(downloadsDir);
    }

    /**
     * Backs up media files (simulated in this implementation).
     */
    public long backupMedia() {
        // In a real implementation, this would backup media files to cloud storage
        // For demonstration purposes, we'll simulate backing up by calculating the size
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        return getDirectorySize(picturesDir) + getDirectorySize(moviesDir);
    }

    /**
     * Deletes all contents of a directory.
     */
    private long deleteDirectoryContents(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }

        long deletedSize = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deletedSize += deleteDirectoryContents(file);
                    file.delete(); // Delete empty directory
                } else {
                    deletedSize += file.length();
                    file.delete();
                }
            }
        }
        return deletedSize;
    }

    // Data classes for file tools
    public static class StorageBreakdown {
        public final StorageInfo internalStorage;
        public final StorageInfo externalStorage;
        public final DirectoryInfo downloads;
        public final DirectoryInfo pictures;
        public final DirectoryInfo movies;
        public final DirectoryInfo music;
        public final DirectoryInfo documents;

        public StorageBreakdown(
                StorageInfo internalStorage,
                StorageInfo externalStorage,
                DirectoryInfo downloads,
                DirectoryInfo pictures,
                DirectoryInfo movies,
                DirectoryInfo music,
                DirectoryInfo documents) {
            this.internalStorage = internalStorage;
            this.externalStorage = externalStorage;
            this.downloads = downloads;
            this.pictures = pictures;
            this.movies = movies;
            this.music = music;
            this.documents = documents;
        }
    }

    public static class StorageInfo {
        public final long freeBytes;
        public final long totalBytes;

        public StorageInfo(long freeBytes, long totalBytes) {
            this.freeBytes = freeBytes;
            this.totalBytes = totalBytes;
        }
    }

    public static class DirectoryInfo {
        public final String name;
        public final long sizeBytes;

        public DirectoryInfo(String name, long sizeBytes) {
            this.name = name;
            this.sizeBytes = sizeBytes;
        }
    }

    public static class CleanupSuggestion {
        public final int titleResId;
        public final int descriptionResId;
        public final long sizeBytes;
        public final Type type;

        public enum Type {
            CACHE,
            DOWNLOADS,
            MEDIA
        }

        public CleanupSuggestion(int titleResId, int descriptionResId, long sizeBytes, Type type) {
            this.titleResId = titleResId;
            this.descriptionResId = descriptionResId;
            this.sizeBytes = sizeBytes;
            this.type = type;
        }
    }
}