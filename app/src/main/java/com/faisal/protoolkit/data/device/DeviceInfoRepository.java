package com.faisal.protoolkit.data.device;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.domain.model.DeviceInfo;
import com.faisal.protoolkit.util.FormatUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Collects comprehensive device information for display.
 */
public class DeviceInfoRepository {

    private final Application application;

    public DeviceInfoRepository(@NonNull Application application) {
        this.application = application;
    }

    @NonNull
    public DeviceInfo fetchDeviceInfo() {
        // Basic Device Information
        String manufacturer = safeGetString(() -> Build.MANUFACTURER, "Unknown");
        String model = safeGetString(() -> Build.MODEL, "Unknown");
        String brand = safeGetString(() -> Build.BRAND, "Unknown");
        String androidVersion = safeGetString(() -> Build.VERSION.RELEASE, "Unknown");
        int sdkInt = Build.VERSION.SDK_INT;
        String buildNumber = safeGetString(() -> Build.DISPLAY, "Unknown");

        // Hardware Information
        String board = safeGetString(() -> Build.BOARD, "Unknown");
        String bootloader = safeGetString(() -> Build.BOOTLOADER, "Unknown");
        String hardware = safeGetString(() -> Build.HARDWARE, "Unknown");
        String product = safeGetString(() -> Build.PRODUCT, "Unknown");
        String radioVersion = safeGetString(() -> Build.getRadioVersion(), "Unknown");
        String fingerprint = safeGetString(() -> Build.FINGERPRINT, "Unknown");

        // Storage Information
        StorageInfo storage = readStorage();
        StorageInfo internalStorage = readInternalStorage();
        StorageInfo externalStorage = readExternalStorage();

        // Memory Information
        MemoryInfo memory = readMemory();
        String memoryThreshold = FormatUtils.formatBytes(getMemoryThreshold());
        String memoryClass = getMemoryClass();

        // Network Information
        NetworkInfoDetails network = readNetworkInfo();

        // Screen Information
        ScreenInfo screen = readScreenInfo();

        // CPU Information
        CpuInfo cpu = readCpuInfo();

        // Capabilities
        String supportedAbis = getSupportedAbis();
        String glEsVersion = getOpenglEsVersion();

        return new DeviceInfo(
                // Basic Device Information
                capitalize(manufacturer), model, capitalize(brand),
                androidVersion, sdkInt, buildNumber,
                // Hardware Information
                board, bootloader, hardware, product, radioVersion, fingerprint,
                // Storage Information
                storage.freeReadable, storage.totalReadable,
                internalStorage.freeReadable, internalStorage.totalReadable,
                externalStorage.freeReadable, externalStorage.totalReadable,
                // Memory Information
                memory.freeReadable, memory.totalReadable,
                memoryThreshold, memoryClass,
                // Network Information
                network.type, network.carrier, network.ipAddress, network.macAddress,
                // Screen Information
                screen.size, screen.resolution, screen.density, screen.orientation,
                // CPU Information
                cpu.model, cpu.cores, cpu.arch, cpu.frequency,
                // Capabilities
                supportedAbis, glEsVersion
        );
    }

    private StorageInfo readStorage() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        long availableBlocks = statFs.getAvailableBlocksLong();

        long totalBytes = totalBlocks * blockSize;
        long freeBytes = availableBlocks * blockSize;

        return new StorageInfo(FormatUtils.formatBytes(freeBytes), FormatUtils.formatBytes(totalBytes));
    }

    private StorageInfo readInternalStorage() {
        File internalStorage = Environment.getDataDirectory();
        StatFs statFs = new StatFs(internalStorage.getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        long availableBlocks = statFs.getAvailableBlocksLong();

        long totalBytes = totalBlocks * blockSize;
        long freeBytes = availableBlocks * blockSize;

        return new StorageInfo(FormatUtils.formatBytes(freeBytes), FormatUtils.formatBytes(totalBytes));
    }

    private StorageInfo readExternalStorage() {
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

    private MemoryInfo readMemory() {
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

    private NetworkInfoDetails readNetworkInfo() {
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

    private ScreenInfo readScreenInfo() {
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

    private CpuInfo readCpuInfo() {
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

    private interface SafeStringSupplier {
        String get() throws Exception;
    }

    private String safeGetString(SafeStringSupplier supplier, String defaultValue) {
        try {
            String result = supplier.get();
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static class StorageInfo {
        final String freeReadable;
        final String totalReadable;

        StorageInfo(String freeReadable, String totalReadable) {
            this.freeReadable = freeReadable;
            this.totalReadable = totalReadable;
        }
    }

    private static class MemoryInfo {
        final String freeReadable;
        final String totalReadable;

        MemoryInfo(String freeReadable, String totalReadable) {
            this.freeReadable = freeReadable;
            this.totalReadable = totalReadable;
        }
    }

    private static class NetworkInfoDetails {
        final String type;
        final String carrier;
        final String ipAddress;
        final String macAddress;

        NetworkInfoDetails(String type, String carrier, String ipAddress, String macAddress) {
            this.type = type;
            this.carrier = carrier;
            this.ipAddress = ipAddress;
            this.macAddress = macAddress;
        }
    }

    private static class ScreenInfo {
        final String size;
        final String resolution;
        final String density;
        final String orientation;

        ScreenInfo(String size, String resolution, String density, String orientation) {
            this.size = size;
            this.resolution = resolution;
            this.density = density;
            this.orientation = orientation;
        }
    }

    private static class CpuInfo {
        final String model;
        final String cores;
        final String arch;
        final String frequency;

        CpuInfo(String model, String cores, String arch, String frequency) {
            this.model = model;
            this.cores = cores;
            this.arch = arch;
            this.frequency = frequency;
        }
    }
}