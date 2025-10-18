package com.faisal.protoolkit.util;

import java.util.Locale;

/**
 * Provides formatting utilities for bytes, storage, and other data.
 */
public class FormatUtils {

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;
    private static final long TB = GB * 1024;

    /**
     * Formats bytes into human-readable strings with appropriate units.
     */
    public static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        
        if (bytes < KB) {
            return bytes + " B";
        } else if (bytes < MB) {
            return String.format("%.1f KB", (float) bytes / KB);
        } else if (bytes < GB) {
            return String.format("%.1f MB", (float) bytes / MB);
        } else if (bytes < TB) {
            return String.format("%.2f GB", (float) bytes / GB);
        } else {
            return String.format("%.2f TB", (float) bytes / TB);
        }
    }

    /**
     * Formats milliseconds into human-readable strings.
     */
    public static String formatMilliseconds(int milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + " ms";
        } else {
            return String.format("%.1f s", (float) milliseconds / 1000);
        }
    }

    /**
     * Formats network speeds into human-readable strings.
     */
    public static String formatSpeed(long speedKBps) {
        if (speedKBps < 1000) {
            return speedKBps + " KB/s";
        } else {
            return String.format("%.1f MB/s", (float) speedKBps / 1000);
        }
    }

    /**
     * Formats percentages into human-readable strings.
     */
    public static String formatPercentage(int percentage) {
        return percentage + "%";
    }

    /**
     * Capitalizes the first letter of a string.
     */
    public static String capitalize(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    /**
     * Converts camelCase to spaced words.
     */
    public static String camelCaseToSpacedWords(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        result.append(Character.toUpperCase(camelCase.charAt(0)));
        
        for (int i = 1; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append(' ');
            }
            result.append(ch);
        }
        
        return result.toString();
    }
    
    /**
     * Formats network latency into human-readable strings.
     */
    public static String formatLatency(int latencyMs) {
        if (latencyMs < 50) {
            return latencyMs + " ms (Excellent)";
        } else if (latencyMs < 100) {
            return latencyMs + " ms (Good)";
        } else if (latencyMs < 200) {
            return latencyMs + " ms (Fair)";
        } else {
            return latencyMs + " ms (Poor)";
        }
    }
    
    /**
     * Formats storage usage into human-readable strings.
     */
    public static String formatStorageUsage(long usedBytes, long totalBytes) {
        if (totalBytes <= 0) {
            return "0%";
        }
        
        long freeBytes = totalBytes - usedBytes;
        double usagePercentage = (double) usedBytes / totalBytes * 100;
        double freePercentage = (double) freeBytes / totalBytes * 100;
        
        return String.format("%.1f%% used (%s free of %s)", 
                           usagePercentage, 
                           formatBytes(freeBytes), 
                           formatBytes(totalBytes));
    }
    
    /**
     * Formats network type into human-readable strings.
     */
    public static String formatNetworkType(int type) {
        switch (type) {
            case 0: return "Unknown";
            case 1: return "WiFi";
            case 2: return "Mobile";
            case 3: return "Ethernet";
            case 4: return "Bluetooth";
            case 5: return "VPN";
            default: return "Other";
        }
    }
    
    /**
     * Formats battery level into human-readable strings.
     */
    public static String formatBatteryLevel(int level, int scale) {
        if (scale <= 0) {
            return "Unknown";
        }
        
        int percentage = (level * 100) / scale;
        return percentage + "%";
    }
    
    /**
     * Formats temperature into human-readable strings.
     */
    public static String formatTemperature(float temperature) {
        // Convert from tenths of a degree Celsius to degrees Celsius
        float celsius = temperature / 10.0f;
        return String.format("%.1f°C (%.1f°F)", celsius, (celsius * 9/5) + 32);
    }
    
    /**
     * Formats voltage into human-readable strings.
     */
    public static String formatVoltage(int voltage) {
        // Convert from millivolts to volts
        float volts = voltage / 1000.0f;
        return String.format("%.2f V", volts);
    }
    
    /**
     * Formats numbers into human-readable strings.
     */
    public static String formatNumber(double number) {
        if (number == (long) number) {
            return String.format("%d", (long) number);
        } else {
            return String.format("%.2f", number);
        }
    }
    
    /**
     * Formats numbers into human-readable strings with commas.
     */
    public static String formatNumberWithCommas(long number) {
        return String.format(Locale.getDefault(), "%,d", number);
    }
}