package com.example.protoolkit.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.protoolkit.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages advertisement display throughout the application.
 */
public class AdsManager {

    private static final String TAG = "AdsManager";
    private static AdsManager instance;
    
    private final Context context;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private final AtomicBoolean isInterstitialLoading = new AtomicBoolean(false);
    private final AtomicBoolean isRewardedLoading = new AtomicBoolean(false);
    
    // Test ad unit IDs for development
    private static final String TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917";

    public interface AdLoadListener {
        void onAdLoaded();
        void onAdFailedToLoad(String error);
    }

    public interface RewardAdListener {
        void onRewardEarned();
        void onAdClosed();
        void onAdFailed(String error);
    }

    private AdsManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        initializeMobileAds();
    }

    public static synchronized AdsManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new AdsManager(context);
        }
        return instance;
    }

    private void initializeMobileAds() {
        MobileAds.initialize(context, initializationStatus -> {
            Log.d(TAG, "MobileAds initialized");
        });
        
        // For testing purposes, set test device IDs
        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(Collections.singletonList(AdRequest.DEVICE_ID_EMULATOR))
                .build();
        MobileAds.setRequestConfiguration(configuration);
    }

    public void loadBanner(ViewGroup container) {
        if (container == null) {
            Log.w(TAG, "Banner container is null");
            return;
        }
        
        container.removeAllViews();
        
        String adUnitId = context.getString(R.string.admob_banner_id);
        if (adUnitId.equals("ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy")) {
            // Use test ID if real ID is not configured
            adUnitId = TEST_BANNER_ID;
        }

        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnitId);
        
        container.addView(adView);
        
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public void loadInterstitialAd(@NonNull AdLoadListener listener) {
        if (isInterstitialLoading.getAndSet(true)) {
            Log.d(TAG, "Interstitial ad is already loading");
            isInterstitialLoading.set(false);
            return;
        }

        String adUnitId = context.getString(R.string.admob_interstitial_id);
        if (adUnitId.equals("ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy")) {
            // Use test ID if real ID is not configured
            adUnitId = TEST_INTERSTITIAL_ID;
        }

        InterstitialAd.load(context, adUnitId, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        isInterstitialLoading.set(false);
                        listener.onAdLoaded();
                        Log.d(TAG, "Interstitial ad loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAd = null;
                        isInterstitialLoading.set(false);
                        listener.onAdFailedToLoad(loadAdError.getMessage());
                        Log.e(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                    }
                });
    }

    public void loadRewardedAd(@NonNull RewardAdListener listener) {
        if (isRewardedLoading.getAndSet(true)) {
            Log.d(TAG, "Rewarded ad is already loading");
            isRewardedLoading.set(false);
            return;
        }

        String adUnitId = context.getString(R.string.admob_rewarded_id);
        if (adUnitId.equals("ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy")) {
            // Use test ID if real ID is not configured
            adUnitId = TEST_REWARDED_ID;
        }

        RewardedAd.load(context, adUnitId, new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        isRewardedLoading.set(false);
                        Log.d(TAG, "Rewarded ad loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedAd = null;
                        isRewardedLoading.set(false);
                        listener.onAdFailed(loadAdError.getMessage());
                        Log.e(TAG, "Rewarded ad failed to load: " + loadAdError.getMessage());
                    }
                });
    }

    public boolean showInterstitialAd(@NonNull Activity activity) {
        if (interstitialAd != null) {
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    interstitialAd = null;
                    Log.d(TAG, "Interstitial ad dismissed");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    interstitialAd = null;
                    Log.e(TAG, "Interstitial ad failed to show: " + adError.getMessage());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed");
                }
            });

            interstitialAd.show(activity);
            return true;
        } else {
            Log.d(TAG, "Interstitial ad not ready");
            return false;
        }
    }

    public boolean showRewardedAd(@NonNull Activity activity, @NonNull RewardAdListener listener) {
        if (rewardedAd != null) {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    rewardedAd = null;
                    listener.onAdFailed(adError.getMessage());
                    Log.e(TAG, "Rewarded ad failed to show: " + adError.getMessage());
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;
                    listener.onAdClosed();
                    Log.d(TAG, "Rewarded ad dismissed");
                }
            });

            rewardedAd.show(activity, rewardItem -> {
                Log.d(TAG, "User earned reward: " + rewardItem.getType() + " " + rewardItem.getAmount());
                listener.onRewardEarned();
            });
            return true;
        } else {
            Log.d(TAG, "Rewarded ad not ready");
            return false;
        }
    }

    public boolean isInterstitialAdReady() {
        return interstitialAd != null;
    }

    public boolean isRewardedAdReady() {
        return rewardedAd != null;
    }
    
    public void showInterstitialIfAvailable(@NonNull Activity activity, @NonNull Runnable onNotShown) {
        if (isInterstitialAdReady()) {
            showInterstitialAd(activity);
        } else {
            onNotShown.run();
        }
    }
}