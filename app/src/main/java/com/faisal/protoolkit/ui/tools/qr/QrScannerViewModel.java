package com.faisal.protoolkit.ui.tools.qr;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.faisal.protoolkit.data.settings.SettingsRepository;
import com.faisal.protoolkit.ui.base.BaseViewModel;
import com.faisal.protoolkit.util.QrCodeUtils;

/***
 * ViewModel for the QR & Barcode scanner tool.
 */
public class QrScannerViewModel extends BaseViewModel {

    private final SettingsRepository settingsRepository;
    private final MutableLiveData<Boolean> cameraPermissionGranted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> scanningActive = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> statusMessageRes = new MutableLiveData<>(com.faisal.protoolkit.R.string.qr_scanner_status_idle);
    private final MutableLiveData<String> scannedResult = new MutableLiveData<>();
    private final MutableLiveData<QrCodeUtils.QrType> qrType = new MutableLiveData<>();
    private final MutableLiveData<String> qrContent = new MutableLiveData<>();

    public QrScannerViewModel(@NonNull SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public LiveData<Boolean> isCameraPermissionGranted() {
        return cameraPermissionGranted;
    }

    public LiveData<Boolean> isScanningActive() {
        return scanningActive;
    }

    public LiveData<Integer> getStatusMessageRes() {
        return statusMessageRes;
    }

    public LiveData<String> getScannedResult() {
        return scannedResult;
    }

    public LiveData<QrCodeUtils.QrType> getQrType() {
        return qrType;
    }

    public LiveData<String> getQrContent() {
        return qrContent;
    }

    public boolean isHapticsEnabled() {
        return settingsRepository.isHapticsEnabled();
    }

    public void updatePermission(boolean granted) {
        cameraPermissionGranted.setValue(granted);
        if (!granted) {
            scanningActive.setValue(false);
            statusMessageRes.setValue(com.faisal.protoolkit.R.string.qr_scanner_permission_required);
        } else {
            statusMessageRes.setValue(com.faisal.protoolkit.R.string.qr_scanner_ready_message);
        }
    }

    public void startScanning() {
        if (!Boolean.TRUE.equals(cameraPermissionGranted.getValue())) {
            statusMessageRes.setValue(com.faisal.protoolkit.R.string.qr_scanner_permission_required);
            return;
        }
        scanningActive.setValue(true);
        statusMessageRes.setValue(com.faisal.protoolkit.R.string.qr_scanner_placeholder_active);
    }

    public void stopScanning() {
        scanningActive.setValue(false);
        statusMessageRes.setValue(com.faisal.protoolkit.R.string.qr_scanner_paused_message);
    }

    public void handleScanResult(String result) {
        Log.d("QrScannerViewModel", "Scan result: " + result);
        scannedResult.setValue(result);
        qrType.setValue(QrCodeUtils.getQrCodeType(result));
        qrContent.setValue(result);
        statusMessageRes.setValue(com.faisal.protoolkit.R.string.qr_scanner_status_idle);
        
        // Stop scanning after successful scan
        scanningActive.setValue(false);
    }

    public void resetScan() {
        scannedResult.setValue(null);
        qrType.setValue(null);
        qrContent.setValue(null);
    }

    public String getActionTextForType() {
        QrCodeUtils.QrType type = qrType.getValue();
        if (type == null) return "Action";

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
}
