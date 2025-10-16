package com.example.protoolkit;

import android.app.Application;

import androidx.preference.PreferenceManager;

import com.example.protoolkit.ads.AdsManager;
import com.example.protoolkit.data.settings.SettingsRepository;
import com.example.protoolkit.util.ServiceLocator;
import com.example.protoolkit.util.ThemeUtils;

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
        AdsManager.init(this, settingsRepository);
    }
}
