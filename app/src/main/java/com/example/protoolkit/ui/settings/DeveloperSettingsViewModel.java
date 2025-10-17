package com.example.protoolkit.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.protoolkit.data.settings.SettingsRepository;
import com.example.protoolkit.ui.base.BaseViewModel;
import com.example.protoolkit.util.AppExecutors;
import com.example.protoolkit.util.DeveloperModeUtil;

/**
 * ViewModel for developer settings.
 */
public class DeveloperSettingsViewModel extends BaseViewModel {

    private final SettingsRepository settingsRepository;
    private final DeveloperModeUtil developerModeUtil;
    
    private final MutableLiveData<Boolean> developerModeEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> sandboxMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> debugLoggingEnabled = new MutableLiveData<>(false);

    public DeveloperSettingsViewModel(@NonNull SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
        this.developerModeUtil = new DeveloperModeUtil(settingsRepository.getApplication());
        loadSettings();
    }

    private void loadSettings() {
        developerModeEnabled.postValue(developerModeUtil.isDeveloperModeEnabled());
        sandboxMode.postValue(developerModeUtil.isSandboxMode());
        debugLoggingEnabled.postValue(developerModeUtil.isDebugLoggingEnabled());
    }

    public void setDeveloperModeEnabled(boolean enabled) {
        AppExecutors.io().execute(() -> {
            developerModeUtil.setDeveloperModeEnabled(enabled);
            developerModeEnabled.postValue(enabled);
        });
    }

    public void setSandboxMode(boolean enabled) {
        AppExecutors.io().execute(() -> {
            developerModeUtil.setSandboxMode(enabled);
            sandboxMode.postValue(enabled);
        });
    }

    public void setDebugLoggingEnabled(boolean enabled) {
        AppExecutors.io().execute(() -> {
            developerModeUtil.setDebugLoggingEnabled(enabled);
            debugLoggingEnabled.postValue(enabled);
        });
    }

    public void testPurchaseFlow() {
        // This would trigger the test purchase flow
        postError("Test purchase flow triggered");
    }

    public void resetPurchaseHistory() {
        // This would clear cached purchase information
        postError("Purchase history reset");
    }

    // Getters
    public LiveData<Boolean> isDeveloperModeEnabled() { return developerModeEnabled; }
    public LiveData<Boolean> isSandboxMode() { return sandboxMode; }
    public LiveData<Boolean> isDebugLoggingEnabled() { return debugLoggingEnabled; }
}