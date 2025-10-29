package com.faisal.protoolkit.ui.tools.document;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;

import com.faisal.protoolkit.BuildConfig;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentDocumentScannerBinding;
import com.faisal.protoolkit.ui.base.BaseFragment;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.data.entities.DocumentEntity;
import com.faisal.protoolkit.data.entities.PageEntity;
import com.faisal.protoolkit.ui.tools.document.adapters.DocumentAdapter;
import com.faisal.protoolkit.ui.tools.document.viewmodels.DocumentsViewModel;
import com.faisal.protoolkit.util.FileManager;
import com.faisal.protoolkit.util.ImageUtils;
import com.faisal.protoolkit.util.ServiceLocator;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Documents List fragment showing all saved documents with ability to scan new ones
 */
public class DocumentScannerFragment extends BaseFragment {

    private FragmentDocumentScannerBinding binding;
    private DocumentsViewModel viewModel;
    private DocumentAdapter documentAdapter;
    private List<DocumentEntity> documents;
    private GmsDocumentScanner documentScanner;
    
    // For creating new document
    private String currentDocumentId;
    private String currentDocumentTitle;
    private FileManager fileManager;
    private AppDatabase database;
    private List<DocumentItem> scannedPages; // Temporary for new document creation

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> documentScanLauncher;

    public DocumentScannerFragment() {
        super(R.layout.fragment_document_scanner);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize database and file manager
        database = AppDatabase.getDatabase(requireContext());
        fileManager = new FileManager(requireContext());
        
        // Initialize the document scanner
        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(true)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                        GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .build();
        documentScanner = GmsDocumentScanning.getClient(options);

        // Initialize documents list
        documents = new ArrayList<>();
        scannedPages = new ArrayList<>();
        
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
                               try {
                                 Bitmap bitmap = ImageUtils.getBitmapFromUri(requireContext(), page.getImageUri());
                                 addScannedPage(bitmap);
                               } catch (Exception e) {
                                   e.printStackTrace();
                                   Toast.makeText(requireContext(), "Error processing scanned page: " + e.getMessage(), 
                                       Toast.LENGTH_LONG).show();
                               }
                            }
                        }
                    }
                }
            });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentDocumentScannerBinding.bind(view);
        viewModel = new ViewModelProvider(this, new DocumentsViewModel.Factory(database)).get(DocumentsViewModel.class);
        
        // Check if we're loading a specific document (e.g., from document detail)
        String documentId = getArguments() != null ? getArguments().getString("document_id") : null;
        if (documentId != null) {
            // Load specific document for editing
            this.currentDocumentId = documentId; // Set the current document ID for editing
            openDocumentForEditing(documentId);
        } else {
            setupToolbar();
            setupRecyclerView();
            setupButtons();
            setupObservers();
            setupFAB();
        }
    }
    
    private void openDocumentForEditing(String documentId) {
        // Load the specific document with its existing pages for editing
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        setupObservers();
        setupFAB();
        
        // Load the document and its pages in the background
        new Thread(() -> {
            DocumentEntity doc = database.documentDao().getDocumentById(documentId);
            if (doc != null) {
                List<PageEntity> pages = database.pageDao().getPagesByDocumentSync(documentId);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Editing: " + doc.title + " (" + pages.size() + " pages)", Toast.LENGTH_LONG).show();
                    
                    // Update the UI to show this document is being edited
                    // This will trigger the observers to update the RecyclerView
                    viewModel.refreshDocuments(); 
                    
                    // Update UI for editing mode if needed
                    binding.btnScan.setText("Add Pages");
                    binding.btnSave.setText("Update Doc");
                });
            }
        }).start();
    }
    
    private void setupFAB() {
        // Create FAB for adding new document
        FloatingActionButton fab = new FloatingActionButton(requireContext());
        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(v -> startNewDocument());
        
        // Add FAB to layout if not already there
        // For now, I'll add it to the layout if needed
    }

    private void setupToolbar() {
        binding.toolbar.setTitle("Documents");
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        documentAdapter = new DocumentAdapter(
            document -> {
                // Navigate to document detail to view/edit pages
                openDocumentDetail(document);
            },
            document -> {
                // Handle document deletion
                showDeleteConfirmationDialog(document);
            }
        );
        binding.documentsList.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.documentsList.setAdapter(documentAdapter);
    }
    
    private void openDocumentDetail(DocumentEntity document) {
        Bundle args = new Bundle();
        args.putString("document_id", document.id);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_documentScannerFragment_to_documentDetailFragment, args);
    }

    private void setupButtons() {
        // Initially set the scan button to create new documents
        binding.btnScan.setText("New Document");
        binding.btnScan.setOnClickListener(v -> startNewDocument());
        
        // Initially set save button to manage documents
        binding.btnSave.setText("Manage");
        binding.btnSave.setOnClickListener(v -> {
            // Show options for managing documents
            showManageOptions();
        });
    }

    private void setupObservers() {
        viewModel.getDocuments().observe(getViewLifecycleOwner(), documentList -> {
            if (documentList != null) {
                documents.clear();
                documents.addAll(documentList);
                documentAdapter.submitList(new ArrayList<>(documents));
            }
        });
    }
    
    private void startNewDocument() {
        // Create a temporary new document and start scanning pages into it
        currentDocumentId = UUID.randomUUID().toString();
        currentDocumentTitle = "Scan " + new Date().toString().replace(":", ".").replace(" ", "_");
        fileManager.createDocumentDir(currentDocumentId);
        scannedPages.clear();
        
        // Show a temporary UI or navigate to scanning mode for this document
        showScanningForNewDocument();
    }
    
    private void showScanningForNewDocument() {
        // For now, show an alert to indicate scanning
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Scan Document")
                .setMessage("Start scanning pages for: " + currentDocumentTitle)
                .setPositiveButton("Start Scanning", (dialog, which) -> {
                    if (hasCameraPermission()) {
                        startDocumentScanning();
                    } else {
                        requestCameraPermission();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    private void addScannedPage(Bitmap bitmap) {
        // Save bitmap to file
        int pageIndex = scannedPages.size();
        File originalFile = fileManager.getOriginalImageFile(currentDocumentId, pageIndex);
        
        // Make sure parent directory exists
        File parentDir = originalFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // Save original bitmap
        try {
            FileOutputStream out = new FileOutputStream(originalFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving scanned page: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        
        // Add to temporary list
        String timestamp = String.format(Locale.getDefault(), "Page_%d", pageIndex + 1);
        DocumentItem documentItem = new DocumentItem(timestamp, bitmap, System.currentTimeMillis());
        scannedPages.add(documentItem);
        
        // Ask if user wants to continue scanning or save
        askToContinueScanning();
    }
    
    private void askToContinueScanning() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Page Added")
                .setMessage("Continue scanning more pages or save this document?")
                .setPositiveButton("Add More Pages", (dialog, which) -> {
                    // Continue scanning
                    if (hasCameraPermission()) {
                        startDocumentScanning();
                    } else {
                        requestCameraPermission();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Ask user if they want to discard changes
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Discard Document?")
                            .setMessage("Do you want to discard this document and its scanned pages?")
                            .setPositiveButton("Discard", (d, w) -> {
                                // Clear temporary data and return to document list
                                scannedPages.clear();
                                currentDocumentId = null;
                                currentDocumentTitle = null;
                                Toast.makeText(requireContext(), "Document discarded", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Keep Scanning", null)
                            .show();
                })
                .setNeutralButton("Save Document", (dialog, which) -> {
                    showSaveDocumentDialog();
                })
                .setCancelable(false)
                .show();
    }

    private void showSaveDocumentDialog() {
        // Create a dialog for user to enter document title
        View dialogView = View.inflate(requireContext(), R.layout.dialog_save_document, null);
        EditText titleEditText = dialogView.findViewById(R.id.edit_text_title);
        
        // Set default title
        titleEditText.setText(currentDocumentTitle);
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Save Document")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(title)) {
                        title = "Scan " + new Date().toString().replace(":", ".").replace(" ", "_");
                    }
                    
                    saveDocument(title);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveDocument(String title) {
        // Show progress
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Saving...");
        
        String docId = currentDocumentId != null ? currentDocumentId : UUID.randomUUID().toString();
        
        // Create or update document in database
        DocumentEntity document = new DocumentEntity(
            docId,
            title,
            null, // folder_id - null for root
            scannedPages.size(),
            0, // cover page index
            "[]", // labels_json
            System.currentTimeMillis(), // created_at
            System.currentTimeMillis(), // updated_at
            "ACTIVE" // status
        );
        
        new Thread(() -> {
            try {
                // Check if document already exists, if so, update it instead of inserting
                DocumentEntity existingDoc = database.documentDao().getDocumentById(docId);
                
                if (existingDoc != null) {
                    // Update existing document
                    database.documentDao().updateDocument(document);
                } else {
                    // Insert new document
                    database.documentDao().insertDocument(document);
                }
                
                // Insert or update pages
                // First, delete existing pages for this document to avoid duplicates
                database.pageDao().deletePagesByDocument(docId);
                
                for (int i = 0; i < scannedPages.size(); i++) {
                    String pageId = UUID.randomUUID().toString();
                    File originalFile = fileManager.getOriginalImageFile(docId, i);
                    
                    PageEntity page = new PageEntity(
                        pageId,
                        docId,
                        i, // index
                        originalFile.getAbsolutePath(), // uri_original
                        null, // uri_render
                        null, // edit_ops_json
                        0, // width - will be populated later
                        0, // height - will be populated later
                        300, // dpi
                        null, // ocr_lang
                        false // ocr_done
                    );
                    
                    database.pageDao().insertPage(page);
                }
                
                // Update document metadata
                database.documentDao().updateDocumentMetadata(
                    docId,
                    scannedPages.size(),
                    0,
                    System.currentTimeMillis()
                );
                
                // Refresh the document list
                requireActivity().runOnUiThread(() -> {
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Manage");
                    Toast.makeText(requireContext(), "Document saved: " + title, Toast.LENGTH_LONG).show();
                    
                    // Force refresh the document list by calling refreshDocuments
                    viewModel.refreshDocuments();
                    
                    // Clear the temporary scanning data
                    scannedPages.clear();
                    currentDocumentId = null;
                    currentDocumentTitle = null;
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Manage");
                    Toast.makeText(requireContext(), "Error saving document: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private void showManageOptions() {
        String[] options = {"Refresh List", "Import Documents", "Settings"};
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Manage Documents")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Refresh
                            viewModel.refreshDocuments();
                            break;
                        case 1: // Import
                            Toast.makeText(requireContext(), "Import feature coming soon", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Settings
                            Toast.makeText(requireContext(), "Settings coming soon", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showDeleteConfirmationDialog(DocumentEntity document) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Document?")
                .setMessage("Are you sure you want to delete \\"" + document.title + "\\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteDocument(document);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void deleteDocument(DocumentEntity document) {
        // Show a progress message
        Toast.makeText(requireContext(), "Deleting document...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                // Delete pages associated with this document first
                database.pageDao().deletePagesByDocument(document.id);
                
                // Delete the document itself
                database.documentDao().deleteDocumentById(document.id);
                
                // Delete the document's folder and all its files
                fileManager.deleteDocumentDir(document.id);
                
                // Update UI on main thread
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Document deleted: " + document.title, Toast.LENGTH_SHORT).show();
                    viewModel.refreshDocuments(); // Refresh the document list
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error deleting document: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // If this fragment was opened for editing a specific document, 
        // override back press behavior to go directly to document list instead of back to document detail
        if (currentDocumentId != null && getParentFragmentManager() != null) {
            // We can't override onBackPress directly in fragment, 
            // so the back behavior will follow normal navigation
        }
    }
}
