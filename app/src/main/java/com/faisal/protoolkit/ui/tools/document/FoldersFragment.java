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
import com.faisal.protoolkit.databinding.FragmentFoldersBinding;
import com.faisal.protoolkit.ui.tools.document.adapters.FolderAdapter;
import com.faisal.protoolkit.ui.tools.document.viewmodels.FoldersViewModel;

public class FoldersFragment extends Fragment {
    private FragmentFoldersBinding binding;
    private FoldersViewModel viewModel;
    private FolderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFoldersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(FoldersViewModel.class);
        
        setupRecyclerView();
        setupObservers();
        setupButtons();
    }

    private void setupRecyclerView() {
        adapter = new FolderAdapter(folder -> {
            // Handle folder selection
            // TODO: Navigate to documents in selected folder
        });
        
        binding.foldersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.foldersRecyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getFolders().observe(getViewLifecycleOwner(), folders -> {
            adapter.submitList(folders);
        });
    }

    private void setupButtons() {
        binding.fabNewFolder.setOnClickListener(v -> {
            // Create a new folder
            // TODO: Implement new folder creation
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}