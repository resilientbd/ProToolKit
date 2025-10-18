package com.faisal.protoolkit.ui.tools.device;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.faisal.protoolkit.data.device.DeviceInfoRepository;
import com.faisal.protoolkit.domain.model.DeviceInfo;
import com.faisal.protoolkit.ui.base.BaseViewModel;
import com.faisal.protoolkit.util.AppExecutors;

/**
 * Loads and exposes comprehensive device information.
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