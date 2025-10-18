package com.example.protoolkit.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.splashscreen.SplashScreen.Companion;

import com.example.protoolkit.ui.MainActivity;
import com.example.protoolkit.R;

/**
 * Splash screen activity that shows branded startup screen.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Install splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        // Set content view (required for splash screen to work)
        setContentView(R.layout.activity_splash);
        
        // Keep splash screen visible for a minimum time
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Navigate to main activity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}