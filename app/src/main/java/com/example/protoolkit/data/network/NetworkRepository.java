package com.example.protoolkit.data.network;

import com.example.protoolkit.util.AppConstants;
import com.example.protoolkit.util.AppExecutors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * Performs comprehensive network tests including latency, speed, and connectivity.
 */
public class NetworkRepository {

    public interface LatencyCallback {
        void onSuccess(int latencyMs);
        void onError(String message);
    }

    public interface SpeedTestCallback {
        void onProgress(int progressPercent);
        void onComplete(long downloadSpeedKBps);
        void onError(String message);
    }

    public interface ConnectionCallback {
        void onSuccess(NetworkInfo info);
        void onError(String message);
    }

    public static class NetworkInfo {
        public final String ipAddress;
        public final String networkType;
        public final String carrierName;
        public final boolean isVpn;
        public final String dnsServer;
        
        public NetworkInfo(String ipAddress, String networkType, String carrierName, boolean isVpn, String dnsServer) {
            this.ipAddress = ipAddress;
            this.networkType = networkType;
            this.carrierName = carrierName;
            this.isVpn = isVpn;
            this.dnsServer = dnsServer;
        }
    }

    public void measureLatency(final String targetUrl, final LatencyCallback callback) {
        AppExecutors.io().execute(() -> {
            HttpURLConnection connection = null;
            long start = System.currentTimeMillis();
            try {
                URL url = new URL(targetUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(AppConstants.NETWORK_TIMEOUT_MS);
                connection.setReadTimeout(AppConstants.NETWORK_TIMEOUT_MS);
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 400) {
                    int latency = (int) (System.currentTimeMillis() - start);
                    callback.onSuccess(latency);
                } else {
                    callback.onError("Response code: " + responseCode);
                }
            } catch (IOException exception) {
                callback.onError(exception.getLocalizedMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void performSpeedTest(final String testUrl, final SpeedTestCallback callback) {
        AppExecutors.io().execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(testUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(AppConstants.NETWORK_TIMEOUT_MS);
                connection.setReadTimeout(AppConstants.NETWORK_TIMEOUT_MS * 5); // Longer timeout for speed test
                
                long startTime = System.currentTimeMillis();
                long totalBytesRead = 0;
                
                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytesRead += bytesRead;
                        
                        // Calculate progress based on expected download size or time
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - startTime > 1000) { // Update progress every second
                            callback.onProgress((int) ((currentTime - startTime) / 100)); // Simplified progress
                        }
                        
                        // Stop after 5 seconds or if we've downloaded enough data
                        if (currentTime - startTime > 5000 || totalBytesRead > 5 * 1024 * 1024) {
                            break;
                        }
                    }
                }
                
                long durationMs = System.currentTimeMillis() - startTime;
                if (durationMs > 0) {
                    long speedBytesPerSec = (totalBytesRead * 1000) / durationMs;
                    long speedKbPerSec = speedBytesPerSec / 1024;
                    callback.onComplete(speedKbPerSec);
                } else {
                    callback.onComplete(0);
                }
                
            } catch (IOException e) {
                callback.onError(e.getLocalizedMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void checkConnection(final ConnectionCallback callback) {
        AppExecutors.io().execute(() -> {
            try {
                // Get IP address
                InetAddress inetAddress = InetAddress.getByName("8.8.8.8");
                String ipAddress = inetAddress.getHostAddress();
                
                // For a real implementation, we'd use ConnectivityManager and TelephonyManager
                // This is a simplified version 
                NetworkInfo info = new NetworkInfo(
                    ipAddress != null ? ipAddress : "Unknown",
                    "WiFi/Ethernet",  // Would be determined by ConnectivityManager
                    "Carrier",        // Would be determined by TelephonyManager
                    false,            // Would be determined by network capabilities
                    "8.8.8.8"         // Default DNS
                );
                
                callback.onSuccess(info);
            } catch (Exception e) {
                callback.onError(e.getLocalizedMessage());
            }
        });
    }

    public void pingHost(final String host, final LatencyCallback callback) {
        AppExecutors.io().execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                InetAddress address = InetAddress.getByName(host);
                boolean reachable = address.isReachable(5000); // 5 second timeout
                
                long pingTime = System.currentTimeMillis() - startTime;
                
                if (reachable) {
                    callback.onSuccess((int) pingTime);
                } else {
                    callback.onError("Host not reachable");
                }
            } catch (Exception e) {
                callback.onError(e.getLocalizedMessage());
            }
        });
    }
}
