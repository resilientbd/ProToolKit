package com.example.protoolkit.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.protoolkit.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * Collects comprehensive device information for display.
 */
public class DeviceInfoCollector {

    private final Application application;

    public DeviceInfoCollector(@NonNull Application application) {
        this.application = application;
    }

    /**
     * Gets basic device information.
     */
    @NonNull
    public DeviceInfo getDeviceInfo() {
        return new DeviceInfo(
                // Basic Device Information
                capitalize(Build.MANUFACTURER),
                Build.MODEL,
                capitalize(Build.BRAND),
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.DISPLAY,
                
                // Hardware Information
                Build.BOARD,
                Build.BOOTLOADER,
                Build.HARDWARE,
                Build.PRODUCT,
                Build.getRadioVersion(),
                Build.FINGERPRINT,
                
                // Storage Information
                getStorageInfo(),
                getInternalStorageInfo(),
                getExternalStorageInfo(),
                
                // Memory Information
                getMemoryInfo(),
                getMemoryThreshold(),
                getMemoryClass(),
                
                // Network Information
                getNetworkInfo(),
                
                // Screen Information
                getScreenInfo(),
                
                // CPU Information
                getCpuInfo(),
                
                // Capabilities
                getSupportedAbis(),
                getOpenglEsVersion()
        );
    }

    private StorageInfo getStorageInfo() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        long availableBlocks = statFs.getAvailableBlocksLong();

        long totalBytes = totalBlocks * blockSize;
        long freeBytes = availableBlocks * blockSize;

        return new StorageInfo(FormatUtils.formatBytes(freeBytes), FormatUtils.formatBytes(totalBytes));
    }

    private StorageInfo getInternalStorageInfo() {
        File internalStorage = Environment.getDataDirectory();
        StatFs statFs = new StatFs(internalStorage.getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        long availableBlocks = statFs.getAvailableBlocksLong();

        long totalBytes = totalBlocks * blockSize;
        long freeBytes = availableBlocks * blockSize;

        return new StorageInfo(FormatUtils.formatBytes(freeBytes), FormatUtils.formatBytes(totalBytes));
    }

    private StorageInfo getExternalStorageInfo() {
        if (Environment.getExternalStorageDirectory() != null) {
            File externalStorage = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(externalStorage.getAbsolutePath());
            long blockSize = statFs.getBlockSizeLong();
            long totalBlocks = statFs.getBlockCountLong();
            long availableBlocks = statFs.getAvailableBlocksLong();

            long totalBytes = totalBlocks * blockSize;
            long freeBytes = availableBlocks * blockSize;

            return new StorageInfo(FormatUtils.formatBytes(freeBytes), FormatUtils.formatBytes(totalBytes));
        }
        return new StorageInfo("Unknown", "Unknown");
    }

    private MemoryInfo getMemoryInfo() {
        ActivityManager manager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return new MemoryInfo("Unknown", "Unknown");
        }
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        long total = info.totalMem;
        long avail = info.availMem;
        return new MemoryInfo(FormatUtils.formatBytes(avail), FormatUtils.formatBytes(total));
    }

    private long getMemoryThreshold() {
        ActivityManager manager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            return info.threshold;
        }
        return 0;
    }

    private String getMemoryClass() {
        ActivityManager manager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            return String.valueOf(manager.getMemoryClass()) + " MB";
        }
        return "Unknown";
    }

    private NetworkInfoDetails getNetworkInfo() {
        String type = "Unknown";
        String carrier = "Unknown";
        String ipAddress = "Unknown";
        String macAddress = "Unknown";

        try {
            ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    type = activeNetwork.getTypeName() + " - " + activeNetwork.getSubtypeName();
                }
            }

            // Get carrier name
            TelephonyManager tm = 
                (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                carrier = tm.getNetworkOperatorName();
            }

            // Get IP address
            ipAddress = getIPAddress(true); // IPv4
            if ("0.0.0.0".equals(ipAddress)) {
                ipAddress = getIPAddress(false); // IPv6
            }

            // Get MAC address (requires permission and may not work on all Android versions)
            WifiManager wifiManager = (WifiManager) application.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    macAddress = wifiInfo.getMacAddress();
                }
            }
        } catch (Exception e) {
            // Handle any exceptions
        }

        return new NetworkInfoDetails(type, carrier, ipAddress, macAddress);
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String sAddr = inetAddress.getHostAddress().toUpperCase(Locale.getDefault());
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { 
            // for now eat exceptions
        }
        return "0.0.0.0";
    }

    private ScreenInfo getScreenInfo() {
        String size = "Unknown";
        String resolution = "Unknown";
        String density = "Unknown";
        String orientation = "Unknown";

        WindowManager wm = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            Point sizePoint = new Point();
            display.getSize(sizePoint);

            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);

            // Resolution
            resolution = sizePoint.x + " x " + sizePoint.y;

            // Density
            float densityValue = metrics.density;
            String densityBucket;
            switch (metrics.densityDpi) {
                case DisplayMetrics.DENSITY_LOW: densityBucket = "ldpi"; break;
                case DisplayMetrics.DENSITY_MEDIUM: densityBucket = "mdpi"; break;
                case DisplayMetrics.DENSITY_HIGH: densityBucket = "hdpi"; break;
                case DisplayMetrics.DENSITY_XHIGH: densityBucket = "xhdpi"; break;
                case DisplayMetrics.DENSITY_XXHIGH: densityBucket = "xxhdpi"; break;
                case DisplayMetrics.DENSITY_XXXHIGH: densityBucket = "xxxhdpi"; break;
                default: densityBucket = "unknown";
            }
            density = densityValue + " (" + densityBucket + ")";

            // Screen size in inches
            double x = sizePoint.x / metrics.xdpi;
            double y = sizePoint.y / metrics.ydpi;
            double sizeInches = Math.sqrt(x * x + y * y);
            size = String.format(Locale.getDefault(), "%.1f", sizeInches) + " inches";

            // Orientation
            int rotation = display.getRotation();
            switch (rotation) {
                case android.view.Surface.ROTATION_0:
                case android.view.Surface.ROTATION_180:
                    orientation = "Portrait";
                    break;
                case android.view.Surface.ROTATION_90:
                case android.view.Surface.ROTATION_270:
                    orientation = "Landscape";
                    break;
                default:
                    orientation = "Unknown";
                    break;
            }
        }

        return new ScreenInfo(size, resolution, density, orientation);
    }

    private CpuInfo getCpuInfo() {
        String model = "Unknown";
        String cores = "Unknown";
        String arch = "Unknown";
        String frequency = "Unknown";

        try {
            // CPU Model and Architecture
            model = Build.CPU_ABI != null ? Build.CPU_ABI : "Unknown";
            arch = System.getProperty("os.arch", "Unknown");

            // CPU Cores
            cores = String.valueOf(Runtime.getRuntime().availableProcessors());

            // CPU Frequency
            String freq = readCpuFreq();
            if (freq != null && !freq.isEmpty()) {
                try {
                    long freqLong = Long.parseLong(freq);
                    if (freqLong > 1000000) {
                        frequency = String.format(Locale.getDefault(), "%.2f GHz", freqLong / 1000000000.0);
                    } else {
                        frequency = String.format(Locale.getDefault(), "%.0f MHz", freqLong / 1000000.0);
                    }
                } catch (NumberFormatException e) {
                    frequency = freq;
                }
            }
        } catch (Exception e) {
            // Handle any exceptions
        }

        return new CpuInfo(model, cores, arch, frequency);
    }

    private String readCpuFreq() {
        try {
            // Read current frequency for CPU 0
            File cpuFreqFile = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            if (cpuFreqFile.exists()) {
                return readFileFirstLine(cpuFreqFile.getAbsolutePath());
            } else {
                // Try alternative path
                cpuFreqFile = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
                if (cpuFreqFile.exists()) {
                    return readFileFirstLine(cpuFreqFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            // Handle exceptions
        }
        return null;
    }

    private String readFileFirstLine(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private String getSupportedAbis() {
        if (Build.SUPPORTED_ABIS != null) {
            StringBuilder abis = new StringBuilder();
            abis.append("[");
            for (int i = 0; i < Build.SUPPORTED_ABIS.length; i++) {
                if (i > 0) abis.append(", ");
                abis.append(Build.SUPPORTED_ABIS[i]);
            }
            abis.append("]");
            return abis.toString();
        }
        return "Unknown";
    }

    private String getOpenglEsVersion() {
        ActivityManager activityManager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            return String.valueOf(activityManager.getDeviceConfigurationInfo().reqGlEsVersion);
        }
        return "Unknown";
    }

    private String capitalize(String value) {
        if (value == null || value.length() == 0) {
            return "Unknown";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    // Data classes for device information
    public static class DeviceInfo {
        // Basic Device Information
        public final String manufacturer;
        public final String model;
        public final String brand;
        public final String androidVersion;
        public final int sdkInt;
        public final String buildNumber;

        // Hardware Information
        public final String board;
        public final String bootloader;
        public final String hardware;
        public final String product;
        public final String radioVersion;
        public final String fingerprint;

        // Storage Information
        public final StorageInfo storage;
        public final StorageInfo internalStorage;
        public final StorageInfo externalStorage;

        // Memory Information
        public final MemoryInfo memory;
        public final long memoryThreshold;
        public final String memoryClass;

        // Network Information
        public final NetworkInfoDetails network;

        // Screen Information
        public final ScreenInfo screen;

        // CPU Information
        public final CpuInfo cpu;

        // Capabilities
        public final String supportedAbis;
        public final String glEsVersion;

        public DeviceInfo(
                String manufacturer,
                String model,
                String brand,
                String androidVersion,
                int sdkInt,
                String buildNumber,
                String board,
                String bootloader,
                String hardware,
                String product,
                String radioVersion,
                String fingerprint,
                StorageInfo storage,
                StorageInfo internalStorage,
                StorageInfo externalStorage,
                MemoryInfo memory,
                long memoryThreshold,
                String memoryClass,
                NetworkInfoDetails network,
                ScreenInfo screen,
                CpuInfo cpu,
                String supportedAbis,
                String glEsVersion) {
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
            this.storage = storage;
            this.internalStorage = internalStorage;
            this.externalStorage = externalStorage;
            this.memory = memory;
            this.memoryThreshold = memoryThreshold;
            this.memoryClass = memoryClass;
            this.network = network;
            this.screen = screen;
            this.cpu = cpu;
            this.supportedAbis = supportedAbis;
            this.glEsVersion = glEsVersion;
        }
    }

    public static class StorageInfo {
        public final String freeReadable;
        public final String totalReadable;

        public StorageInfo(String freeReadable, String totalReadable) {
            this.freeReadable = freeReadable;
            this.totalReadable = totalReadable;
        }
    }

    public static class MemoryInfo {
        public final String freeReadable;
        public final String totalReadable;

        public MemoryInfo(String freeReadable, String totalReadable) {
            this.freeReadable = freeReadable;
            this.totalReadable = totalReadable;
        }
    }

    public static class NetworkInfoDetails {
        public final String type;
        public final String carrier;
        public final String ipAddress;
        public final String macAddress;

        public NetworkInfoDetails(String type, String carrier, String ipAddress, String macAddress) {
            this.type = type;
            this.carrier = carrier;
            this.ipAddress = ipAddress;
            this.macAddress = macAddress;
        }
    }

    public static class ScreenInfo {
        public final String size;
        public final String resolution;
        public final String density;
        public final String orientation;

        public ScreenInfo(String size, String resolution, String density, String orientation) {
            this.size = size;
            this.resolution = resolution;
            this.density = density;
            this.orientation = orientation;
        }
    }

    public static class CpuInfo {
        public final String model;
        public final String cores;
        public final String arch;
        public final String frequency;

        public CpuInfo(String model, String cores, String arch, String frequency) {
            this.model = model;
            this.cores = cores;
            this.arch = arch;
            this.frequency = frequency;
        }
    }
}