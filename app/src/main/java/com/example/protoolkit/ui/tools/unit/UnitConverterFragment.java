package com.example.protoolkit.ui.tools.unit;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.protoolkit.R;
import com.example.protoolkit.databinding.FragmentUnitConverterBinding;
import com.example.protoolkit.ui.base.BaseFragment;
import com.example.protoolkit.ui.tools.unit.UnitConverterViewModel.Category;
import com.example.protoolkit.util.HapticHelper;
import com.example.protoolkit.util.ServiceLocator;
import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * Fragment that renders the Unit Converter tool.
 */
public class UnitConverterFragment extends BaseFragment {

    private FragmentUnitConverterBinding binding;
    private UnitConverterViewModel viewModel;

    public UnitConverterFragment() {
        super(R.layout.fragment_unit_converter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentUnitConverterBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory())
                .get(UnitConverterViewModel.class);
        setupCategoryToggle();
        setupSpinners(Category.LENGTH);
        setupInput();
        binding.buttonSwap.setOnClickListener(v -> {
            viewModel.swapUnits();
            if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
                HapticHelper.vibrate(requireContext());
            }
        });
        observe(viewModel.getResultText(), text -> binding.textResult.setText(text));
        observe(viewModel.getCurrentCategory(), this::setupSpinners);
    }

    private void setupCategoryToggle() {
        binding.categoryToggle.check(R.id.button_length);
        binding.categoryToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (!isChecked) {
                    return;
                }
                Category category = Category.LENGTH;
                if (checkedId == R.id.button_weight) {
                    category = Category.WEIGHT;
                } else if (checkedId == R.id.button_temperature) {
                    category = Category.TEMPERATURE;
                } else if (checkedId == R.id.button_area) {
                    category = Category.AREA;
                }
                viewModel.setCategory(category);
            }
        });
    }

    private void setupSpinners(@NonNull Category category) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                getArrayResForCategory(category),
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFrom.setAdapter(adapter);
        binding.spinnerTo.setAdapter(adapter);
        binding.spinnerFrom.setSelection(0);
        binding.spinnerTo.setSelection(Math.min(1, adapter.getCount() - 1));

        binding.spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setFromIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setToIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupInput() {
        binding.inputValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.updateInput(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private int getArrayResForCategory(@NonNull Category category) {
        switch (category) {
            case WEIGHT:
                return R.array.unit_weight_options;
            case TEMPERATURE:
                return R.array.unit_temperature_options;
            case AREA:
                return R.array.unit_area_options;
            case LENGTH:
            default:
                return R.array.unit_length_options;
        }
    }
}
