package com.faisal.protoolkit.ui.tools.document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.faisal.protoolkit.R;
import com.faisal.protoolkit.databinding.FragmentDocumentDetailBinding;
import com.faisal.protoolkit.data.database.AppDatabase;
import com.faisal.protoolkit.ui.tools.document.adapters.PageAdapter;
import com.faisal.protoolkit.ui.tools.document.viewmodels.DocumentDetailViewModel;

public class DocumentDetailFragment extends Fragment {
    private FragmentDocumentDetailBinding binding;
    private DocumentDetailViewModel viewModel;
    private PageAdapter adapter;

    public static DocumentDetailFragment newInstance(String documentId) {
        DocumentDetailFragment fragment = new DocumentDetailFragment();
        Bundle args = new Bundle();
        args.putString("document_id", documentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDocumentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        String documentId = getArguments() != null ? getArguments().getString("document_id") : null;
        if (documentId == null) {
            // Handle error - documentId is required
            return;
        }
        
        AppDatabase database = AppDatabase.getDatabase(requireContext());
        viewModel = new ViewModelProvider(this, new DocumentDetailViewModel.Factory(database))
                .get(DocumentDetailViewModel.class);
        viewModel.setDocumentId(documentId);
        
        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupButtons();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        
        // Set document title
        viewModel.getDocument().observe(getViewLifecycleOwner(), document -> {
            if (document != null) {
                binding.toolbar.setTitle(document.title);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new PageAdapter(viewModel, page -> {
            // Navigate to editor for this page
            // TODO: Implement navigation to EditorFragment
        });
        
        binding.pagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        binding.pagesRecyclerView.setAdapter(adapter);
        
        // Setup drag and drop for reordering
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = 0; // No swipe for pages
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // Handle item move
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                viewModel.reorderPage(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe for pages
            }
        });
        
        itemTouchHelper.attachToRecyclerView(binding.pagesRecyclerView);
    }

    private void setupObservers() {
        viewModel.getPages().observe(getViewLifecycleOwner(), pages -> {
            adapter.submitList(pages);
        });
    }

    private void setupButtons() {
        binding.fabAddPage.setOnClickListener(v -> {
            // Add a new page to the document
            // TODO: Implement adding new page
        });
        
        binding.btnExport.setOnClickListener(v -> {
            // Show export options
            // TODO: Implement export functionality
        });
        
        binding.btnShare.setOnClickListener(v -> {
            // Share the document
            // TODO: Implement share functionality
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
