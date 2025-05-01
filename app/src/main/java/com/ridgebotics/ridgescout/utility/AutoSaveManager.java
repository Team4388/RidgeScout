package com.ridgebotics.ridgescout.utility;

import android.os.Handler;
import android.os.Looper;

public class AutoSaveManager {
    private static final long AUTO_SAVE_DELAY = 2000; // 2 seconds

    private final Handler handler;
    private final Runnable autoSaveRunnable;
    private boolean isAutoSaveScheduled = false;
    private final AutoSaveFunction autoSaveFunction;
    public boolean isRunning = false;

    // Functional interface for the auto-save function
    @FunctionalInterface
    public interface AutoSaveFunction {
        void save();
    }

    public AutoSaveManager(AutoSaveFunction autoSaveFunction) {
        this.autoSaveFunction = autoSaveFunction;
        handler = new Handler(Looper.getMainLooper());
        autoSaveRunnable = new Runnable() {
            @Override
            public void run() {
                performAutoSave();
                isAutoSaveScheduled = false;
            }
        };
    }

    public void start() {
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacks(autoSaveRunnable);
        isAutoSaveScheduled = false;
    }

    public void update() {
        if (!isRunning) {
            return;  // Don't schedule auto-saves when not running
        }

        // Cancel any previously scheduled auto-save
        handler.removeCallbacks(autoSaveRunnable);

        // Schedule a new auto-save
        handler.postDelayed(autoSaveRunnable, AUTO_SAVE_DELAY);
        isAutoSaveScheduled = true;
    }

    private void performAutoSave() {
        if (isRunning) {
            // Call the provided auto-save function
            autoSaveFunction.save();
        }
    }

    public void onDestroy() {
        // Remove any pending auto-save tasks when the activity or fragment is destroyed
        stop();
    }

    public boolean isAutoSaveScheduled() {
        return isAutoSaveScheduled;
    }

    public boolean isRunning() {
        return isRunning;
    }
}