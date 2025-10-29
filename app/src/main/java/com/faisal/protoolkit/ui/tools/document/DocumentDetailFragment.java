package com.faisal.protoolkit.ui.tools.document;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faisal.protoolkit.BuildConfig;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentDocumentDetailBinding;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.PageEntity;
import com.faisal.protoolkit.ui.tools.document.adapters.PageAdapter;
import com.faisal.protoolkit.ui.tools.document.DocumentPageEditActivity;
import com.faisal.protoolkit.ui.tools.document.viewmodels.DocumentDetailViewModel;
import com.faisal.protoolkit.util.FileManager;
import com.faisal.protoolkit.util.ImageUtils;
import com.faisal.protoolkit.util.PdfExportUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentDetailFragment extends Fragment {
    private FragmentDocumentDetailBinding binding;
    private DocumentDetailViewModel viewModel;
    private PageAdapter adapter;
    private String documentId;
    private AppDatabase database;
    private FileManager fileManager;
    private GmsDocumentScanner documentScanner;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> documentScanLauncher;

    public static DocumentDetailFragment newInstance(String documentId) {
        DocumentDetailFragment fragment = new DocumentDetailFragment();
        Bundle args = new Bundle();
        args.putString("document_id", documentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = AppDatabase.getDatabase(requireContext());
        fileManager = new FileManager(requireContext());

        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(true)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                        GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .build();
        documentScanner = GmsDocumentScanning.getClient(options);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                this::handleCameraPermissionResult
        );

        documentScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        GmsDocumentScanningResult scanningResult =
                                GmsDocumentScanningResult.fromActivityResultIntent(result.getData());
                        if (scanningResult != null) {
                            handleScanResult(scanningResult);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDocumentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        documentId = getArguments() != null ? getArguments().getString("document_id") : null;
        if (documentId == null) {
            // Handle error - documentId is required
            Toast.makeText(requireContext(), "Document ID is required", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }
        
        viewModel = new ViewModelProvider(this, new DocumentDetailViewModel.Factory(database))
                .get(DocumentDetailViewModel.class);
        viewModel.setDocumentId(documentId);
        
        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupButtons();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        
        // Set document title
        viewModel.getDocument().observe(getViewLifecycleOwner(), document -> {
            if (document != null) {
                binding.toolbar.setTitle(document.title);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new PageAdapter(viewModel, 
            page -> {
                // Start the full-screen edit activity for this page
                android.content.Intent intent = new android.content.Intent(requireContext(), 
                    com.faisal.protoolkit.ui.tools.document.DocumentPageEditActivity.class);
                intent.putExtra("page_id", page.id);
                startActivity(intent);
            },
            page -> {
                // Delete this page
                showDeletePageConfirmationDialog(page);
            });
        
        binding.pagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        binding.pagesRecyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getPages().observe(getViewLifecycleOwner(), pages -> {
            adapter.submitList(pages);
        });
    }
    
    private void showDeletePageConfirmationDialog(PageEntity page) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Page?")
                .setMessage("Are you sure you want to delete this page? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deletePage(page);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportToPdf() {
        if (documentId == null) {
            Toast.makeText(requireContext(), "Document not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show a progress message
        Toast.makeText(requireContext(), "Preparing PDF...", Toast.LENGTH_SHORT).show();

        // Run the export in a background thread
        new Thread(() -> {
            try {
                // Get all pages for the document
                List<PageEntity> pages = database.pageDao().getPagesByDocumentSync(documentId);
                
                if (pages == null || pages.isEmpty()) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "No pages to export", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Sort pages by index to ensure correct order
                pages.sort((p1, p2) -> Integer.compare(p1.index, p2.index));

                // Create a list to hold all bitmaps
                List<Bitmap> bitmaps = new ArrayList<>();
                
                // Load bitmap for each page
                for (PageEntity page : pages) {
                    try {
                        Bitmap bitmap = null;
                        if (page.uri_original != null) {
                            if (page.uri_original.startsWith("content://") || page.uri_original.startsWith("file://")) {
                                // It's already a URI, use it directly
                                Uri imageUri = Uri.parse(page.uri_original);
                                bitmap = ImageUtils.getBitmapFromUri(requireContext(), imageUri);
                            } else {
                                // It's a file path, check if it exists and load it directly
                                java.io.File imageFile = new java.io.File(page.uri_original);
                                if (imageFile.exists()) {
                                    bitmap = BitmapFactory.decodeFile(page.uri_original);
                                } else {
                                    System.out.println("File does not exist: " + page.uri_original);
                                }
                            }
                        }
                        
                        if (bitmap != null) {
                            bitmaps.add(bitmap);
                        } else {
                            // Log for debugging
                            System.out.println("Could not load bitmap for: " + page.uri_original);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Continue with other pages even if one fails
                    }
                }

                if (bitmaps.isEmpty()) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "No valid images to export", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Create the PDF file
                String fileName = "Document_" + documentId + ".pdf";
                File pdfFile = PdfExportUtil.createPdfFromBitmapsWithA4Size(bitmaps, fileName, requireContext());

                // Clean up bitmaps to free memory
                for (Bitmap bitmap : bitmaps) {
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }

                if (pdfFile != null && pdfFile.exists()) {
                    // Share the PDF file
                    sharePdfFile(pdfFile);
                } else {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sharePdfFile(File pdfFile) {
        requireActivity().runOnUiThread(() -> {
            try {
                // Create a content URI for the PDF file using FileProvider
                Uri pdfUri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    pdfFile
                );

                // Create the share intent
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("application/pdf");

                // Create a chooser intent to let the user choose an app to share with
                Intent chooserIntent = Intent.createChooser(shareIntent, "Share PDF");

                // Grant temporary read permission to the receiving app
                List<android.content.pm.ResolveInfo> resInfoList = requireContext().getPackageManager()
                    .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (android.content.pm.ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    requireContext().grantUriPermission(
                        packageName,
                        pdfUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                }

                // Start the activity to share the PDF
                startActivity(chooserIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error sharing PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        binding.fabAddPage.setOnClickListener(v -> {
            // Add a new page to the document
            startEditingDocument();
        });
        

        
        binding.btnExport.setOnClickListener(v -> {
            exportToPdf();
        });
    }

    private void editDocument() {
        // Navigate to DocumentScannerFragment to edit this document
        Bundle args = new Bundle();
        args.putString("document_id", documentId);
        androidx.navigation.fragment.NavHostFragment.findNavController(this)
                .navigate(R.id.action_documentDetailFragment_to_documentScannerFragment, args);
    }

    private void startEditingDocument() {
        if (documentId == null) {
            Toast.makeText(requireContext(), "Unable to edit: document not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasCameraPermission()) {
            startDocumentScanning();
        } else {
            requestCameraPermission();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
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
        if (documentScanner == null) {
            Toast.makeText(requireContext(), "Document scanner is not available", Toast.LENGTH_SHORT).show();
            return;
        }

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
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Document scanner launch failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void handleScanResult(GmsDocumentScanningResult scanningResult) {
        if (documentId == null) {
            return;
        }

        final List<GmsDocumentScanningResult.Page> pages = scanningResult.getPages();
        if (pages == null || pages.isEmpty()) {
            Toast.makeText(requireContext(), "No pages captured", Toast.LENGTH_SHORT).show();
            return;
        }

        final android.content.Context appContext = requireContext().getApplicationContext();

        new Thread(() -> {
            int addedCount = 0;

            try {
                fileManager.createDocumentDir(documentId);
                Integer maxIndex = database.pageDao().getMaxPageIndex(documentId);
                int baseIndex = maxIndex != null ? maxIndex + 1 : 0;

                for (int i = 0; i < pages.size(); i++) {
                    GmsDocumentScanningResult.Page page = pages.get(i);
                    try {
                        Bitmap bitmap = ImageUtils.getBitmapFromUri(appContext, page.getImageUri());
                        if (bitmap == null) {
                            continue;
                        }

                        int targetIndex = baseIndex + addedCount;
                        File originalFile = fileManager.getOriginalImageFile(documentId, targetIndex);
                        File parentDir = originalFile.getParentFile();
                        if (parentDir != null && !parentDir.exists()) {
                            parentDir.mkdirs();
                        }

                        try (FileOutputStream out = new FileOutputStream(originalFile)) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                        }

                        PageEntity pageEntity = new PageEntity(
                                UUID.randomUUID().toString(),
                                documentId,
                                targetIndex,
                                originalFile.getAbsolutePath(),
                                null,
                                null,
                                bitmap.getWidth(),
                                bitmap.getHeight(),
                                300,
                                null,
                                false
                        );

                        database.pageDao().insertPage(pageEntity);
                        addedCount++;

                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (addedCount > 0) {
                    int pageCount = database.pageDao().getPagesByDocumentSync(documentId).size();
                    database.documentDao().updateDocumentMetadata(
                            documentId,
                            pageCount,
                            0,
                            System.currentTimeMillis()
                    );

                    requireActivity().runOnUiThread(() -> {
//                        Toast.makeText(requireContext(),
//                                "Added " + addedCount + " page(s)",
//                                Toast.LENGTH_SHORT).show();
                        viewModel.refresh();
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "No new pages were added",
                                    Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Error adding pages: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
