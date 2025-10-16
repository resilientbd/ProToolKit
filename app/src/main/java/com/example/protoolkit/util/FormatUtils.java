package com.example.protoolkit.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Helpers for consistent numeric formatting.
 */
public final class FormatUtils {

    private static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##");

    private FormatUtils() {
    }

    public static String formatNumber(double value) {
        return TWO_DECIMAL.format(value);
    }

    public static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String formatPercentage(double fraction) {
        NumberFormat percentInstance = NumberFormat.getPercentInstance(Locale.getDefault());
        percentInstance.setMaximumFractionDigits(1);
        return percentInstance.format(fraction);
    }
}
