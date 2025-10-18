package com.faisal.protoolkit.data.network;

import androidx.annotation.NonNull;

import com.faisal.protoolkit.util.AppConstants;
import com.faisal.protoolkit.util.AppExecutors;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Performs lightweight network checks such as HTTP latency measurements.
 */
public class NetworkRepository {

    public interface LatencyCallback {
        void onSuccess(int latencyMs);
        void onError(String message);
    }

    public void measureLatency(@NonNull String targetUrl, @NonNull LatencyCallback callback) {
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
}
