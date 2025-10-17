package com.example.protoolkit.ui.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.protoolkit.R;
import com.example.protoolkit.databinding.FragmentDeveloperSettingsBinding;
import com.example.protoolkit.ui.base.BaseFragment;
import com.example.protoolkit.util.HapticHelper;
import com.example.protoolkit.util.ServiceLocator;

/**
 * Developer settings for testing and debugging.
 */
public class DeveloperSettingsFragment extends BaseFragment {

    private FragmentDeveloperSettingsBinding binding;
    private DeveloperSettingsViewModel viewModel;

    public DeveloperSettingsFragment() {
        super(R.layout.fragment_developer_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentDeveloperSettingsBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(DeveloperSettingsViewModel.class);

        setupListeners();
        observeData();
    }

    private void setupListeners() {
        binding.switchDevMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDeveloperModeEnabled(isChecked);
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });

        binding.switchSandbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setSandboxMode(isChecked);
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });

        binding.switchDebugLogging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDebugLoggingEnabled(isChecked);
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });

        binding.buttonTestPurchase.setOnClickListener(v -> {
            viewModel.testPurchaseFlow();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });

        binding.buttonResetPurchases.setOnClickListener(v -> {
            viewModel.resetPurchaseHistory();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
    }

    private void observeData() {
        observe(viewModel.isDeveloperModeEnabled(), enabled -> {
            binding.switchDevMode.setChecked(enabled);
            binding.cardSandbox.setVisibility(enabled ? View.VISIBLE : View.GONE);
            binding.cardTestPurchases.setVisibility(enabled ? View.VISIBLE : View.GONE);
        });

        observe(viewModel.isSandboxMode(), sandbox -> binding.switchSandbox.setChecked(sandbox));
        observe(viewModel.isDebugLoggingEnabled(), logging -> binding.switchDebugLogging.setChecked(logging));
    }
}