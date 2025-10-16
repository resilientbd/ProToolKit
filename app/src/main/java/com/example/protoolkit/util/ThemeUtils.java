package com.example.protoolkit.util;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Applies theme modes across the app.
 */
public final class ThemeUtils {

    private ThemeUtils() {
    }

    public static void applyTheme(@NonNull String mode) {
        int nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        switch (mode) {
            case "light":
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case "system":
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
