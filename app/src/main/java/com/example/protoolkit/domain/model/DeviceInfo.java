package com.example.protoolkit.domain.model;

import androidx.annotation.NonNull;

/**
 * Represents comprehensive device information shown in the Device Info tool.
 */
public class DeviceInfo {

    // Basic Device Information
    private final String manufacturer;
    private final String model;
    private final String brand;
    private final String androidVersion;
    private final int sdkInt;
    private final String buildNumber;

    // Hardware Information
    private final String board;
    private final String bootloader;
    private final String hardware;
    private final String product;
    private final String radioVersion;
    private final String fingerprint;

    // Storage Information
    private final String storageFree;
    private final String storageTotal;
    private final String internalStorageFree;
    private final String internalStorageTotal;
    private final String externalStorageFree;
    private final String externalStorageTotal;

    // Memory Information
    private final String memoryFree;
    private final String memoryTotal;
    private final String memoryThreshold;
    private final String memoryClass;

    // Network Information
    private final String networkType;
    private final String networkCarrier;
    private final String ipAddress;
    private final String macAddress;

    // Screen Information
    private final String screenSize;
    private final String screenResolution;
    private final String screenDensity;
    private final String screenOrientation;

    // CPU Information
    private final String cpuModel;
    private final String cpuCores;
    private final String cpuArch;
    private final String cpuFrequency;

    // Capabilities
    private final String supportedAbis;
    private final String glEsVersion;

    public DeviceInfo(@NonNull String manufacturer,
                      @NonNull String model,
                      @NonNull String brand,
                      @NonNull String androidVersion,
                      int sdkInt,
                      @NonNull String buildNumber,
                      @NonNull String board,
                      @NonNull String bootloader,
                      @NonNull String hardware,
                      @NonNull String product,
                      @NonNull String radioVersion,
                      @NonNull String fingerprint,
                      @NonNull String storageFree,
                      @NonNull String storageTotal,
                      @NonNull String internalStorageFree,
                      @NonNull String internalStorageTotal,
                      @NonNull String externalStorageFree,
                      @NonNull String externalStorageTotal,
                      @NonNull String memoryFree,
                      @NonNull String memoryTotal,
                      @NonNull String memoryThreshold,
                      @NonNull String memoryClass,
                      @NonNull String networkType,
                      @NonNull String networkCarrier,
                      @NonNull String ipAddress,
                      @NonNull String macAddress,
                      @NonNull String screenSize,
                      @NonNull String screenResolution,
                      @NonNull String screenDensity,
                      @NonNull String screenOrientation,
                      @NonNull String cpuModel,
                      @NonNull String cpuCores,
                      @NonNull String cpuArch,
                      @NonNull String cpuFrequency,
                      @NonNull String supportedAbis,
                      @NonNull String glEsVersion) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.brand = brand;
        this.androidVersion = androidVersion;
        this.sdkInt = sdkInt;
        this.buildNumber = buildNumber;
        this.board = board;
        this.bootloader = bootloader;
        this.hardware = hardware;
        this.product = product;
        this.radioVersion = radioVersion;
        this.fingerprint = fingerprint;
        this.storageFree = storageFree;
        this.storageTotal = storageTotal;
        this.internalStorageFree = internalStorageFree;
        this.internalStorageTotal = internalStorageTotal;
        this.externalStorageFree = externalStorageFree;
        this.externalStorageTotal = externalStorageTotal;
        this.memoryFree = memoryFree;
        this.memoryTotal = memoryTotal;
        this.memoryThreshold = memoryThreshold;
        this.memoryClass = memoryClass;
        this.networkType = networkType;
        this.networkCarrier = networkCarrier;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.screenSize = screenSize;
        this.screenResolution = screenResolution;
        this.screenDensity = screenDensity;
        this.screenOrientation = screenOrientation;
        this.cpuModel = cpuModel;
        this.cpuCores = cpuCores;
        this.cpuArch = cpuArch;
        this.cpuFrequency = cpuFrequency;
        this.supportedAbis = supportedAbis;
        this.glEsVersion = glEsVersion;
    }

    // Basic Device Information
    @NonNull
    public String getManufacturer() { return manufacturer; }
    
    @NonNull
    public String getModel() { return model; }
    
    @NonNull
    public String getBrand() { return brand; }
    
    @NonNull
    public String getAndroidVersion() { return androidVersion; }
    
    public int getSdkInt() { return sdkInt; }
    
    @NonNull
    public String getBuildNumber() { return buildNumber; }

    // Hardware Information
    @NonNull
    public String getBoard() { return board; }
    
    @NonNull
    public String getBootloader() { return bootloader; }
    
    @NonNull
    public String getHardware() { return hardware; }
    
    @NonNull
    public String getProduct() { return product; }
    
    @NonNull
    public String getRadioVersion() { return radioVersion; }
    
    @NonNull
    public String getFingerprint() { return fingerprint; }

    // Storage Information
    @NonNull
    public String getStorageFree() { return storageFree; }
    
    @NonNull
    public String getStorageTotal() { return storageTotal; }
    
    @NonNull
    public String getInternalStorageFree() { return internalStorageFree; }
    
    @NonNull
    public String getInternalStorageTotal() { return internalStorageTotal; }
    
    @NonNull
    public String getExternalStorageFree() { return externalStorageFree; }
    
    @NonNull
    public String getExternalStorageTotal() { return externalStorageTotal; }

    // Memory Information
    @NonNull
    public String getMemoryFree() { return memoryFree; }
    
    @NonNull
    public String getMemoryTotal() { return memoryTotal; }
    
    @NonNull
    public String getMemoryThreshold() { return memoryThreshold; }
    
    @NonNull
    public String getMemoryClass() { return memoryClass; }

    // Network Information
    @NonNull
    public String getNetworkType() { return networkType; }
    
    @NonNull
    public String getNetworkCarrier() { return networkCarrier; }
    
    @NonNull
    public String getIpAddress() { return ipAddress; }
    
    @NonNull
    public String getMacAddress() { return macAddress; }

    // Screen Information
    @NonNull
    public String getScreenSize() { return screenSize; }
    
    @NonNull
    public String getScreenResolution() { return screenResolution; }
    
    @NonNull
    public String getScreenDensity() { return screenDensity; }
    
    @NonNull
    public String getScreenOrientation() { return screenOrientation; }

    // CPU Information
    @NonNull
    public String getCpuModel() { return cpuModel; }
    
    @NonNull
    public String getCpuCores() { return cpuCores; }
    
    @NonNull
    public String getCpuArch() { return cpuArch; }
    
    @NonNull
    public String getCpuFrequency() { return cpuFrequency; }

    // Capabilities
    @NonNull
    public String getSupportedAbis() { return supportedAbis; }
    
    @NonNull
    public String getGlEsVersion() { return glEsVersion; }
}