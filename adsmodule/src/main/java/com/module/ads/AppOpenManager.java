package com.module.ads;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    private static final String LOG_TAG = "AppOpenManager";
    private String AD_UNIT_ID = "";
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private static boolean isShowingAd = false;
    private final Application myApplication;
    private Activity currentActivity;
    private long loadTime = 0;
    private MySharedPref sharedprefs;
    public static OnSplashCallBack onSplashCallBack;
    public static boolean callAppOpenAddOnlyOnce = false;
    public static int appOpenAddCounter = 0;


    public static OnSplashCallBack getOnSplashCallBack() {
        return onSplashCallBack;
    }

    public static void setOnSplashCallBack(OnSplashCallBack onSplashCallBack) {
        AppOpenManager.onSplashCallBack = onSplashCallBack;
    }
    FullScreenContentCallback fullScreenContentCallback =
            new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    Log.e("***Openapp","On Add Dismmed ");
                    AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    try{
                        if(onSplashCallBack != null){
                            onSplashCallBack.afterOpenAddCallBack();
                            onSplashCallBack = null;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    fetchAd();

                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.e("***Openapp","Add Failed to show content ");
                    try{
                        if(onSplashCallBack != null){
                            onSplashCallBack.afterOpenAddCallBack();
                            onSplashCallBack = null;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.e("***Openapp","Add shown ");
                    isShowingAd = true;
                }
            };

    public AppOpenManager(Application myApplication, MySharedPref sharedPref, Boolean isDebugRunning) {
        AD_UNIT_ID=AddIds.getAppOpenId(sharedPref,isDebugRunning);
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        this.sharedprefs = sharedPref;

    }


    @OnLifecycleEvent(ON_START)
    public void onStart() {
        if(!sharedprefs.isPurshed()) {
            showAdIfAvailable();
        }
//        sharedprefs = new Sharedprefs(currentActivity);
//        if (!sharedprefs.showPreferences()) {
//            showAdIfAvailable();
//            Log.d(LOG_TAG, "onStart");
//        }
    }


    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");


            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);


        } else {
            Log.d(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }

    public void fetchAd() {
        if (isAdAvailable()) {
            return;
        }

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                super.onAdLoaded(appOpenAd);
                AppOpenManager.this.appOpenAd = appOpenAd;
                AppOpenManager.this.loadTime = (new Date()).getTime();
                Log.e("***Ad","open add loaded");
                try {
                    if (onSplashCallBack != null) {
                        appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                        appOpenAd.show(currentActivity);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e("***Ad","open add failed "+ loadAdError.getMessage());
                try{
                    if(onSplashCallBack != null){
                        onSplashCallBack.afterOpenAddCallBack();
                        onSplashCallBack = null;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        AdRequest request = getAdRequest();
        AppOpenAd.load(
                myApplication, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }


    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }


    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }


    public boolean isAdAvailable() {
        return (appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4));
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        Log.e("***Openapp","Activity resumed ");
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Log.e("***Openapp","onActivitySaveInstanceState ");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }
}