package com.faisal.protoolkit;

import android.app.Application;

import androidx.preference.PreferenceManager;

import com.faisal.protoolkit.ads.AdsManager;
import com.faisal.protoolkit.data.settings.SettingsRepository;
import com.faisal.protoolkit.util.ServiceLocator;
import com.faisal.protoolkit.util.ThemeUtils;

/**
 * Custom Application for global initialization.
 */
public class ProToolkitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceLocator.initialize(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_settings, false);
        SettingsRepository settingsRepository = ServiceLocator.getSettingsRepository();
        ThemeUtils.applyTheme(settingsRepository.getThemeMode());
        // Initialize AdsManager with context
        AdsManager.getInstance(this);
    }
}
