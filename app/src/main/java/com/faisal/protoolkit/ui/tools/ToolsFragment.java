package com.faisal.protoolkit.ui.tools;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.ads.AdsManager;
import com.faisal.protoolkit.databinding.FragmentToolsBinding;
import com.faisal.protoolkit.domain.model.ToolItem;
import com.faisal.protoolkit.ui.base.BaseFragment;
import com.faisal.protoolkit.ui.home.ToolAdapter;
import com.faisal.protoolkit.util.HapticHelper;
import com.faisal.protoolkit.util.ServiceLocator;

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
        AdsManager.getInstance(requireContext()).showInterstitialIfAvailable(requireActivity(), () ->
                NavHostFragment.findNavController(this).navigate(item.getDestinationId()));
    }
}
