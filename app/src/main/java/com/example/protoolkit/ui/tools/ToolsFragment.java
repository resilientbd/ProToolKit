package com.example.protoolkit.ui.tools;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.protoolkit.R;
import com.example.protoolkit.ads.AdsManager;
import com.example.protoolkit.databinding.FragmentToolsBinding;
import com.example.protoolkit.domain.model.ToolItem;
import com.example.protoolkit.ui.base.BaseFragment;
import com.example.protoolkit.ui.home.ToolAdapter;
import com.example.protoolkit.util.HapticHelper;
import com.example.protoolkit.util.ServiceLocator;

/**
 * Tools screen listing every available utility.
 */
public class ToolsFragment extends BaseFragment {

    private FragmentToolsBinding binding;
    private ToolAdapter adapter;
    private ToolsViewModel viewModel;

    public ToolsFragment() {
        super(R.layout.fragment_tools);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentToolsBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(ToolsViewModel.class);
        setupRecycler();
        observe(viewModel.getTools(), tools -> adapter.submitList(tools));
    }

    private void setupRecycler() {
        adapter = new ToolAdapter(this::handleToolClick);
        binding.toolsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.toolsRecycler.setAdapter(adapter);
    }

    private void handleToolClick(@NonNull ToolItem item) {
        if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
            HapticHelper.vibrate(requireContext());
        }
        AdsManager.getInstance().showInterstitialIfAvailable(requireActivity(), () ->
                NavHostFragment.findNavController(this).navigate(item.getDestinationId()));
    }
}
