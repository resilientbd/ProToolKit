package com.example.protoolkit.util;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.example.protoolkit.ads.AdsManager;
import com.example.protoolkit.data.converter.UnitConverterRepository;
import com.example.protoolkit.data.device.DeviceInfoRepository;
import com.example.protoolkit.data.file.FileToolsRepository;
import com.example.protoolkit.data.network.NetworkRepository;
import com.example.protoolkit.data.network.NetworkToolsRepository;
import com.example.protoolkit.data.settings.SettingsRepository;
import com.example.protoolkit.data.text.TextToolsRepository;

/**
 * Lazy service locator for app-wide singletons.
 */
public final class ServiceLocator {

    private static Application application;
    private static SettingsRepository settingsRepository;
    private static UnitConverterRepository unitConverterRepository;
    private static TextToolsRepository textToolsRepository;
    private static DeviceInfoRepository deviceInfoRepository;
    private static NetworkRepository networkRepository;
    private static NetworkToolsRepository networkToolsRepository;
    private static FileToolsRepository fileToolsRepository;
    private static AnalyticsLogger analyticsLogger;
    private static ViewModelFactory viewModelFactory;

    private ServiceLocator() {
    }

    public static void initialize(@NonNull Application app) {
        application = app;
    }

    public static Context getContext() {
        return application;
    }

    public static SettingsRepository getSettingsRepository() {
        if (settingsRepository == null) {
            settingsRepository = new SettingsRepository(application);
        }
        return settingsRepository;
    }

    public static UnitConverterRepository getUnitConverterRepository() {
        if (unitConverterRepository == null) {
            unitConverterRepository = new UnitConverterRepository();
        }
        return unitConverterRepository;
    }

    public static TextToolsRepository getTextToolsRepository() {
        if (textToolsRepository == null) {
            textToolsRepository = new TextToolsRepository();
        }
        return textToolsRepository;
    }

    public static DeviceInfoRepository getDeviceInfoRepository() {
        if (deviceInfoRepository == null) {
            deviceInfoRepository = new DeviceInfoRepository(application);
        }
        return deviceInfoRepository;
    }

    public static NetworkRepository getNetworkRepository() {
        if (networkRepository == null) {
            networkRepository = new NetworkRepository();
        }
        return networkRepository;
    }

    public static NetworkToolsRepository getNetworkToolsRepository() {
        if (networkToolsRepository == null) {
            networkToolsRepository = new NetworkToolsRepository(application);
        }
        return networkToolsRepository;
    }

    public static FileToolsRepository getFileToolsRepository() {
        if (fileToolsRepository == null) {
            fileToolsRepository = new FileToolsRepository(application);
        }
        return fileToolsRepository;
    }

    public static AnalyticsLogger getAnalyticsLogger() {
        if (analyticsLogger == null) {
            analyticsLogger = new AnalyticsLogger();
        }
        return analyticsLogger;
    }

    public static ViewModelFactory getViewModelFactory() {
        if (viewModelFactory == null) {
            viewModelFactory = new ViewModelFactory(
                    application,
                    getSettingsRepository(),
                    getUnitConverterRepository(),
                    getTextToolsRepository(),
                    getDeviceInfoRepository(),
                    getNetworkRepository(),
                    getNetworkToolsRepository(),
                    getFileToolsRepository());
        }
        return viewModelFactory;
    }

    public static AdsManager getAdsManager() {
        return AdsManager.getInstance(application);
    }
}
