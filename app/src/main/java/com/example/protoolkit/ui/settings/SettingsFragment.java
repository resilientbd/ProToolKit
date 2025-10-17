package com.example.protoolkit.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.protoolkit.R;
import com.example.protoolkit.ads.AdsManager;
import com.example.protoolkit.ads.AdsManager.RewardAdListener;
import com.example.protoolkit.util.AppConstants;
import com.example.protoolkit.util.ServiceLocator;

/**
 * Settings screen built with Preference APIs.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private SettingsViewModel viewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey);
        viewModel = new ViewModelProvider(this, ServiceLocator.getViewModelFactory()).get(SettingsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupThemePreference();
        setupHapticsPreference();
        setupRewardPreference();
        viewModel.getAdsDisabledUntil().observe(getViewLifecycleOwner(), timestamp -> {
            Preference rewardPref = findPreference(AppConstants.PREF_REWARDED_AD);
            if (rewardPref != null) {
                if (timestamp != null && timestamp > System.currentTimeMillis()) {
                    java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(requireContext());
                    java.text.DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(requireContext());
                    String message = getString(R.string.settings_ads_disabled_until,
                            dateFormat.format(new java.util.Date(timestamp)) + " " + timeFormat.format(new java.util.Date(timestamp)));
                    rewardPref.setSummary(message);
                } else {
                    rewardPref.setSummary(R.string.settings_rewarded_ads_summary);
                }
            }
        });
    }

    private void setupThemePreference() {
        ListPreference preference = findPreference(AppConstants.PREF_THEME_MODE);
        if (preference != null) {
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                if (newValue instanceof String) {
                    viewModel.updateTheme((String) newValue);
                    return true;
                }
                return false;
            });
        }
    }

    private void setupHapticsPreference() {
        SwitchPreferenceCompat preference = findPreference(AppConstants.PREF_HAPTICS_ENABLED);
        if (preference != null) {
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                if (newValue instanceof Boolean) {
                    viewModel.updateHaptics((Boolean) newValue);
                    return true;
                }
                return false;
            });
        }
    }

    private void setupRewardPreference() {
        Preference rewardPref = findPreference(AppConstants.PREF_REWARDED_AD);
        if (rewardPref != null) {
            rewardPref.setOnPreferenceClickListener(pref -> {
                AdsManager.getInstance(requireContext()).showRewardedAd(requireActivity(), new RewardAdListener() {
                    @Override
                    public void onRewardEarned() {
                        viewModel.disableAdsTemporarily();
                        Toast.makeText(requireContext(), R.string.ads_rewarded_success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdClosed() {
                        Toast.makeText(requireContext(), R.string.ads_rewarded_failure, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailed(String error) {
                        Toast.makeText(requireContext(), R.string.ads_rewarded_failure + ": " + error, Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            });
        }
    }
}
