# CamScanner-style Document Management System

## Overview
This implementation provides a complete CamScanner-style document management system with non-destructive editing, organized storage, and PDF export capabilities.

## Features
1. **Storage Layout** (App files)
   - Root: /Android/data/<PKG>/files/
   - Documents: files/scans/<DOC_ID>/
       - meta.json                   (document metadata)
       - page_0001/original.jpg      (untouched)
       - page_0001/edit.json         (non-destructive ops)
       - page_0001/render.jpg        (optional cached render)
       - page_0002/...
       - exports/v1.pdf              (optional)
   - Cache: files/cache/ (thumbnails, previews)
   - Temp:  files/tmp/   (capture/import staging)

2. **Database (Room)**
   - Documents table: id, title, folder_id, page_count, cover_page_index, labels_json, created_at, updated_at, status
   - Pages table: id, document_id, index, uri_original, uri_render, edit_ops_json, width, height, dpi, ocr_lang, ocr_done
   - Folders table: id, name, parent_id
   - Exports table: id, document_id, type, uri_file, settings_json, created_at

3. **Non-destructive Editing Model**
   - EditOps model with crop, warp, rotate, filter, denoise, deskew operations
   - EditOpsUtil for loading/saving/merging operations

4. **Rendering Pipeline**
   - RenderEngine with preview and final rendering capabilities
   - OOM-safe bitmap decoding with inSampleSize

5. **Caching & Thumbnails** (WorkManager)
   - GenerateDocThumbWorker
   - GeneratePagePreviewWorker
   - CleanCacheWorker

6. **UI/Flows**
   - DocumentsFragment: Grid of documents
   - DocumentDetailFragment: Page management
   - EditorFragment: Single page editing
   - FoldersFragment: Folder management
   - TrashFragment: Deleted documents

## Integration Steps

### 1. Dependencies
Add these to your app's build.gradle:
```gradle
dependencies {
    implementation "androidx.room:room-runtime:2.4.3"
    implementation "androidx.room:room-ktx:2.4.3"
    annotationProcessor "androidx.room:room-compiler:2.4.3"
    
    implementation "androidx.work:work-runtime:2.8.1"
    implementation "androidx.work:work-ktx:2.8.1"
    
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // For OpenCV integration (optional)
    // implementation 'org.opencv:opencv-android:4.6.0'
}
```

### 2. Permissions
The implementation uses scoped storage so no additional permissions needed beyond your existing ones.

### 3. ProGuard Rules
Add to proguard-rules.pro:
```proguard
-keep class com.faisal.protoolkit.model.EditOps { *; }
-keep class com.faisal.protoolkit.model.EditOps$Filter { *; }
```

## Configuration

### Constants
All configurable constants are in Config.java:
- CACHE_QUOTA_BYTES = 150 * 1024 * 1024
- THUMB_SIZE_PX = 512
- PREVIEW_MAX_WIDTH_PX = 1600
- EXPORT_DEFAULT_PAGE = "A4"
- EXPORT_DEFAULT_QUALITY = 88

## Migration Plan
1. Current document scanner UI can be refactored to DocumentDetailFragment
2. Existing scanned images can be imported into new document structure
3. Database schema will be created on first run

## OpenCV Integration Hooks
- TODO comments are placed in RenderEngine and ImageFilters where OpenCV operations can be plugged in
- Current implementation uses basic Android APIs with hooks for:
  - Adaptive thresholding
  - Perspective warping
  - Advanced noise reduction

## PDFBox Integration (Alternative)
- Current implementation uses Android's PdfDocument
- For advanced PDF features, consider switching to PDFBox-Android
- ExportPDFWorker can be modified to use PDFBox instead

## Testing
- Unit tests available for EditOps serialization
- Database operations tested with Room
- UI flows tested with instrumentation tests

## Performance Considerations
- All heavy operations run in WorkManager
- Bitmap decoding uses inSampleSize to prevent OOM
- Caching implemented with LRU eviction