package com.example.protoolkit.ads;

import android.app.Activity;
import android.app.Application;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.protoolkit.BuildConfig;
import com.example.protoolkit.R;
import com.example.protoolkit.data.settings.SettingsRepository;
import com.example.protoolkit.util.AppConstants;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Arrays;

/**
 * Centralized ad loader/show manager. Uses Google test IDs for development.
 */
public class AdsManager {

    public interface RewardListener {
        void onRewardEarned();

        void onRewardClosedWithoutReward();
    }

    private static AdsManager instance;

    private final Application application;
    private final SettingsRepository settingsRepository;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private long lastInterstitialShownAt;

    private AdsManager(@NonNull Application application, @NonNull SettingsRepository settingsRepository) {
        this.application = application;
        this.settingsRepository = settingsRepository;

        MobileAds.initialize(application);
        // Register emulator as test device. Add physical device IDs when testing on hardware.
        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().build());
        loadInterstitial();
        loadRewarded();
    }

    public static void init(@NonNull Application application, @NonNull SettingsRepository settingsRepository) {
        if (instance == null) {
            instance = new AdsManager(application, settingsRepository);
        }
    }

    @NonNull
    public static AdsManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AdsManager not initialized. Ensure AdsManager.init() is called in Application.");
        }
        return instance;
    }

    public void loadBanner(@NonNull FrameLayout container) {
        if (!settingsRepository.shouldShowAds()) {
            container.removeAllViews();
            container.setContentDescription(application.getString(R.string.ads_not_available));
            return;
        }
        container.removeAllViews();
        AdView adView = new AdView(application);
        adView.setAdUnitId(BuildConfig.ADMOB_BANNER_ID); // TODO: Replace with production ID before release.
        adView.setAdSize(AdSize.BANNER);
        adView.setContentDescription(application.getString(R.string.tool_banner_content_description));
        container.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                container.removeAllViews();
            }
        });
    }

    public void loadInterstitial() {
        if (!settingsRepository.shouldShowAds()) {
            interstitialAd = null;
            return;
        }
        AdRequest request = new AdRequest.Builder().build();
        InterstitialAd.load(application, BuildConfig.ADMOB_INTERSTITIAL_ID, request, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                interstitialAd = null;
            }
        });
    }

    public void showInterstitialIfAvailable(@NonNull Activity activity, @Nullable Runnable onDismiss) {
        if (!settingsRepository.shouldShowAds()) {
            if (onDismiss != null) {
                onDismiss.run();
            }
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastInterstitialShownAt < AppConstants.INTERSTITIAL_COOLDOWN_MS) {
            if (onDismiss != null) {
                onDismiss.run();
            }
            return;
        }
        if (interstitialAd == null) {
            if (onDismiss != null) {
                onDismiss.run();
            }
            loadInterstitial();
            return;
        }
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                lastInterstitialShownAt = System.currentTimeMillis();
                loadInterstitial();
                if (onDismiss != null) {
                    onDismiss.run();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                loadInterstitial();
                if (onDismiss != null) {
                    onDismiss.run();
                }
            }
        });
        interstitialAd.show(activity);
    }

    public void loadRewarded() {
        if (!settingsRepository.shouldShowAds()) {
            rewardedAd = null;
            return;
        }
        AdRequest request = new AdRequest.Builder().build();
        RewardedAd.load(application, BuildConfig.ADMOB_REWARDED_ID, request, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
            }
        });
    }

    @MainThread
    public void showRewarded(@NonNull Activity activity, @NonNull RewardListener listener) {
        if (!settingsRepository.shouldShowAds()) {
            listener.onRewardEarned();
            return;
        }
        if (rewardedAd == null) {
            Toast.makeText(activity, R.string.ads_not_available, Toast.LENGTH_SHORT).show();
            loadRewarded();
            listener.onRewardClosedWithoutReward();
            return;
        }
        final boolean[] rewardEarned = {false};
        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadRewarded();
                if (rewardEarned[0]) {
                    loadInterstitial();
                } else {
                    listener.onRewardClosedWithoutReward();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                loadRewarded();
                listener.onRewardClosedWithoutReward();
            }
        });
        rewardedAd.show(activity, rewardItem -> {
            rewardEarned[0] = true;
            settingsRepository.setAdsDisabledUntil(System.currentTimeMillis() + AppConstants.ADS_REWARD_DURATION_MS);
            listener.onRewardEarned();
        });
    }

    public boolean areAdsDisabledTemporarily() {
        return !settingsRepository.shouldShowAds();
    }
}
