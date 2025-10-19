package com.faisal.protoolkit.ui.tools.document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentEditorBinding;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.ui.tools.document.viewmodels.EditorViewModel;
import com.faisal.protoolkit.util.RenderEngine;

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
            // Handle error - for now just go back
            requireActivity().onBackPressed();
            return;
        }
        
        // Initialize database and render engine
        AppDatabase database = AppDatabase.getDatabase(requireContext());
        RenderEngine renderEngine = new RenderEngine(requireContext());
        
        // Create ViewModel with proper factory
        viewModel = new ViewModelProvider(this, new EditorViewModel.Factory(database, renderEngine))
                .get(EditorViewModel.class);
        viewModel.setDocumentId(documentId);
        viewModel.setPageIndex(pageIndex);
        
        // For now, just show a simple message since actual functionality is in the document scanner
        // In the future, we can extend this to have proper editing features
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}