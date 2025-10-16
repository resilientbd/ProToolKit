package com.example.protoolkit.ui.tools.file;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.protoolkit.R;
import com.example.protoolkit.databinding.FragmentFileToolsBinding;
import com.example.protoolkit.ui.base.BaseFragment;
import com.example.protoolkit.util.HapticHelper;
import com.example.protoolkit.util.ServiceLocator;

/**
 * Shows storage summary and safe cleaner suggestions.
 */
public class FileToolsFragment extends BaseFragment {

    private FragmentFileToolsBinding binding;
    private FileToolsViewModel viewModel;
    private FileSuggestionAdapter adapter;

    public FileToolsFragment() {
        super(R.layout.fragment_file_tools);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentFileToolsBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(FileToolsViewModel.class);
        adapter = new FileSuggestionAdapter();
        binding.suggestionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.suggestionList.setAdapter(adapter);
        
        // Set up button click listeners
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
        
        observe(viewModel.getStorageSummary(), summary -> binding.textStorageSummary.setText(summary));
        observe(viewModel.getSuggestions(), adapter::submitList);
        observe(viewModel.isLoading(), loading -> binding.progressIndicator.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
        
        // Observe detailed storage information
        observe(viewModel.getAppsDataSize(), size -> binding.textAppsDataSize.setText(size));
        observe(viewModel.getImagesSize(), size -> binding.textImagesSize.setText(size));
        observe(viewModel.getVideosSize(), size -> binding.textVideosSize.setText(size));
        observe(viewModel.getAudioSize(), size -> binding.textAudioSize.setText(size));
        observe(viewModel.getDocumentsSize(), size -> binding.textDocumentsSize.setText(size));
        observe(viewModel.getDownloadsSize(), size -> binding.textDownloadsSize.setText(size));
        
        // Observe storage progress
        observe(viewModel.getStorageProgress(), progress -> binding.storageProgress.setProgress(progress));
    }
}