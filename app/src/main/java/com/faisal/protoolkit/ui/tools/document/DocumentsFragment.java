package com.faisal.protoolkit.ui.tools.document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentDocumentsBinding;
import com.faisal.protoolkit.ui.tools.document.adapters.DocumentAdapter;
import com.faisal.protoolkit.ui.tools.document.viewmodels.DocumentsViewModel;

public class DocumentsFragment extends Fragment {
    private FragmentDocumentsBinding binding;
    private DocumentsViewModel viewModel;
    private DocumentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDocumentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(DocumentsViewModel.class);
        
        setupRecyclerView();
        setupObservers();
        setupButtons();
    }

    private void setupRecyclerView() {
        adapter = new DocumentAdapter(document -> {
            // Navigate to document detail
            // TODO: Implement navigation to DocumentDetailFragment
        });
        
        binding.documentsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.documentsRecyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getDocuments().observe(getViewLifecycleOwner(), documents -> {
            adapter.submitList(documents);
        });
    }

    private void setupButtons() {
        binding.fabNewScan.setOnClickListener(v -> {
            // Navigate to scan screen
            // TODO: Implement navigation to scan screen
        });
        
        binding.fabImport.setOnClickListener(v -> {
            // Open file picker to import images
            // TODO: Implement import functionality
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}