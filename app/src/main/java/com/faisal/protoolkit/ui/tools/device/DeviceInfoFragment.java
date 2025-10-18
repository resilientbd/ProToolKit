package com.faisal.protoolkit.ui.tools.device;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentDeviceInfoBinding;
import com.faisal.protoolkit.domain.model.DeviceInfo;
import com.faisal.protoolkit.ui.base.BaseFragment;
import com.faisal.protoolkit.util.HapticHelper;
import com.faisal.protoolkit.util.ServiceLocator;

/**
 * Displays comprehensive device information in a professional overview.
 */
public class DeviceInfoFragment extends BaseFragment {

    private FragmentDeviceInfoBinding binding;
    private DeviceInfoViewModel viewModel;

    public DeviceInfoFragment() {
        super(R.layout.fragment_device_info);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentDeviceInfoBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(DeviceInfoViewModel.class);
        binding.buttonRefresh.setOnClickListener(v -> {
            viewModel.refresh();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        observeData();
    }

    private void observeData() {
        // Observe basic device information
        observe(viewModel.getDeviceInfo(), this::renderDeviceInfo);
        observe(viewModel.isLoading(), loading -> binding.progressIndicator.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
    }

    private void renderDeviceInfo(@NonNull DeviceInfo info) {
        // Basic Device Information
        binding.textManufacturer.setText(String.format("Manufacturer: %s", info.getManufacturer()));
        binding.textModel.setText(String.format("Model: %s", info.getModel()));
        binding.textBrand.setText(String.format("Brand: %s", info.getBrand()));
        binding.textOsVersion.setText(String.format("Android %s (SDK %d)", info.getAndroidVersion(), info.getSdkInt()));
        binding.textBuildNumber.setText(String.format("Build: %s", info.getBuildNumber()));

        // Hardware Information
        binding.textBoard.setText(String.format("Board: %s", info.getBoard()));
        binding.textBootloader.setText(String.format("Bootloader: %s", info.getBootloader()));
        binding.textHardware.setText(String.format("Hardware: %s", info.getHardware()));
        binding.textProduct.setText(String.format("Product: %s", info.getProduct()));
        binding.textRadioVersion.setText(String.format("Radio Version: %s", info.getRadioVersion()));
        binding.textFingerprint.setText(String.format("Fingerprint: %s", info.getFingerprint()));

        // Storage Information
        binding.textStorage.setText(String.format("Total Storage: %s", info.getStorageTotal()));
        binding.textStorageFree.setText(String.format("Free Storage: %s", info.getStorageFree()));
        binding.textInternalStorage.setText(String.format("Internal Storage: %s", 
            info.getInternalStorageTotal().equals("Unknown") ? "Not available" : info.getInternalStorageTotal()));
        binding.textExternalStorage.setText(String.format("External Storage: %s", 
            info.getExternalStorageTotal().equals("Unknown") ? "Not available" : info.getExternalStorageTotal()));

        // Memory Information
        binding.textMemoryTotal.setText(String.format("Total RAM: %s", info.getMemoryTotal()));
        binding.textMemoryFree.setText(String.format("Available RAM: %s", info.getMemoryFree()));
        binding.textMemoryClass.setText(String.format("Memory Class: %s", info.getMemoryClass()));
        binding.textMemoryThreshold.setText(String.format("Low Memory Threshold: %s", info.getMemoryThreshold()));

        // Network Information
        binding.textNetworkType.setText(String.format("Network Type: %s", info.getNetworkType()));
        binding.textNetworkCarrier.setText(String.format("Carrier: %s", info.getNetworkCarrier()));
        binding.textIpAddress.setText(String.format("IP Address: %s", info.getIpAddress()));
        binding.textMacAddress.setText(String.format("MAC Address: %s", info.getMacAddress()));

        // Screen Information
        binding.textScreenSize.setText(String.format("Screen Size: %s", info.getScreenSize()));
        binding.textScreenResolution.setText(String.format("Resolution: %s", info.getScreenResolution()));
        binding.textScreenDensity.setText(String.format("Density: %s", info.getScreenDensity()));
        binding.textScreenOrientation.setText(String.format("Orientation: %s", info.getScreenOrientation()));

        // CPU Information
        binding.textCpuModel.setText(String.format("Model: %s", info.getCpuModel()));
        binding.textCpuCores.setText(String.format("Cores: %s", info.getCpuCores()));
        binding.textCpuArch.setText(String.format("Architecture: %s", info.getCpuArch()));
        binding.textCpuFrequency.setText(String.format("Frequency: %s", info.getCpuFrequency()));

        // Capabilities
        binding.textSupportedAbis.setText(String.format("ABIs: %s", info.getSupportedAbis()));
        binding.textGlEsVersion.setText(String.format("OpenGL ES: %s", info.getGlEsVersion()));
    }
}