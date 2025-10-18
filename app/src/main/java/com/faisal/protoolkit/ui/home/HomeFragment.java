package com.faisal.protoolkit.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faisal.protoolkit.R;
import com.faisal.protoolkit.ads.AdsManager;
import com.faisal.protoolkit.databinding.FragmentHomeBinding;
import com.faisal.protoolkit.domain.model.ToolItem;
import com.faisal.protoolkit.ui.base.BaseFragment;
import com.faisal.protoolkit.util.HapticHelper;
import com.faisal.protoolkit.util.ServiceLocator;

/**
 * Home screen displaying featured tools.
 */
public class HomeFragment extends BaseFragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ToolAdapter adapter;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentHomeBinding.bind(view);
        viewModel = new androidx.lifecycle.ViewModelProvider(
                this,
                ServiceLocator.getViewModelFactory()).get(HomeViewModel.class);
        setupRecyclerView();
        setupSearch();
        observe(viewModel.getVisibleTools(), tools -> adapter.submitList(tools));
        observe(viewModel.areAdsVisible(), visible -> {
            if (Boolean.TRUE.equals(visible)) {
                binding.bannerContainer.setVisibility(View.VISIBLE);
                AdsManager.getInstance(requireContext()).loadBanner(binding.bannerContainer);
            } else {
                binding.bannerContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshAdsState();
    }

    private void setupRecyclerView() {
        adapter = new ToolAdapter(this::onToolClicked);
        RecyclerView recyclerView = binding.recyclerTools;
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void onToolClicked(@NonNull ToolItem item) {
        if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
            HapticHelper.vibrate(requireContext());
        }
        AdsManager.getInstance(requireContext()).showInterstitialIfAvailable(requireActivity(), () ->
                NavHostFragment.findNavController(this).navigate(item.getDestinationId()));
    }
}
