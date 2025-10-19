package com.faisal.protoolkit.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.faisal.protoolkit.util.FileManager;
import java.io.File;

public class CleanCacheWorker extends Worker {
    private static final String TAG = "CleanCacheWorker";
    
    public static final String MAX_BYTES_KEY = "max_bytes";
    public static final long DEFAULT_CACHE_QUOTA = 150 * 1024 * 1024; // 150MB
    
    public CleanCacheWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long maxBytes = getInputData().getLong(MAX_BYTES_KEY, DEFAULT_CACHE_QUOTA);

        try {
            FileManager fileManager = new FileManager(getApplicationContext());
            
            boolean success = fileManager.cleanCache(maxBytes);
            
            if (success) {
                Log.d(TAG, "Cache cleanup completed successfully");
                return Result.success();
            } else {
                Log.w(TAG, "Cache cleanup partially failed");
                return Result.failure();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during cache cleanup", e);
            return Result.failure();
        }
    }
}