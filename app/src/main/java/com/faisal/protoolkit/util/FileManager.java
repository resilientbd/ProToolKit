package com.faisal.protoolkit.util;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FileManager {
    private static final String TAG = "FileManager";
    
    // App-specific directories
    private final File rootDir;
    private final File scansDir;
    private final File cacheDir;
    private final File tmpDir;
    
    // Subdirectories
    private static final String SCANS_SUBDIR = "scans";
    private static final String CACHE_SUBDIR = "cache";
    private static final String TMP_SUBDIR = "tmp";
    private static final String EXPORTS_SUBDIR = "exports";
    private static final String PAGES_SUBDIR = "pages";

    public FileManager(Context context) {
        rootDir = context.getExternalFilesDir(null);
        if (rootDir == null) {
            throw new IllegalStateException("External files directory is null");
        }
        
        scansDir = new File(rootDir, SCANS_SUBDIR);
        cacheDir = new File(rootDir, CACHE_SUBDIR);
        tmpDir = new File(rootDir, TMP_SUBDIR);
        
        // Create directories if they don't exist
        ensureDirExists(scansDir);
        ensureDirExists(cacheDir);
        ensureDirExists(tmpDir);
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getScansDir() {
        return scansDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public File getTmpDir() {
        return tmpDir;
    }

    public File getDocumentDir(String documentId) {
        return new File(scansDir, documentId);
    }

    public File getPageDir(String documentId, int pageIndex) {
        String pageDirName = String.format("page_%04d", pageIndex + 1);
        return new File(getDocumentDir(documentId), pageDirName);
    }

    public File getExportsDir(String documentId) {
        return new File(getDocumentDir(documentId), EXPORTS_SUBDIR);
    }

    public File getOriginalImageFile(String documentId, int pageIndex) {
        File pageDir = getPageDir(documentId, pageIndex);
        return new File(pageDir, "original.jpg");
    }

    public File getRenderedImageFile(String documentId, int pageIndex) {
        File pageDir = getPageDir(documentId, pageIndex);
        return new File(pageDir, "render.jpg");
    }

    public File getEditOpsFile(String documentId, int pageIndex) {
        File pageDir = getPageDir(documentId, pageIndex);
        return new File(pageDir, "edit.json");
    }

    public File getMetadataFile(String documentId) {
        return new File(getDocumentDir(documentId), "meta.json");
    }

    public File getExportFile(String documentId, String exportType, int version) {
        File exportsDir = getExportsDir(documentId);
        ensureDirExists(exportsDir);
        return new File(exportsDir, String.format("v%d.%s", version, exportType.toLowerCase()));
    }

    public boolean createDocumentDir(String documentId) {
        File docDir = getDocumentDir(documentId);
        return ensureDirExists(docDir);
    }

    public boolean createPageDir(String documentId, int pageIndex) {
        File pageDir = getPageDir(documentId, pageIndex);
        return ensureDirExists(pageDir);
    }

    public boolean moveFile(File source, File dest) {
        if (!source.exists()) {
            Log.e(TAG, "Source file does not exist: " + source.getAbsolutePath());
            return false;
        }

        // Create parent directory if it doesn't exist
        File parentDir = dest.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!ensureDirExists(parentDir)) {
                Log.e(TAG, "Failed to create parent directory: " + parentDir.getAbsolutePath());
                return false;
            }
        }

        // Try rename first (faster if on same filesystem)
        if (source.renameTo(dest)) {
            Log.d(TAG, "Moved file using rename: " + source.getAbsolutePath() + " -> " + dest.getAbsolutePath());
            return true;
        }

        // If rename fails, try copy and delete
        if (copyFile(source, dest)) {
            boolean deleteSuccess = source.delete();
            if (deleteSuccess) {
                Log.d(TAG, "Moved file using copy+delete: " + source.getAbsolutePath() + " -> " + dest.getAbsolutePath());
                return true;
            } else {
                Log.e(TAG, "Failed to delete source file after copy: " + source.getAbsolutePath());
                // Keep the copy but return false to indicate partial failure
                return false;
            }
        }

        Log.e(TAG, "Failed to move file: " + source.getAbsolutePath() + " -> " + dest.getAbsolutePath());
        return false;
    }

    public boolean copyFile(File source, File dest) {
        if (!source.exists()) {
            Log.e(TAG, "Source file does not exist: " + source.getAbsolutePath());
            return false;
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            
            // Create parent directory if it doesn't exist
            File parentDir = dest.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!ensureDirExists(parentDir)) {
                    Log.e(TAG, "Failed to create parent directory: " + parentDir.getAbsolutePath());
                    return false;
                }
            }
            
            out = new FileOutputStream(dest);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            
            Log.d(TAG, "Copied file: " + source.getAbsolutePath() + " -> " + dest.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying file: " + e.getMessage(), e);
            return false;
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException e) { /* ignore */ }
            }
            if (out != null) {
                try { out.close(); } catch (IOException e) { /* ignore */ }
            }
        }
    }

    public boolean ensureDirExists(File dir) {
        if (dir.exists()) {
            return dir.isDirectory();
        }
        return dir.mkdirs();
    }

    public boolean deleteDocument(String documentId) {
        File docDir = getDocumentDir(documentId);
        return deleteRecursively(docDir);
    }

    public boolean deletePage(String documentId, int pageIndex) {
        File pageDir = getPageDir(documentId, pageIndex);
        return deleteRecursively(pageDir);
    }

    private boolean deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return true; // Nothing to delete
        }

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursively(child)) {
                        return false;
                    }
                }
            }
        }

        return file.delete();
    }

    public static String generateDocumentId() {
        return UUID.randomUUID().toString();
    }

    public static String generatePageId() {
        return UUID.randomUUID().toString();
    }

    public boolean cleanCache(long maxBytes) {
        File[] files = cacheDir.listFiles();
        if (files == null || files.length == 0) {
            return true;
        }

        // Calculate total cache size
        long totalSize = 0;
        for (File file : files) {
            if (file.isFile()) {
                totalSize += file.length();
            }
        }

        if (totalSize <= maxBytes) {
            return true; // Cache size is within limit
        }

        // Sort files by last modified time (oldest first)
        java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

        // Delete oldest files until cache is within limit
        for (File file : files) {
            if (totalSize <= maxBytes) {
                break;
            }
            if (file.delete()) {
                totalSize -= file.length();
                Log.d(TAG, "Deleted cache file: " + file.getName());
            }
        }

        return true;
    }
}