package com.faisal.protoolkit.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.ActivityMainBinding;
import com.faisal.protoolkit.util.AnalyticsLogger;
import com.faisal.protoolkit.util.ServiceLocator;

/**
 * Single-activity entry point hosting the Navigation Component.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private final AnalyticsLogger analyticsLogger = ServiceLocator.getAnalyticsLogger();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(R.string.app_title);
        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return;
        }
        NavController navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.toolsFragment,
                R.id.settingsFragment)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                analyticsLogger.logClick("bottom_nav_" + item.getTitle());
            }
            return handled;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return super.onSupportNavigateUp();
        }
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
