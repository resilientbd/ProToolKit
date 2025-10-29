package com.faisal.protoolkit.ui.tools.document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentTrashBinding;
import com.faisal.protoolkit.ui.tools.document.adapters.DocumentAdapter;
import com.faisal.protoolkit.ui.tools.document.viewmodels.TrashViewModel;

public class TrashFragment extends Fragment {
    private FragmentTrashBinding binding;
    private TrashViewModel viewModel;
    private DocumentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(TrashViewModel.class);
        
        setupRecyclerView();
        setupObservers();
        setupButtons();
    }

    private void setupRecyclerView() {
        adapter = new DocumentAdapter(
            document -> {
                // View trashed document
                // TODO: Implement document viewing in trash
            },
            document -> {
                // Delete functionality not typically needed in trash fragment
                // Or implement permanent deletion
            }
        );
        
        binding.trashRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.trashRecyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getTrashedDocuments().observe(getViewLifecycleOwner(), documents -> {
            adapter.submitList(documents);
        });
    }

    private void setupButtons() {
        binding.btnEmptyTrash.setOnClickListener(v -> {
            // Empty the entire trash
            // TODO: Implement empty trash functionality
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}