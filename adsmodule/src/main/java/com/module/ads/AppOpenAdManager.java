package com.module.ads;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class AppOpenAdManager implements
        Application.ActivityLifecycleCallbacks,
        DefaultLifecycleObserver {

    private static final String TAG = "AppOpenAdManager";

    private final Application application;
    private final String adUnitId;
    private final GoogleMobileAdsConsentManager consentManager;

    private AppOpenAd appOpenAd;
    private Activity currentActivity;

    private boolean isShowingAd = false;
    private long loadTime = 0;

    public AppOpenAdManager(Application application, String adUnitId, GoogleMobileAdsConsentManager consentManager) {
        this.application = application;
        this.adUnitId = adUnitId;
        this.consentManager = consentManager;

        application.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // IMPORTANT: do NOT load ad here unless consent is already granted
        if (consentManager.canRequestAds()) {
            loadAd();
        }
    }

    // ---------------- LOAD AD ----------------

    private void loadAd() {
        if (!consentManager.canRequestAds()) {
            Log.d(TAG, "Consent not granted. Skipping App Open load.");
            return;
        }

        if (isAdAvailable()) return;

        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(
                application,
                adUnitId,
                request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        loadTime = new Date().getTime();
                        Log.d(TAG, "App Open Ad loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.e(TAG, "App Open failed: " + error.getMessage());
                    }

                }
        );
    }

    private boolean isAdAvailable() {
        return appOpenAd != null && wasLoadedWithinHours(4);
    }

    private boolean wasLoadedWithinHours(int hours) {
        long diff = new Date().getTime() - loadTime;
        return diff < hours * 3600000L;
    }

    // ---------------- SHOW AD ----------------

    private void showAdIfAvailable() {
        if (!consentManager.canRequestAds()) return;
        if (isShowingAd || !isAdAvailable() || currentActivity == null) {
            loadAd();
            return;
        }

        appOpenAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        isShowingAd = true;
                        Log.d(TAG, "App Open shown");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        appOpenAd = null;
                        isShowingAd = false;
                        Log.d(TAG, "App Open dismissed");
                        loadAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        appOpenAd = null;
                        isShowingAd = false;
                        Log.e(TAG, "App Open failed to show");
                        loadAd();
                    }


                }
        );
        try{
            appOpenAd.setOnPaidEventListener(adValue -> {
                try{
                    double revenue =
                            adValue.getValueMicros() / 1_000_000.0;
                    String currency = adValue.getCurrencyCode();
                    AdRevenueListener listener = AdRevenueDispatcher.INSTANCE.getListener();

                    if (listener != null) {
                        listener.onAdRevenuePaid(
                                revenue,
                                currency,
                                "app_open",
                                currentActivity != null
                                        ? currentActivity.getClass().getSimpleName()
                                        : "unknown",
                                false
                        );
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            });
        }catch (Exception e){
            e.printStackTrace();
        }

        appOpenAd.show(currentActivity);
    }

    // ---------------- PROCESS LIFECYCLE ----------------

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        showAdIfAvailable();
    }

    // ---------------- ACTIVITY TRACKING ----------------

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override public void onActivityCreated(Activity a, Bundle b) {}
    @Override public void onActivityStarted(Activity a) {}
    @Override public void onActivityPaused(Activity a) {}
    @Override public void onActivityStopped(Activity a) {}
    @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (activity == currentActivity) {
            currentActivity = null;
        }
    }
}
