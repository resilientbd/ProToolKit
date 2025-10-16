package com.example.protoolkit.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.protoolkit.data.settings.SettingsRepository;
import com.example.protoolkit.ui.base.BaseViewModel;
import com.example.protoolkit.util.AppConstants;
import com.example.protoolkit.util.ThemeUtils;

/**
 * Handles updates originating from SettingsFragment.
 */
public class SettingsViewModel extends BaseViewModel {

    private final SettingsRepository repository;
    private final MutableLiveData<String> themeMode = new MutableLiveData<>("system");
    private final MutableLiveData<Boolean> hapticsEnabled = new MutableLiveData<>(true);
    private final MutableLiveData<Long> adsDisabledUntil = new MutableLiveData<>(0L);

    public SettingsViewModel(@NonNull SettingsRepository repository) {
        this.repository = repository;
        refresh();
    }

    public LiveData<String> getThemeMode() {
        return themeMode;
    }

    public LiveData<Boolean> getHapticsEnabled() {
        return hapticsEnabled;
    }

    public LiveData<Long> getAdsDisabledUntil() {
        return adsDisabledUntil;
    }

    public void refresh() {
        themeMode.setValue(repository.getThemeMode());
        hapticsEnabled.setValue(repository.isHapticsEnabled());
        adsDisabledUntil.setValue(repository.getAdsDisabledUntil());
    }

    public void updateTheme(@NonNull String mode) {
        repository.setThemeMode(mode);
        ThemeUtils.applyTheme(mode);
        themeMode.setValue(mode);
    }

    public void updateHaptics(boolean enabled) {
        repository.setHapticsEnabled(enabled);
        hapticsEnabled.setValue(enabled);
    }

    public void disableAdsTemporarily() {
        repository.setAdsDisabledUntil(System.currentTimeMillis() + AppConstants.ADS_REWARD_DURATION_MS);
        adsDisabledUntil.setValue(repository.getAdsDisabledUntil());
    }
}
