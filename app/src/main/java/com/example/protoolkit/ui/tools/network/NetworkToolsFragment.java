package com.example.protoolkit.ui.tools.network;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.protoolkit.R;
import com.example.protoolkit.databinding.FragmentNetworkToolsBinding;
import com.example.protoolkit.ui.base.BaseFragment;
import com.example.protoolkit.util.HapticHelper;
import com.example.protoolkit.util.ServiceLocator;

/**
 * Fragment that runs comprehensive network tests.
 */
public class NetworkToolsFragment extends BaseFragment {

    private FragmentNetworkToolsBinding binding;
    private NetworkToolsViewModel viewModel;

    public NetworkToolsFragment() {
        super(R.layout.fragment_network_tools);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentNetworkToolsBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(NetworkToolsViewModel.class);
        
        setupInput();
        
        // Set up all test buttons
        binding.buttonPing.setOnClickListener(v -> {
            viewModel.measureLatency();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        
        binding.buttonSpeedTest.setOnClickListener(v -> {
            viewModel.performSpeedTest();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        
        binding.buttonConnectionCheck.setOnClickListener(v -> {
            viewModel.checkConnection();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        
        binding.buttonPingHost.setOnClickListener(v -> {
            viewModel.pingHost();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });

        // Observe results
        observe(viewModel.getLatencyMs(), latency -> {
            if (latency == null) {
                binding.textResult.setText(R.string.label_latency_error);
            } else {
                binding.textResult.setText(getString(R.string.label_latency_result, latency));
            }
        });
        
        observe(viewModel.getDownloadSpeed(), speed -> {
            if (speed != null) {
                binding.textResult.append("\nDownload Speed: " + speed + " KB/s");
            }
        });
        
        observe(viewModel.getConnectionInfo(), info -> {
            if (info != null) {
                String connInfo = "IP: " + info.ipAddress + 
                                "\nType: " + info.networkType + 
                                "\nCarrier: " + info.carrierName;
                binding.textResult.setText(connInfo);
            }
        });
        
        observe(viewModel.getSpeedTestProgress(), progress -> {
            if (progress != null) {
                binding.progressIndicator.setProgress(progress);
            }
        });
        
        observe(viewModel.inProgress(), progress -> {
            binding.progressIndicator.setVisibility(Boolean.TRUE.equals(progress) ? View.VISIBLE : View.GONE);
        });
        
        observe(viewModel.getErrorMessage(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.network_test_failure) + " " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupInput() {
        binding.inputTarget.setText(viewModel.getTargetUrl().getValue());
        binding.inputTarget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setTargetUrl(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}