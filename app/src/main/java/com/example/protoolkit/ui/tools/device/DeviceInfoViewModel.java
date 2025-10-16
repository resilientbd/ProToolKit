package com.example.protoolkit.ui.tools.device;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.protoolkit.data.device.DeviceInfoRepository;
import com.example.protoolkit.domain.model.DeviceInfo;
import com.example.protoolkit.ui.base.BaseViewModel;
import com.example.protoolkit.util.AppExecutors;

/**
 * Loads and exposes device information.
 */
public class DeviceInfoViewModel extends BaseViewModel {

    private final DeviceInfoRepository repository;
    private final MutableLiveData<DeviceInfo> deviceInfo = new MutableLiveData<>();

    public DeviceInfoViewModel(@NonNull DeviceInfoRepository repository) {
        this.repository = repository;
        refresh();
    }

    public LiveData<DeviceInfo> getDeviceInfo() {
        return deviceInfo;
    }

    public void refresh() {
        setLoading(true);
        AppExecutors.io().execute(() -> {
            DeviceInfo info = repository.fetchDeviceInfo();
            deviceInfo.postValue(info);
            setLoading(false);
        });
    }
}
