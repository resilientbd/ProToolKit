package com.example.protoolkit.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Provides shared executors for background work.
 */
public final class AppExecutors {

    private static final ExecutorService IO_EXECUTOR = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = new ScheduledThreadPoolExecutor(1);

    private AppExecutors() {
        // No instances.
    }

    public static Executor io() {
        return IO_EXECUTOR;
    }

    public static ScheduledExecutorService scheduler() {
        return SCHEDULED_EXECUTOR;
    }
}
