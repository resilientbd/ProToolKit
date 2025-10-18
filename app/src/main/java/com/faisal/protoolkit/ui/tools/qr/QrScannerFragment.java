package com.faisal.protoolkit.ui.tools.qr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.ImageAnalysis;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentQrScannerBinding;
import com.faisal.protoolkit.ui.base.BaseFragment;
import com.faisal.protoolkit.util.HapticHelper;
import com.faisal.protoolkit.util.PermissionHelper;
import com.faisal.protoolkit.util.ServiceLocator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrScannerFragment extends BaseFragment {

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    private FragmentQrScannerBinding binding;
    private QrScannerViewModel viewModel;
    private ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> providerFuture;
    private boolean previewBound;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onPermissionResult);

    public QrScannerFragment() {
        super(R.layout.fragment_qr_scanner);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentQrScannerBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(QrScannerViewModel.class);
        
        // Initialize barcode scanner
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_128, 
                                  Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93,
                                  Barcode.FORMAT_CODABAR, Barcode.FORMAT_DATA_MATRIX,
                                  Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                                  Barcode.FORMAT_ITF, Barcode.FORMAT_PDF417,
                                  Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
        
        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();
        
        binding.buttonRequestPermission.setOnClickListener(v -> requestCameraPermission());
        binding.buttonStartScan.setOnClickListener(v -> {
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
            viewModel.startScanning();
        });
        binding.buttonOpenSettings.setOnClickListener(v -> PermissionHelper.openAppSettings(requireContext()));
        // Hide the stop scan button since scanning stops automatically after successful detection
        binding.buttonStopScan.setVisibility(View.GONE);

        observe(viewModel.isCameraPermissionGranted(), granted -> {
            binding.permissionStatus.setText(granted ? R.string.label_permission_camera : R.string.label_permission_rationale_scanner);
            binding.buttonStartScan.setEnabled(Boolean.TRUE.equals(granted));
            updatePreviewVisibility();
        });
        observe(viewModel.isScanningActive(), active -> {
            binding.scanStatus.setText(Boolean.TRUE.equals(active) ? R.string.qr_scanner_status_active : R.string.qr_scanner_status_inactive);
            updatePreviewVisibility();
            if (Boolean.TRUE.equals(active)) {
                ensureCameraPreview();
            } else {
                stopCameraPreview();
            }
        });
        observe(viewModel.getStatusMessageRes(), messageRes -> {
            if (messageRes != null) {
                binding.statusMessage.setText(getString(messageRes));
            }
        });
        observe(viewModel.getScannedResult(), result -> {
            if (result != null && !result.isEmpty()) {
                // Show the scanned result and action options
                showScanResult(result);
            }
        });

        boolean granted = ContextCompat.checkSelfPermission(requireContext(), CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED;
        viewModel.updatePermission(granted);
        updatePreviewVisibility();
        if (Boolean.TRUE.equals(viewModel.isScanningActive().getValue()) && granted) {
            ensureCameraPreview();
        } else {
            stopCameraPreview();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean granted = ContextCompat.checkSelfPermission(requireContext(), CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED;
        viewModel.updatePermission(granted);
        updatePreviewVisibility();
        if (Boolean.TRUE.equals(viewModel.isScanningActive().getValue()) && granted) {
            ensureCameraPreview();
        } else {
            stopCameraPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCameraPreview(false);
    }

    private void showScanResult(String result) {
        // Show the result in a dialog with appropriate action options
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("QR Code Scanned");
        
        // Determine QR type and show appropriate message
        com.faisal.protoolkit.util.QrCodeUtils.QrType qrType =
            com.faisal.protoolkit.util.QrCodeUtils.getQrCodeType(result);
        
        String typeText = getQrTypeDisplayText(qrType);
        String message;
        
        // For WiFi QR codes, show parsed information
        if (qrType == com.faisal.protoolkit.util.QrCodeUtils.QrType.WIFI) {
            String ssid = extractWifiValue(result, "S:");
            String password = extractWifiValue(result, "P:");
            String security = extractWifiValue(result, "T:");
            message = "Type: " + typeText + 
                     "\nNetwork: " + ssid + 
                     "\nSecurity: " + security + 
                     "\nPassword: " + (password.isEmpty() ? "[No password]" : "••••••••") +
                     "\n\nRaw Content: " + result;
            
            // For WiFi QR codes, copy credentials immediately when dialog shows
            copyWifiCredentialsToClipboard(result);
        } else {
            message = "Type: " + typeText + "\n\nContent: " + result;
        }
        
        builder.setMessage(message);
        
        // Set action button based on QR type
        String actionText = getActionTextForType(qrType);
        builder.setPositiveButton(actionText, (dialog, which) -> {
            // Perform the appropriate action based on QR type
            com.faisal.protoolkit.util.QrCodeUtils.handleQrResult(requireContext(), result);
        });
        
        // For WiFi, provide a secondary option to copy again if needed
        if (qrType != com.faisal.protoolkit.util.QrCodeUtils.QrType.WIFI) {
            builder.setNegativeButton("Copy Text", (dialog, which) -> {
                // Copy the text to clipboard
                try {
                    android.content.ClipboardManager clipboard = 
                        (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("QR Code Content", result);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Handle clipboard access error gracefully
                    Toast.makeText(requireContext(), "Failed to copy - permission issue", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // For WiFi, provide "Copy Again" option
            builder.setNegativeButton("Copy Again", (dialog, which) -> {
                copyWifiCredentialsToClipboard(result);
            });
        }
        
        builder.setNeutralButton("Cancel", (dialog, which) -> {
            // Dismiss dialog and reset scan if needed
            viewModel.resetScan();
        });
        
        builder.show();
    }
    
    private String extractWifiValue(String wifiString, String key) {
        int startIndex = wifiString.indexOf(key);
        if (startIndex == -1) return "";
        
        startIndex += key.length();
        int endIndex = wifiString.indexOf(";", startIndex);
        if (endIndex == -1) return wifiString.substring(startIndex);
        
        return wifiString.substring(startIndex, endIndex);
    }
    
    private void copyWifiCredentialsToClipboard(String wifiString) {
        try {
            String ssid = extractWifiValue(wifiString, "S:");
            String password = extractWifiValue(wifiString, "P:");
            
            android.content.ClipboardManager clipboard = 
                (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            // Copy just the password for easy pasting in WiFi password field
            android.content.ClipData clip = android.content.ClipData.newPlainText("WiFi Password", password);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(requireContext(), "WiFi password copied to clipboard", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Silently handle clipboard access issues
        }
    }
    
    private String getQrTypeDisplayText(com.faisal.protoolkit.util.QrCodeUtils.QrType type) {
        switch (type) {
            case URL:
                return "Website URL";
            case TEXT:
                return "Text";
            case WIFI:
                return "WiFi Credentials";
            case CONTACT:
                return "Contact Info";
            case EMAIL:
                return "Email";
            case SMS:
                return "SMS";
            case CALENDAR:
                return "Calendar Event";
            case GEO:
                return "Geolocation";
            default:
                return "Other";
        }
    }
    
    private String getActionTextForType(com.faisal.protoolkit.util.QrCodeUtils.QrType type) {
        switch (type) {
            case URL:
                return "Open Website";
            case TEXT:
                return "Copy Text";
            case WIFI:
                return "Connect to WiFi";
            case CONTACT:
                return "Add Contact";
            case EMAIL:
                return "Send Email";
            case SMS:
                return "Send Message";
            case CALENDAR:
                return "Add to Calendar";
            case GEO:
                return "Open Map";
            default:
                return "Handle";
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCameraPreview();
        if (providerFuture != null) {
            providerFuture.cancel(true);
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (barcodeScanner != null) {
            try {
                barcodeScanner.close();
            } catch (Exception e) {
                Log.e("QrScannerFragment", "Error closing barcode scanner", e);
            }
        }
        cameraProvider = null;
        binding = null;
    }

    private void updatePreviewVisibility() {
        if (binding == null) {
            return;
        }
        boolean granted = Boolean.TRUE.equals(viewModel.isCameraPermissionGranted().getValue());
        boolean active = Boolean.TRUE.equals(viewModel.isScanningActive().getValue());
        if (!granted) {
            stopCameraPreview(true);
            binding.previewPlaceholderTitle.setText(R.string.qr_scanner_preview_title_permission);
            binding.previewPlaceholderDescription.setText(R.string.qr_scanner_preview_permission_hint);
        } else if (!active) {
            stopCameraPreview(true);
            binding.previewPlaceholderTitle.setText(R.string.qr_scanner_preview_title_ready);
            binding.previewPlaceholderDescription.setText(R.string.qr_scanner_preview_ready_hint);
        } else if (previewBound) {
            binding.previewPlaceholderOverlay.setVisibility(View.GONE);
        }
    }

    private void ensureCameraPreview() {
        if (binding == null) {
            return;
        }
        if (previewBound) {
            binding.previewPlaceholderOverlay.setVisibility(View.GONE);
            return;
        }
        if (cameraProvider != null) {
            bindPreview(cameraProvider);
            return;
        }
        if (providerFuture == null) {
            providerFuture = ProcessCameraProvider.getInstance(requireContext());
        }
        binding.previewPlaceholderOverlay.setVisibility(View.VISIBLE);
        binding.previewPlaceholderTitle.setText(R.string.qr_scanner_preview_title_ready);
        binding.previewPlaceholderDescription.setText(R.string.qr_scanner_preview_initializing);
        providerFuture.addListener(() -> {
            try {
                cameraProvider = providerFuture.get();
                providerFuture = null;
                bindPreview(cameraProvider);
            } catch (Exception exception) {
                binding.previewPlaceholderOverlay.setVisibility(View.VISIBLE);
                binding.previewPlaceholderDescription.setText(R.string.qr_scanner_preview_error);
                providerFuture = null;
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider provider) {
        if (binding == null) {
            return;
        }
        try {
            provider.unbindAll();
            
            // Create preview
            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
            
            // Create image analysis for barcode scanning
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();
            
            imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                @SuppressWarnings("UnsafeOptInUsageError")
                InputImage image = InputImage.fromMediaImage(
                        imageProxy.getImage(), 
                        imageProxy.getImageInfo().getRotationDegrees()
                );
                
                barcodeScanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();
                                if (rawValue != null) {
                                    // Stop scanning after successful detection
                                    viewModel.handleScanResult(rawValue);
                                    // Stop camera after successful scan
                                    stopCameraPreview();
                                    break; // exit the loop after first successful scan
                                }
                            }
                        })
                        .addOnFailureListener(exception -> {
                            Log.e("QrScannerFragment", "Error processing image for barcode scanning", exception);
                        })
                        .addOnCompleteListener(task -> {
                            // Close the image proxy to free up resources
                            imageProxy.close();
                        });
            });
            
            CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
            
            // Bind preview and image analysis to lifecycle
            provider.bindToLifecycle(
                    getViewLifecycleOwner(), 
                    selector, 
                    preview, 
                    imageAnalysis
            );
            
            previewBound = true;
            binding.previewPlaceholderOverlay.setVisibility(View.GONE);
            binding.previewPlaceholderTitle.setText(R.string.qr_scanner_preview_title);
        } catch (Exception exception) {
            Log.e("QrScannerFragment", "Error binding camera use cases", exception);
            binding.previewPlaceholderOverlay.setVisibility(View.VISIBLE);
            binding.previewPlaceholderDescription.setText(R.string.qr_scanner_preview_error);
            previewBound = false;
        }
    }

    private void stopCameraPreview() {
        stopCameraPreview(true);
    }

    private void stopCameraPreview(boolean showOverlay) {
        if (binding == null) {
            return;
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        previewBound = false;
        if (showOverlay) {
            binding.previewPlaceholderOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.updatePermission(true);
            Toast.makeText(requireContext(), R.string.permission_already_granted, Toast.LENGTH_SHORT).show();
            return;
        }
        if (shouldShowRequestPermissionRationale(CAMERA_PERMISSION)) {
            Toast.makeText(requireContext(), R.string.label_permission_rationale_scanner, Toast.LENGTH_LONG).show();
        }
        permissionLauncher.launch(CAMERA_PERMISSION);
    }

    private void onPermissionResult(boolean granted) {
        viewModel.updatePermission(granted);
        if (!granted) {
            Toast.makeText(requireContext(), R.string.label_permission_rationale_scanner, Toast.LENGTH_SHORT).show();
        }
    }
}