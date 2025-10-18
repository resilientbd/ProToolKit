package com.faisal.protoolkit.ui.tools.network;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.faisal.protoolkit.data.network.NetworkToolsRepository;
import com.faisal.protoolkit.ui.base.BaseViewModel;
import com.faisal.protoolkit.util.AppConstants;

/**
 * Executes comprehensive network tests for the Network Tools screen.
 */
public class NetworkToolsViewModel extends BaseViewModel {

    private final NetworkToolsRepository repository;
    
    // Basic network test results
    private final MutableLiveData<Integer> latencyMs = new MutableLiveData<>();
    private final MutableLiveData<Long> downloadSpeed = new MutableLiveData<>();
    private final MutableLiveData<Integer> speedTestProgress = new MutableLiveData<>(0);
    private final MutableLiveData<NetworkToolsRepository.NetworkInfoDetails> connectionInfo = new MutableLiveData<>();
    
    // Advanced network test results
    private final MutableLiveData<String> dnsLookupResult = new MutableLiveData<>();
    private final MutableLiveData<String> portScanResult = new MutableLiveData<>();
    private final MutableLiveData<String> tracerouteResult = new MutableLiveData<>();
    private final MutableLiveData<String> diagnosticsResult = new MutableLiveData<>();
    
    // General states
    private final MutableLiveData<String> targetUrl = new MutableLiveData<>(AppConstants.DEFAULT_PING_TARGET);
    private final MutableLiveData<Boolean> inProgress = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentTest = new MutableLiveData<>("");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");

    public NetworkToolsViewModel(@NonNull NetworkToolsRepository repository) {
        this.repository = repository;
    }

    // Getters for basic network test results
    public LiveData<Integer> getLatencyMs() { return latencyMs; }
    public LiveData<Long> getDownloadSpeed() { return downloadSpeed; }
    public LiveData<Integer> getSpeedTestProgress() { return speedTestProgress; }
    public LiveData<NetworkToolsRepository.NetworkInfoDetails> getConnectionInfo() { return connectionInfo; }
    
    // Getters for advanced network test results
    public LiveData<String> getDnsLookupResult() { return dnsLookupResult; }
    public LiveData<String> getPortScanResult() { return portScanResult; }
    public LiveData<String> getTracerouteResult() { return tracerouteResult; }
    public LiveData<String> getDiagnosticsResult() { return diagnosticsResult; }
    
    // Getters for general states
    public LiveData<String> getTargetUrl() { return targetUrl; }
    public LiveData<Boolean> inProgress() { return inProgress; }
    public LiveData<String> getCurrentTest() { return currentTest; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void setTargetUrl(@NonNull String url) {
        targetUrl.setValue(url);
    }

    // Basic network tests
    public void measureLatency() {
        String url = targetUrl.getValue();
        if (url == null || url.trim().isEmpty()) {
            postError("Invalid URL");
            return;
        }
        inProgress.setValue(true);
        currentTest.setValue("Latency Test");
        
        repository.measureLatency(url, 4, 64, AppConstants.NETWORK_TIMEOUT_MS, new NetworkToolsRepository.LatencyCallback() {
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
        
        repository.performSpeedTest(url, new NetworkToolsRepository.SpeedTestCallback() {
            @Override
            public void onProgress(int progressPercent) {
                speedTestProgress.postValue(progressPercent);
            }

            @Override
            public void onComplete(long downloadSpeedKBps, long uploadSpeedKBps) {
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
        
        repository.checkConnection(new NetworkToolsRepository.ConnectionCallback() {
            @Override
            public void onSuccess(NetworkToolsRepository.NetworkInfoDetails info) {
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
        
        repository.pingHost(host, new NetworkToolsRepository.LatencyCallback() {
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

    // Advanced network tests
    public void performDnsLookup() {
        String host = targetUrl.getValue();
        if (host == null || host.trim().isEmpty()) {
            postError("Invalid host for DNS lookup");
            return;
        }
        inProgress.setValue(true);
        currentTest.setValue("DNS Lookup");
        
        repository.performDnsLookup(host, new NetworkToolsRepository.DnsLookupCallback() {
            @Override
            public void onSuccess(String result) {
                dnsLookupResult.postValue(result);
                inProgress.postValue(false);
                currentTest.postValue("");
            }

            @Override
            public void onError(String message) {
                dnsLookupResult.postValue(null);
                postError("DNS lookup failed: " + message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }

    public void performPortScan() {
        String host = targetUrl.getValue();
        if (host == null || host.trim().isEmpty()) {
            postError("Invalid host for port scan");
            return;
        }
        inProgress.setValue(true);
        currentTest.setValue("Port Scan");
        
        repository.performPortScan(host, 1, 1024, AppConstants.NETWORK_TIMEOUT_MS, 50, new NetworkToolsRepository.PortScanCallback() {
            @Override
            public void onSuccess(String result) {
                portScanResult.postValue(result);
                inProgress.postValue(false);
                currentTest.postValue("");
            }

            @Override
            public void onError(String message) {
                portScanResult.postValue(null);
                postError("Port scan failed: " + message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }

    public void performTraceroute() {
        String host = targetUrl.getValue();
        if (host == null || host.trim().isEmpty()) {
            postError("Invalid host for traceroute");
            return;
        }
        inProgress.setValue(true);
        currentTest.setValue("Traceroute");
        
        repository.performTraceroute(host, new NetworkToolsRepository.TracerouteCallback() {
            @Override
            public void onSuccess(String result) {
                tracerouteResult.postValue(result);
                inProgress.postValue(false);
                currentTest.postValue("");
            }

            @Override
            public void onError(String message) {
                tracerouteResult.postValue(null);
                postError("Traceroute failed: " + message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }

    public void runFullDiagnostics() {
        inProgress.setValue(true);
        currentTest.setValue("Full Diagnostics");
        
        repository.runFullDiagnostics(new NetworkToolsRepository.DiagnosticsCallback() {
            @Override
            public void onSuccess(String result) {
                diagnosticsResult.postValue(result);
                inProgress.postValue(false);
                currentTest.postValue("");
            }

            @Override
            public void onError(String message) {
                diagnosticsResult.postValue(null);
                postError("Diagnostics failed: " + message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }

    // File management actions
    public void cleanCache() {
        inProgress.setValue(true);
        currentTest.setValue("Cleaning Cache");
        
        repository.cleanCache(new NetworkToolsRepository.LatencyCallback() {
            @Override
            public void onSuccess(int freedSpaceMB) {
                inProgress.postValue(false);
                currentTest.postValue("");
                postMessage("Cache cleaned successfully! Freed space: " + freedSpaceMB + " MB");
            }

            @Override
            public void onError(String message) {
                postError("Cache cleaning failed: " + message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }
    
    public void clearDownloads() {
        inProgress.setValue(true);
        currentTest.setValue("Clearing Downloads");
        
        repository.clearDownloads(new NetworkToolsRepository.LatencyCallback() {
            @Override
            public void onSuccess(int freedSpaceMB) {
                inProgress.postValue(false);
                currentTest.postValue("");
                postMessage("Downloads cleared successfully! Freed space: " + freedSpaceMB + " MB");
            }

            @Override
            public void onError(String message) {
                postError("Downloads clearing failed: " + message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }
    
    public void backupMedia() {
        inProgress.setValue(true);
        currentTest.setValue("Backing Up Media");
        
        repository.backupMedia(new NetworkToolsRepository.LatencyCallback() {
            @Override
            public void onSuccess(int backedUpSizeMB) {
                inProgress.postValue(false);
                currentTest.postValue("");
                postMessage("Media backup completed successfully! Backed up: " + backedUpSizeMB + " MB");
            }

            @Override
            public void onError(String message) {
                postError("Media backup failed: " + message);
                inProgress.postValue(false);
                currentTest.postValue("");
            }
        });
    }

    protected void postError(String message) {
        errorMessage.postValue(message);
    }
    
    private void postMessage(String message) {
        // This would typically update a message LiveData or show a toast
        // For now, we'll just log it
        System.out.println("NetworkToolsViewModel: " + message);
    }
}