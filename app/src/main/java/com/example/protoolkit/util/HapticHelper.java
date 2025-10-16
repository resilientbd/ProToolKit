package com.example.protoolkit.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;

/**
 * Simple helper for haptic feedback on supported devices.
 */
public final class HapticHelper {

    private HapticHelper() {
    }

    public static void vibrate(@NonNull Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(40);
            }
        } catch (SecurityException ignored) {
            // Device may revoke VIBRATE permission; ignore vibration and continue.
        }
    }
}
