package com.faisal.protoolkit.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * Provides enhanced haptic feedback functionality for user interactions.
 */
public class HapticHelper {

    /**
     * Vibrates the device with a light tap effect.
     */
    public static void vibrate(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrateModern(context, VibrationEffect.EFFECT_TICK);
        } else {
            vibrateLegacy(context, 20);
        }
    }

    /**
     * Vibrates the device with a medium impact effect.
     */
    public static void vibrateMedium(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrateModern(context, VibrationEffect.EFFECT_CLICK);
        } else {
            vibrateLegacy(context, 50);
        }
    }

    /**
     * Vibrates the device with a heavy impact effect.
     */
    public static void vibrateHeavy(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrateModern(context, VibrationEffect.EFFECT_HEAVY_CLICK);
        } else {
            vibrateLegacy(context, 100);
        }
    }

    /**
     * Vibrates the device with a double tap effect.
     */
    public static void vibrateDoubleTap(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrateModern(context, VibrationEffect.EFFECT_DOUBLE_CLICK);
        } else {
            vibrateLegacy(context, new long[]{0, 50, 100, 50});
        }
    }

    /**
     * Vibrates the device with a custom duration.
     */
    public static void vibrateCustom(@NonNull Context context, long milliseconds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibrationEffect effect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrateModern(context, effect);
        } else {
            vibrateLegacy(context, milliseconds);
        }
    }

    /**
     * Vibrates the device with a custom pattern.
     */
    public static void vibratePattern(@NonNull Context context, long[] pattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
            vibrateModern(context, effect);
        } else {
            vibrateLegacy(context, pattern);
        }
    }

    /**
     * Provides haptic feedback on a view.
     */
    public static void provideHapticFeedback(@NonNull View view) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
    }

    /**
     * Provides confirmation haptic feedback on a view.
     */
    public static void provideConfirmationFeedback(@NonNull View view) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
    }

    /**
     * Provides error haptic feedback on a view.
     */
    public static void provideErrorFeedback(@NonNull View view) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT);
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private static void vibrateModern(@NonNull Context context, int effectId) {
        VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        if (vibratorManager != null) {
            Vibrator vibrator = vibratorManager.getDefaultVibrator();
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createPredefined(effectId));
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private static void vibrateModern(@NonNull Context context, VibrationEffect effect) {
        VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        if (vibratorManager != null) {
            Vibrator vibrator = vibratorManager.getDefaultVibrator();
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(effect);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void vibrateLegacy(@NonNull Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void vibrateLegacy(@NonNull Context context, long[] pattern) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }
}