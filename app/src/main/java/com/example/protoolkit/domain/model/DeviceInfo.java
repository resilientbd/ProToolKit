package com.example.protoolkit.domain.model;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Represents device information shown in the Device Info tool.
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
    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public String getBrand() { return brand; }
    public String getAndroidVersion() { return androidVersion; }
    public int getSdkInt() { return sdkInt; }
    public String getBuildNumber() { return buildNumber; }

    // Hardware Information
    public String getBoard() { return board; }
    public String getBootloader() { return bootloader; }
    public String getHardware() { return hardware; }
    public String getProduct() { return product; }
    public String getRadioVersion() { return radioVersion; }
    public String getFingerprint() { return fingerprint; }

    // Storage Information
    public String getStorageFree() { return storageFree; }
    public String getStorageTotal() { return storageTotal; }
    public String getInternalStorageFree() { return internalStorageFree; }
    public String getInternalStorageTotal() { return internalStorageTotal; }
    public String getExternalStorageFree() { return externalStorageFree; }
    public String getExternalStorageTotal() { return externalStorageTotal; }

    // Memory Information
    public String getMemoryFree() { return memoryFree; }
    public String getMemoryTotal() { return memoryTotal; }
    public String getMemoryThreshold() { return memoryThreshold; }
    public String getMemoryClass() { return memoryClass; }

    // Network Information
    public String getNetworkType() { return networkType; }
    public String getNetworkCarrier() { return networkCarrier; }
    public String getIpAddress() { return ipAddress; }
    public String getMacAddress() { return macAddress; }

    // Screen Information
    public String getScreenSize() { return screenSize; }
    public String getScreenResolution() { return screenResolution; }
    public String getScreenDensity() { return screenDensity; }
    public String getScreenOrientation() { return screenOrientation; }

    // CPU Information
    public String getCpuModel() { return cpuModel; }
    public String getCpuCores() { return cpuCores; }
    public String getCpuArch() { return cpuArch; }
    public String getCpuFrequency() { return cpuFrequency; }

    // Capabilities
    public String getSupportedAbis() { return supportedAbis; }
    public String getGlEsVersion() { return glEsVersion; }
}