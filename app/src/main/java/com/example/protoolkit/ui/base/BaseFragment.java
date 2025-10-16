package com.example.protoolkit.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.protoolkit.util.AnalyticsLogger;
import com.example.protoolkit.util.ServiceLocator;

/**
 * Base fragment offering analytics logging helpers.
 */
public abstract class BaseFragment extends Fragment {

    protected final AnalyticsLogger analyticsLogger = ServiceLocator.getAnalyticsLogger();

    public BaseFragment(int layoutId) {
        super(layoutId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logScreen();
    }

    protected <T> void observe(@NonNull LiveData<T> liveData, @NonNull Observer<T> observer) {
        liveData.observe(getViewLifecycleOwner(), observer);
    }

    protected void logScreen() {
        analyticsLogger.logScreen(getClass().getSimpleName());
    }
}
