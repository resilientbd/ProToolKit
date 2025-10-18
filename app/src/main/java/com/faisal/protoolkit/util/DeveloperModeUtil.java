package com.faisal.protoolkit.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Utility class for managing developer mode and sandbox environments.
 */
public class DeveloperModeUtil {

    private static final String PREF_DEV_MODE_ENABLED = "pref_dev_mode_enabled";
    private static final String PREF_SANDBOX_MODE = "pref_sandbox_mode";
    private static final String PREF_DEBUG_LOGGING = "pref_debug_logging";
    
    private final SharedPreferences prefs;
    
    public DeveloperModeUtil(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    /**
     * Enable or disable developer mode.
     */
    public void setDeveloperModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_DEV_MODE_ENABLED, enabled).apply();
    }
    
    /**
     * Check if developer mode is enabled.
     */
    public boolean isDeveloperModeEnabled() {
        return prefs.getBoolean(PREF_DEV_MODE_ENABLED, false);
    }
    
    /**
     * Enable or disable sandbox mode for testing.
     */
    public void setSandboxMode(boolean enabled) {
        prefs.edit().putBoolean(PREF_SANDBOX_MODE, enabled).apply();
    }
    
    /**
     * Check if sandbox mode is enabled.
     */
    public boolean isSandboxMode() {
        return prefs.getBoolean(PREF_SANDBOX_MODE, false);
    }
    
    /**
     * Enable or disable debug logging.
     */
    public void setDebugLoggingEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_DEBUG_LOGGING, enabled).apply();
    }
    
    /**
     * Check if debug logging is enabled.
     */
    public boolean isDebugLoggingEnabled() {
        return prefs.getBoolean(PREF_DEBUG_LOGGING, false) || isDeveloperModeEnabled();
    }
    
    /**
     * Get the current environment (production or sandbox).
     */
    public Environment getEnvironment() {
        return isSandboxMode() ? Environment.SANDBOX : Environment.PRODUCTION;
    }
    
    /**
     * Environment types.
     */
    public enum Environment {
        PRODUCTION,
        SANDBOX
    }
    
    /**
     * Get billing configuration based on current environment.
     */
    public BillingConfig getBillingConfig() {
        if (isSandboxMode()) {
            return new BillingConfig(
                "android.test.purchased",
                "android.test.canceled",
                "android.test.item_unavailable"
            );
        } else {
            return new BillingConfig(
                BillingManager.PRODUCT_PREMIUM,
                BillingManager.PRODUCT_REMOVE_ADS,
                BillingManager.PRODUCT_UNLOCK_ALL
            );
        }
    }
    
    /**
     * Configuration class for billing.
     */
    public static class BillingConfig {
        public final String premiumProductId;
        public final String removeAdsProductId;
        public final String unlockAllProductId;
        
        public BillingConfig(String premium, String removeAds, String unlockAll) {
            this.premiumProductId = premium;
            this.removeAdsProductId = removeAds;
            this.unlockAllProductId = unlockAll;
        }
    }
}