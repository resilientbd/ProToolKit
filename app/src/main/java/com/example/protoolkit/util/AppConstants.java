package com.example.protoolkit.util;

/**
 * Defines shared constants used throughout the application.
 */
public class AppConstants {

    // Network constants
    public static final int NETWORK_TIMEOUT_MS = 10000;
    public static final String DEFAULT_PING_TARGET = "google.com";
    public static final String DEFAULT_SPEED_TEST_URL = "https://httpbin.org/bytes/1048576"; // 1MB test file
    
    // File tools constants
    public static final long CACHE_SIZE_THRESHOLD = 100 * 1024 * 1024; // 100MB
    public static final long DOWNLOADS_SIZE_THRESHOLD = 50 * 1024 * 1024; // 50MB
    public static final long MEDIA_SIZE_THRESHOLD = 1000 * 1024 * 1024; // 1GB
    public static final long LARGE_FILE_THRESHOLD = 10 * 1024 * 1024; // 10MB
    
    // Device info constants
    public static final String UNKNOWN_VALUE = "Unknown";
    
    // UI constants
    public static final int PADDING_SCREEN = 16;
    public static final int SPACING_LARGE = 24;
    public static final int SPACING_MEDIUM = 16;
    public static final int SPACING_SMALL = 8;
    
    // Tool IDs
    public static final String TOOL_ID_QR_SCANNER = "tool_qr_scanner";
    public static final String TOOL_ID_DEVICE_INFO = "tool_device_info";
    public static final String TOOL_ID_FILE_TOOLS = "tool_file_tools";
    public static final String TOOL_ID_NETWORK_TOOLS = "tool_network_tools";
    public static final String TOOL_ID_TEXT_TOOLS = "tool_text_tools";
    
    // Permission constants
    public static final String PERMISSION_CAMERA = "android.permission.CAMERA";
    public static final String PERMISSION_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String PERMISSION_WIFI = "android.permission.ACCESS_WIFI_STATE";
    public static final String PERMISSION_NETWORK = "android.permission.ACCESS_NETWORK_STATE";
    
    // Error messages
    public static final String ERROR_INVALID_INPUT = "Invalid input provided";
    public static final String ERROR_PERMISSION_DENIED = "Permission denied";
    public static final String ERROR_NETWORK_FAILURE = "Network request failed";
    public static final String ERROR_UNKNOWN = "An unknown error occurred";
    
    // Success messages
    public static final String SUCCESS_OPERATION_COMPLETED = "Operation completed successfully";
    public static final String SUCCESS_CACHE_CLEANED = "Cache cleaned successfully";
    public static final String SUCCESS_DOWNLOADS_CLEARED = "Downloads cleared successfully";
    public static final String SUCCESS_MEDIA_BACKED_UP = "Media backed up successfully";
    
    // Warning messages
    public static final String WARNING_OPERATION_IRREVERSIBLE = "This operation is irreversible";
    public static final String WARNING_DELETE_CONFIRMATION = "Are you sure you want to delete these files?";
    
    // Info messages
    public static final String INFO_OPERATION_IN_PROGRESS = "Operation in progress...";
    public static final String INFO_NO_SUGGESTIONS_AVAILABLE = "No cleanup suggestions available at this time";
    
    // Settings constants
    public static final String PREF_THEME_MODE = "pref_theme_mode";
    public static final String PREF_HAPTICS_ENABLED = "pref_haptics_enabled";
    public static final String PREF_ADS_DISABLED_UNTIL = "pref_ads_disabled_until";
    public static final String PREF_REWARDED_AD = "pref_rewarded_ad";
    public static final long ADS_REWARD_DURATION_MS = 24 * 60 * 60 * 1000; // 24 hours
    
    // Tool IDs
    public static final String TOOL_ID_UNIT_CONVERTER = "tool_unit_converter";
    
    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}