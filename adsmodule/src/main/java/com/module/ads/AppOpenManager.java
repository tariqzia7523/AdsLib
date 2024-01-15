package com.module.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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
    private static AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private static boolean isShowingAd = false;
    private final Application myApplication;
    private Activity currentActivity;
    private long loadTime = 0;
    private MySharedPref sharedprefs;
    public static OnSplashCallBack onSplashCallBack;
    public static boolean callAppOpenAddOnlyOnce = false;
    public static int appOpenAddCounter = 0;

    Context context;


    public static OnSplashCallBack getOnSplashCallBack() {
        return onSplashCallBack;
    }

    public static void setOnSplashCallBack(OnSplashCallBack onSplashCallBack) {
        AppOpenManager.onSplashCallBack = onSplashCallBack;
    }
    public static boolean ifAddIsNull(){
        return appOpenAd == null;
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
                    appOpenAddCounter += 1;
                }
            };

    public AppOpenManager(Application myApplication, Context context,MySharedPref sharedPref, Boolean isDebugRunning) {
        AD_UNIT_ID=AddIds.getAppOpenId(sharedPref,isDebugRunning);
        this.context = context;
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        this.sharedprefs = sharedPref;

    }


    @OnLifecycleEvent(ON_START)
    public void onStart() {
        try{
            if(!isNetAvailable()){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(onSplashCallBack != null)
                            onSplashCallBack.afterOpenAddCallBack();
                    }
                },1000);

            }
            else showAdIfAvailable();
        }catch (Exception e){
            e.printStackTrace();
        }
//        if(!sharedprefs.isPurshed()) {
//            showAdIfAvailable();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    Log.e("***Openapp","called before new condition ");
//                    if(onSplashCallBack != null && !isShowingAd) {
//                        Log.e("***Openapp","new condition ");
//                        Log.e("***Openapp","called in condition ");
//                        onSplashCallBack.afterOpenAddCallBack();
//                    }
//                }
//            },6000);
//
//        }
//        sharedprefs = new Sharedprefs(currentActivity);
//        if (!sharedprefs.showPreferences()) {
//            showAdIfAvailable();
//            Log.e(LOG_TAG, "onStart");
//        }
    }


    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.


        if (!isShowingAd && isAdAvailable()) {
            Log.e(LOG_TAG, "Will show ad.");


            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);


        } else {
            Log.e(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }

    public void fetchAd() {
        if(!new GoogleMobileAdsConsentManager(context).getCanRequestAds()){
            return;
        }
        if (isAdAvailable()) {
            return;
        }
        if(callAppOpenAddOnlyOnce && appOpenAddCounter < 1){
            loadADd();
        }else if(!callAppOpenAddOnlyOnce){
            loadADd();
        }


    }
    public void loadADd(){
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

    public boolean isNetAvailable(){
        boolean isNetAvlibel = false;
        try{
            ConnectivityManager mgr = (ConnectivityManager) currentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    isNetAvlibel = true;
                }else {
                    isNetAvlibel = false;
                }
            } else {
                isNetAvlibel = false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isNetAvlibel;
    }
}