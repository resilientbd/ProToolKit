package com.faisal.protoolkit.ui.tools.network;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentNetworkToolsBinding;
import com.faisal.protoolkit.ui.base.BaseFragment;
import com.faisal.protoolkit.util.HapticHelper;
import com.faisal.protoolkit.util.ServiceLocator;

/**
 * Fragment that provides comprehensive network tools including all advanced features.
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
        setupButtons();
        observeData();
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

    private void setupButtons() {
        // Basic network test buttons
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

        // Advanced network test buttons
        binding.buttonDnsLookup.setOnClickListener(v -> {
            viewModel.performDnsLookup();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        
        binding.buttonPortScan.setOnClickListener(v -> {
            viewModel.performPortScan();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        
        binding.buttonTraceroute.setOnClickListener(v -> {
            viewModel.performTraceroute();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        
        binding.buttonNetworkDiagnostics.setOnClickListener(v -> {
            viewModel.runFullDiagnostics();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });

        // File management action buttons
        binding.buttonCleanCache.setOnClickListener(v -> {
            viewModel.cleanCache();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        
        binding.buttonClearDownloads.setOnClickListener(v -> {
            viewModel.clearDownloads();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        
        binding.buttonBackupMedia.setOnClickListener(v -> {
            viewModel.backupMedia();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
    }

    private void observeData() {
        // Observe basic network test results
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
        
        // Observe advanced network test results
        observe(viewModel.getDnsLookupResult(), result -> {
            if (result != null) {
                binding.textResult.setText("DNS Lookup Result:\n" + result);
            }
        });
        
        observe(viewModel.getPortScanResult(), result -> {
            if (result != null) {
                binding.textResult.setText("Port Scan Result:\n" + result);
            }
        });
        
        observe(viewModel.getTracerouteResult(), result -> {
            if (result != null) {
                binding.textResult.setText("Traceroute Result:\n" + result);
            }
        });
        
        observe(viewModel.getDiagnosticsResult(), result -> {
            if (result != null) {
                binding.textResult.setText("Diagnostics Result:\n" + result);
            }
        });
        
        // Observe speed test progress
        observe(viewModel.getSpeedTestProgress(), progress -> {
            if (progress != null) {
                binding.progressIndicator.setProgress(progress);
            }
        });
        
        // Observe loading state
        observe(viewModel.inProgress(), inProgress -> {
            binding.progressIndicator.setVisibility(Boolean.TRUE.equals(inProgress) ? View.VISIBLE : View.GONE);
        });
        
        // Observe current test name
        observe(viewModel.getCurrentTest(), test -> {
            if (test != null && !test.isEmpty()) {
                binding.textCurrentTest.setText("Running: " + test);
                binding.textCurrentTest.setVisibility(View.VISIBLE);
            } else {
                binding.textCurrentTest.setVisibility(View.GONE);
            }
        });
        
        // Observe error messages
        observe(viewModel.getErrorMessage(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.network_test_failure) + " " + message, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Messages are handled by the postMessage method in ViewModel which logs to console
        // For a production app, we would implement a proper message LiveData in the ViewModel
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}