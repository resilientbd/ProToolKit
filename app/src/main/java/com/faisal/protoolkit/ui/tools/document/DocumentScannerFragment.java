package com.faisal.protoolkit.ui.tools.document;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faisal.protoolkit.BuildConfig;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentDocumentScannerBinding;
import com.faisal.protoolkit.ui.base.BaseFragment;
import com.faisal.protoolkit.util.ImageUtils;
import com.faisal.protoolkit.util.ServiceLocator;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Document Scanner fragment implementing ML Kit's document scanner API
 */
public class DocumentScannerFragment extends BaseFragment {

    private FragmentDocumentScannerBinding binding;
    private DocumentScannerViewModel viewModel;
    private DocumentAdapter documentAdapter;
    private List<DocumentItem> scannedDocuments;
    private GmsDocumentScanner documentScanner;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> documentScanLauncher;

    public DocumentScannerFragment() {
        super(R.layout.fragment_document_scanner);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the document scanner
        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(true)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                        GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .build();
        documentScanner = GmsDocumentScanning.getClient(options);

        // Initialize documents list
        scannedDocuments = new ArrayList<>();
        
        // Create permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), 
                this::handleCameraPermissionResult);
                
        // Create document scan launcher
        documentScanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        GmsDocumentScanningResult scanningResult =
                            GmsDocumentScanningResult.fromActivityResultIntent(data);
                        
                        if (scanningResult != null) {

                            for (GmsDocumentScanningResult.Page page : scanningResult.getPages()) {
                               // Uri imageUri = pages.get(0).getImageUri();
                                page.getImageUri();
                               try{
                                 Bitmap bitmap =  ImageUtils.getBitmapFromUri(getActivity().getBaseContext(),page.getImageUri());
                                 addScannedDocument(bitmap);
                               }catch (Exception e)
                               {
                                   e.printStackTrace();
                               }
                            }
//                        List<Bitmap> bitmaps = scanningResult.getBitmaps();
//                        for (Bitmap bitmap : bitmaps) {
//                            addScannedDocument(bitmap);
//                        }
                        }
                    }
                }
            });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentDocumentScannerBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(DocumentScannerViewModel.class);
        
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        setupObservers();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        documentAdapter = new DocumentAdapter(scannedDocuments, this::onDeleteDocument);
        binding.documentsList.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.documentsList.setAdapter(documentAdapter);
    }

    private void setupButtons() {
        binding.btnScan.setOnClickListener(v -> {
            if (hasCameraPermission()) {
                startDocumentScanning();
            } else {
                requestCameraPermission();
            }
        });

        binding.btnExport.setOnClickListener(v -> {
            if (scannedDocuments.isEmpty()) {
                Toast.makeText(requireContext(), "No documents to export", Toast.LENGTH_SHORT).show();
                return;
            }
            
            showExportOptionsDialog();
        });
    }

    private void setupObservers() {
        // Observe scanned documents if needed
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(), 
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Show rationale dialog
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Camera Permission Required")
                    .setMessage("This app needs camera access to scan documents. Please grant permission to continue.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> 
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void handleCameraPermissionResult(boolean isGranted) {
        if (isGranted) {
            startDocumentScanning();
        } else {
            Toast.makeText(requireContext(), "Camera permission denied. Cannot scan documents.", Toast.LENGTH_LONG).show();
        }
    }

    private void startDocumentScanning() {
        documentScanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener(intentSender -> {
                try {
                    documentScanLauncher.launch(new IntentSenderRequest.Builder(intentSender).build());
                } catch (Exception e) {
                    Toast.makeText(requireContext(), 
                            "Error launching document scanner: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), 
                        "Document scanner launch failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
            });
    }

    private void addScannedDocument(Bitmap bitmap) {
        String timestamp = String.format(Locale.getDefault(), "Page_%d", System.currentTimeMillis());
        DocumentItem document = new DocumentItem(timestamp, bitmap, System.currentTimeMillis());
        scannedDocuments.add(document);
        documentAdapter.notifyItemInserted(scannedDocuments.size() - 1);
        
        // Show preview of the last scanned document
        showPreview(bitmap);
        
        Toast.makeText(requireContext(), "Document added", Toast.LENGTH_SHORT).show();
    }

    private void showPreview(Bitmap bitmap) {
        binding.documentPreview.setImageBitmap(bitmap);
        binding.documentPreview.setVisibility(View.VISIBLE);
        binding.previewPlaceholder.setVisibility(View.GONE);
    }

    private void onDeleteDocument(int position) {
        if (position >= 0 && position < scannedDocuments.size()) {
            scannedDocuments.remove(position);
            documentAdapter.notifyItemRemoved(position);
            
            // Update preview if the deleted document was the last one
            if (scannedDocuments.isEmpty()) {
                binding.documentPreview.setVisibility(View.GONE);
                binding.previewPlaceholder.setVisibility(View.VISIBLE);
            } else if (position == scannedDocuments.size()) {
                // If the last document was deleted, show the new last document
                showPreview(scannedDocuments.get(scannedDocuments.size() - 1).getBitmap());
            }
        }
    }

    private void showExportOptionsDialog() {
        String[] options = {"Export as PDF", "Export as Images", "Share as PDF", "Share as Images"};
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Export Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            exportAsPdf();
                            break;
                        case 1:
                            exportAsImages();
                            break;
                        case 2:
                            shareAsPdf();
                            break;
                        case 3:
                            shareAsImages();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportAsPdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            List<Bitmap> bitmaps = new ArrayList<>();
            for (DocumentItem item : scannedDocuments) {
                bitmaps.add(item.getBitmap());
            }
            
            String fileName = "ScannedDocument_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = PdfUtils.createPdfFromBitmapsWithA4Size(bitmaps, fileName, requireContext());
            
            if (pdfFile != null) {
                Toast.makeText(requireContext(), "PDF exported successfully: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "PDF creation requires Android KitKat (API 19) or higher", Toast.LENGTH_LONG).show();
        }
    }

    private void exportAsImages() {
        // Implementation for image export
        Toast.makeText(requireContext(), "Exporting as Images...", Toast.LENGTH_SHORT).show();
        
        for (int i = 0; i < scannedDocuments.size(); i++) {
            DocumentItem document = scannedDocuments.get(i);
            saveBitmapToFile(document.getBitmap(), "Document_Page_" + (i + 1) + ".jpg");
        }
        
        Toast.makeText(requireContext(), "Images exported successfully", Toast.LENGTH_SHORT).show();
    }

    private void shareAsPdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            List<Bitmap> bitmaps = new ArrayList<>();
            for (DocumentItem item : scannedDocuments) {
                bitmaps.add(item.getBitmap());
            }
            
            String fileName = "SharedDocument_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = PdfUtils.createPdfFromBitmapsWithA4Size(bitmaps, fileName, requireContext());
            
            if (pdfFile != null) {
                Uri uri = FileProvider.getUriForFile(
                        requireContext(),
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        pdfFile
                );
                
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("application/pdf");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                startActivity(Intent.createChooser(shareIntent, "Share document as PDF"));
            } else {
                Toast.makeText(requireContext(), "Failed to create PDF for sharing", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "PDF sharing requires Android KitKat (API 19) or higher", Toast.LENGTH_LONG).show();
        }
    }

    private void shareAsImages() {
        // Implementation for sharing images
        Toast.makeText(requireContext(), "Preparing to share images...", Toast.LENGTH_SHORT).show();
        
        List<Uri> imageUris = new ArrayList<>();
        for (int i = 0; i < scannedDocuments.size(); i++) {
            DocumentItem document = scannedDocuments.get(i);
            String fileName = "Document_Page_" + (i + 1) + ".jpg";
            File file = saveBitmapToFile(document.getBitmap(), fileName);
            if (file != null) {
                Uri uri = FileProvider.getUriForFile(
                        requireContext(),
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        file
                );
                imageUris.add(uri);
            }
        }
        
        if (!imageUris.isEmpty()) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(imageUris));
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share documents"));
        }
    }

    private File saveBitmapToFile(Bitmap bitmap, String fileName) {
        try {
            File documentsDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ScannedDocs");
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }
            
            File file = new File(documentsDir, fileName);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}