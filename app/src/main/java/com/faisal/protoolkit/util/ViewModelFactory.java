package com.faisal.protoolkit.util;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.faisal.protoolkit.data.converter.UnitConverterRepository;
import com.faisal.protoolkit.data.device.DeviceInfoRepository;
import com.faisal.protoolkit.data.file.FileToolsRepository;
import com.faisal.protoolkit.data.network.NetworkRepository;
import com.faisal.protoolkit.data.network.NetworkToolsRepository;
import com.faisal.protoolkit.data.settings.SettingsRepository;
import com.faisal.protoolkit.data.text.TextToolsRepository;
import com.faisal.protoolkit.ui.home.HomeViewModel;
import com.faisal.protoolkit.ui.settings.SettingsViewModel;
import com.faisal.protoolkit.ui.tools.ToolsViewModel;
import com.faisal.protoolkit.ui.tools.device.DeviceInfoViewModel;
import com.faisal.protoolkit.ui.tools.file.FileToolsViewModel;
import com.faisal.protoolkit.ui.tools.network.NetworkToolsViewModel;
import com.faisal.protoolkit.ui.tools.qr.QrScannerViewModel;
import com.faisal.protoolkit.ui.tools.text.TextToolsViewModel;
import com.faisal.protoolkit.ui.tools.unit.UnitConverterViewModel;
import com.faisal.protoolkit.ui.tools.document.DocumentScannerViewModel;

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
    private final NetworkToolsRepository networkToolsRepository;
    private final FileToolsRepository fileToolsRepository;

    public ViewModelFactory(
            Application application,
            SettingsRepository settingsRepository,
            UnitConverterRepository unitConverterRepository,
            TextToolsRepository textToolsRepository,
            DeviceInfoRepository deviceInfoRepository,
            NetworkRepository networkRepository,
            NetworkToolsRepository networkToolsRepository,
            FileToolsRepository fileToolsRepository) {
        this.application = application;
        this.settingsRepository = settingsRepository;
        this.unitConverterRepository = unitConverterRepository;
        this.textToolsRepository = textToolsRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.networkRepository = networkRepository;
        this.networkToolsRepository = networkToolsRepository;
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
            return (T) new NetworkToolsViewModel(networkToolsRepository);
        } else if (modelClass.isAssignableFrom(FileToolsViewModel.class)) {
            return (T) new FileToolsViewModel(fileToolsRepository);
        } else if (modelClass.isAssignableFrom(QrScannerViewModel.class)) {
            return (T) new QrScannerViewModel(settingsRepository);
        } else if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(settingsRepository);
        } else if (modelClass.isAssignableFrom(DocumentScannerViewModel.class)) {
            return (T) new DocumentScannerViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
