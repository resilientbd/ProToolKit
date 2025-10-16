package com.example.protoolkit.util;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.protoolkit.data.converter.UnitConverterRepository;
import com.example.protoolkit.data.device.DeviceInfoRepository;
import com.example.protoolkit.data.file.FileToolsRepository;
import com.example.protoolkit.data.network.NetworkRepository;
import com.example.protoolkit.data.settings.SettingsRepository;
import com.example.protoolkit.data.text.TextToolsRepository;
import com.example.protoolkit.ui.home.HomeViewModel;
import com.example.protoolkit.ui.settings.SettingsViewModel;
import com.example.protoolkit.ui.tools.ToolsViewModel;
import com.example.protoolkit.ui.tools.device.DeviceInfoViewModel;
import com.example.protoolkit.ui.tools.file.FileToolsViewModel;
import com.example.protoolkit.ui.tools.network.NetworkToolsViewModel;
import com.example.protoolkit.ui.tools.qr.QrScannerViewModel;
import com.example.protoolkit.ui.tools.text.TextToolsViewModel;
import com.example.protoolkit.ui.tools.unit.UnitConverterViewModel;

/**
 * Central ViewModel factory wiring repositories.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final SettingsRepository settingsRepository;
    private final UnitConverterRepository unitConverterRepository;
    private final TextToolsRepository textToolsRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final NetworkRepository networkRepository;
    private final FileToolsRepository fileToolsRepository;

    public ViewModelFactory(
            Application application,
            SettingsRepository settingsRepository,
            UnitConverterRepository unitConverterRepository,
            TextToolsRepository textToolsRepository,
            DeviceInfoRepository deviceInfoRepository,
            NetworkRepository networkRepository,
            FileToolsRepository fileToolsRepository) {
        this.application = application;
        this.settingsRepository = settingsRepository;
        this.unitConverterRepository = unitConverterRepository;
        this.textToolsRepository = textToolsRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.networkRepository = networkRepository;
        this.fileToolsRepository = fileToolsRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(application, settingsRepository);
        } else if (modelClass.isAssignableFrom(UnitConverterViewModel.class)) {
            return (T) new UnitConverterViewModel(unitConverterRepository);
        } else if (modelClass.isAssignableFrom(TextToolsViewModel.class)) {
            return (T) new TextToolsViewModel(textToolsRepository);
        } else if (modelClass.isAssignableFrom(DeviceInfoViewModel.class)) {
            return (T) new DeviceInfoViewModel(deviceInfoRepository);
        } else if (modelClass.isAssignableFrom(ToolsViewModel.class)) {
            return (T) new ToolsViewModel(application);
        } else if (modelClass.isAssignableFrom(NetworkToolsViewModel.class)) {
            return (T) new NetworkToolsViewModel(networkRepository);
        } else if (modelClass.isAssignableFrom(FileToolsViewModel.class)) {
            return (T) new FileToolsViewModel(fileToolsRepository);
        } else if (modelClass.isAssignableFrom(QrScannerViewModel.class)) {
            return (T) new QrScannerViewModel(settingsRepository);
        } else if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(settingsRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
