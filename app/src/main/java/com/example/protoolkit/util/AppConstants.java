package com.example.protoolkit.util;

/**
 * Central location for IDs and configuration values used across the app.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class.
    }

    public static final String TOOL_ID_UNIT_CONVERTER = "tool_unit_converter";
    public static final String TOOL_ID_TEXT_TOOLS = "tool_text_tools";
    public static final String TOOL_ID_DEVICE_INFO = "tool_device_info";
    public static final String TOOL_ID_QR_SCANNER = "tool_qr_scanner";
    public static final String TOOL_ID_FILE_TOOLS = "tool_file_tools";
    public static final String TOOL_ID_NETWORK_TOOLS = "tool_network_tools";

    public static final String PREF_THEME_MODE = "pref_theme_mode";
    public static final String PREF_HAPTICS_ENABLED = "pref_haptics_enabled";
    public static final String PREF_REWARDED_AD = "pref_rewarded_ads";
    public static final String PREF_LANGUAGE = "pref_language";
    public static final String PREF_ADS_DISABLED_UNTIL = "pref_ads_disabled_until";

    public static final long INTERSTITIAL_COOLDOWN_MS = 120_000L;
    public static final long ADS_REWARD_DURATION_MS = 60 * 60 * 1000L;

    public static final String DEFAULT_PING_TARGET = "https://www.google.com";
    public static final int NETWORK_TIMEOUT_MS = 7_000;

    public static final String KEY_DEVICE_INFO = "device_info";
    public static final String KEY_LATENCY = "latency";
}
