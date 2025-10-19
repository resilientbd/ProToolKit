package com.faisal.protoolkit.ui.tools.document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentEditorBinding;
import com.faisal.protoolkit.model.EditOps;
import com.faisal.protoolkit.ui.tools.document.viewmodels.EditorViewModel;

public class EditorFragment extends Fragment {
    private FragmentEditorBinding binding;
    private EditorViewModel viewModel;

    public static EditorFragment newInstance(String documentId, int pageIndex) {
        EditorFragment fragment = new EditorFragment();
        Bundle args = new Bundle();
        args.putString("document_id", documentId);
        args.putInt("page_index", pageIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        String documentId = getArguments() != null ? getArguments().getString("document_id") : null;
        int pageIndex = getArguments() != null ? getArguments().getInt("page_index") : -1;
        
        if (documentId == null || pageIndex < 0) {
            // Handle error
            return;
        }
        
        viewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        viewModel.setDocumentId(documentId);
        viewModel.setPageIndex(pageIndex);
        
        setupPreviewImage();
        setupFilterControls();
        setupRotationControls();
        setupSaveButton();
    }

    private void setupPreviewImage() {
        // Load and display the current page preview
        viewModel.getPagePreview().observe(getViewLifecycleOwner(), bitmap -> {
            if (bitmap != null) {
                binding.imageViewPreview.setImageBitmap(bitmap);
            }
        });
    }

    private void setupFilterControls() {
        // Set up filter mode selector
        binding.spinnerFilterMode.setSelection(0); // Default to ORIGINAL
        binding.spinnerFilterMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String[] modes = {"ORIGINAL", "GRAY", "BW", "COLOR_BOOST"};
                if (position >= 0 && position < modes.length) {
                    viewModel.updateFilterMode(modes[position]);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Set up contrast seekbar
        binding.seekbarContrast.setProgress(50); // 1.0 (middle value for 0.0-2.0 range)
        binding.seekbarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Convert 0-100 to 0.0-2.0
                float contrast = progress / 50.0f;
                viewModel.updateContrast(contrast);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set up brightness seekbar
        binding.seekbarBrightness.setProgress(50); // 0.0 (middle value for -1.0 to 1.0 range)
        binding.seekbarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Convert 0-100 to -1.0 to 1.0
                float brightness = (progress - 50) / 50.0f;
                viewModel.updateBrightness(brightness);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set up sharpen seekbar
        binding.seekbarSharpen.setProgress(0); // 0.0
        binding.seekbarSharpen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Convert 0-100 to 0.0-1.0
                float sharpen = progress / 100.0f;
                viewModel.updateSharpen(sharpen);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupRotationControls() {
        binding.btnRotateLeft.setOnClickListener(v -> {
            viewModel.rotateLeft();
        });
        
        binding.btnRotateRight.setOnClickListener(v -> {
            viewModel.rotateRight();
        });
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            // Save current edits
            viewModel.saveEdits();
            requireActivity().onBackPressed();
        });
        
        binding.btnCancel.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}