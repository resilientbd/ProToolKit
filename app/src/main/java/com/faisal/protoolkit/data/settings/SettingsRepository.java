package com.faisal.protoolkit.data.settings;

import android.app.Application;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.faisal.protoolkit.util.AppConstants;

import java.util.Date;

/**
 * Wraps SharedPreferences for settings access.
 */
public class SettingsRepository {

    private final Application application;
    private final SharedPreferences preferences;
    
    public Application getApplication() {
        return application;
    }

    public SettingsRepository(@NonNull Application application) {
        this.application = application;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(application);
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public String getThemeMode() {
        return preferences.getString(AppConstants.PREF_THEME_MODE, "system");
    }

    public void setThemeMode(@NonNull String mode) {
        preferences.edit().putString(AppConstants.PREF_THEME_MODE, mode).apply();
    }

    public boolean isHapticsEnabled() {
        return preferences.getBoolean(AppConstants.PREF_HAPTICS_ENABLED, true);
    }

    public void setHapticsEnabled(boolean enabled) {
        preferences.edit().putBoolean(AppConstants.PREF_HAPTICS_ENABLED, enabled).apply();
    }

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public long getAdsDisabledUntil() {
        return preferences.getLong(AppConstants.PREF_ADS_DISABLED_UNTIL, 0L);
    }

    public void setAdsDisabledUntil(long timestamp) {
        preferences.edit().putLong(AppConstants.PREF_ADS_DISABLED_UNTIL, timestamp).apply();
    }

    public boolean shouldShowAds() {
        return System.currentTimeMillis() >= getAdsDisabledUntil();
    }

    public String getReadableAdsResumeTime() {
        long until = getAdsDisabledUntil();
        if (until <= System.currentTimeMillis()) {
            return "";
        }
        Date date = new Date(until);
        return DateFormat.getMediumDateFormat(application).format(date) + " " + DateFormat.getTimeFormat(application).format(date);
    }
}