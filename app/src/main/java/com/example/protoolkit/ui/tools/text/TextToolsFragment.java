package com.example.protoolkit.ui.tools.text;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.protoolkit.R;
import com.example.protoolkit.databinding.FragmentTextToolsBinding;
import com.example.protoolkit.ui.base.BaseFragment;
import com.example.protoolkit.util.HapticHelper;
import com.example.protoolkit.util.ServiceLocator;

/**
 * Fragment implementing text utilities such as case conversion and counters.
 */
public class TextToolsFragment extends BaseFragment {

    private FragmentTextToolsBinding binding;
    private TextToolsViewModel viewModel;

    public TextToolsFragment() {
        super(R.layout.fragment_text_tools);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentTextToolsBinding.bind(view);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(TextToolsViewModel.class);
        setupInput();
        setupButtons();
        observe(viewModel.getTransformedText(), text -> binding.outputText.setText(text));
        observe(viewModel.getWordCount(), count -> binding.wordCount.setText(getString(R.string.text_tools_word_count, count)));
        observe(viewModel.getCharacterCount(), count -> binding.characterCount.setText(getString(R.string.text_tools_character_count, count)));
    }

    private void setupInput() {
        binding.inputText.addTextChangedListener(new TextWatcher() {
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

    private void setupButtons() {
        binding.buttonUppercase.setOnClickListener(v -> {
            viewModel.transformUppercase();
            triggerHaptic();
        });
        binding.buttonLowercase.setOnClickListener(v -> {
            viewModel.transformLowercase();
            triggerHaptic();
        });
        binding.buttonTitlecase.setOnClickListener(v -> {
            viewModel.transformTitleCase();
            triggerHaptic();
        });
        binding.buttonCopy.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData data = ClipData.newPlainText("text-tools", binding.outputText.getText());
                clipboardManager.setPrimaryClip(data);
                Toast.makeText(requireContext(), R.string.text_tools_copy_success, Toast.LENGTH_SHORT).show();
            }
            triggerHaptic();
        });
    }

    private void triggerHaptic() {
        if (ServiceLocator.getSettingsRepository().isHapticsEnabled()) {
            HapticHelper.vibrate(requireContext());
        }
    }
}
