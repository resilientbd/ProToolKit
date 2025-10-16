package com.example.protoolkit.ui.tools.network;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.protoolkit.data.network.NetworkRepository;
import com.example.protoolkit.ui.base.BaseViewModel;
import com.example.protoolkit.util.AppConstants;

/**
 * Executes comprehensive network tests for the Network Tools screen.
 */
public class NetworkToolsViewModel extends BaseViewModel {

    private final NetworkRepository repository;
    
    // Latency test
    private final MutableLiveData<Integer> latencyMs = new MutableLiveData<>();
    
    // Speed test
    private final MutableLiveData<Long> downloadSpeed = new MutableLiveData<>();
    private final MutableLiveData<Integer> speedTestProgress = new MutableLiveData<>(0);
    
    // Connection info
    private final MutableLiveData<NetworkRepository.NetworkInfo> connectionInfo = new MutableLiveData<>();
    
    // General states
    private final MutableLiveData<String> targetUrl = new MutableLiveData<>(AppConstants.DEFAULT_PING_TARGET);
    private final MutableLiveData<Boolean> inProgress = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentTest = new MutableLiveData<>("");

    public NetworkToolsViewModel(@NonNull NetworkRepository repository) {
        this.repository = repository;
    }

    // Getters
    public LiveData<Integer> getLatencyMs() { return latencyMs; }
    public LiveData<Long> getDownloadSpeed() { return downloadSpeed; }
    public LiveData<Integer> getSpeedTestProgress() { return speedTestProgress; }
    public LiveData<NetworkRepository.NetworkInfo> getConnectionInfo() { return connectionInfo; }
    public LiveData<String> getTargetUrl() { return targetUrl; }
    public LiveData<Boolean> inProgress() { return inProgress; }
    public LiveData<String> getCurrentTest() { return currentTest; }

    public void setTargetUrl(@NonNull String url) {
        targetUrl.setValue(url);
    }

    public void measureLatency() {
        String url = targetUrl.getValue();
        if (url == null || url.trim().isEmpty()) {
            postError("Invalid URL");
            return;
        }
        inProgress.setValue(true);
        currentTest.setValue("Latency Test");
        
        repository.measureLatency(url, new NetworkRepository.LatencyCallback() {
            @Override
            public void onSuccess(int latency) {
                latencyMs.postValue(latency);
                inProgress.postValue(false);
                currentTest.postValue("");
            }

            @Override
            public void onError(String message) {
                latencyMs.postValue(null);
                postError(message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }

    public void performSpeedTest() {
        String url = "https://httpbin.org/bytes/1048576"; // 1MB test file
        inProgress.setValue(true);
        currentTest.setValue("Speed Test");
        speedTestProgress.setValue(0);
        
        repository.performSpeedTest(url, new NetworkRepository.SpeedTestCallback() {
            @Override
            public void onProgress(int progressPercent) {
                speedTestProgress.postValue(progressPercent);
            }

            @Override
            public void onComplete(long downloadSpeedKBps) {
                downloadSpeed.postValue(downloadSpeedKBps);
                inProgress.postValue(false);
                currentTest.postValue("");
            }

            @Override
            public void onError(String message) {
                downloadSpeed.postValue(0L);
                postError(message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }

    public void checkConnection() {
        inProgress.setValue(true);
        currentTest.setValue("Connection Check");
        
        repository.checkConnection(new NetworkRepository.ConnectionCallback() {
            @Override
            public void onSuccess(NetworkRepository.NetworkInfo info) {
                connectionInfo.postValue(info);
                inProgress.postValue(false);
                currentTest.postValue("");
            }

            @Override
            public void onError(String message) {
                postError(message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }

    public void pingHost() {
        String host = targetUrl.getValue();
        if (host == null || host.trim().isEmpty()) {
            postError("Invalid host");
            return;
        }
        inProgress.setValue(true);
        currentTest.setValue("Ping Test");
        
        repository.pingHost(host, new NetworkRepository.LatencyCallback() {
            @Override
            public void onSuccess(int latency) {
                latencyMs.postValue(latency);
                inProgress.postValue(false);
                currentTest.postValue("");
            }

            @Override
            public void onError(String message) {
                latencyMs.postValue(null);
                postError(message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }
}
