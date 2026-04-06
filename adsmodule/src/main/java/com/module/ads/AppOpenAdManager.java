package com.module.ads;



import android.app.Activity;

import android.app.Application;

import android.os.Bundle;

import android.os.Handler;

import android.os.Looper;

import android.text.TextUtils;

import android.util.Log;



import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import androidx.lifecycle.DefaultLifecycleObserver;

import androidx.lifecycle.Lifecycle;

import androidx.lifecycle.LifecycleOwner;

import androidx.lifecycle.ProcessLifecycleOwner;



import com.google.android.gms.ads.AdError;

import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdValue;

import com.google.android.gms.ads.FullScreenContentCallback;

import com.google.android.gms.ads.LoadAdError;

import com.google.android.gms.ads.MobileAds;

import com.google.android.gms.ads.OnPaidEventListener;

import com.google.android.gms.ads.appopen.AppOpenAd;

import com.google.android.gms.ads.initialization.InitializationStatus;

import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;



import java.util.Date;

import java.util.concurrent.atomic.AtomicBoolean;



/**

 * App open ads: at most one successful impression per app process (session), while ensuring

 * the user sees an app open at least once during that session when consent allows and inventory loads.

 * <p>

 * Loads after {@link MobileAds#initialize(android.content.Context, OnInitializationCompleteListener)}

 * so requests are not made before the SDK is ready (required when the host app initializes ads off

 * the main thread).

 * <p>

 * Register {@link #setActivityPredicate(ActivityPredicate)} from the host app to block showing

 * on splash / consent screens; preloading still runs so the ad can show on the next allowed activity.

 * <p>

 * <b>Splash / cold start:</b> Set {@link #setOnSplashCallBack(OnSplashCallBack)} <em>before</em>

 * constructing this manager. The callback is delivered <strong>exactly once</strong> when the

 * cold-start app open attempt is finished: ad dismissed, show failed, load failed, no ad unit,

 * consent/premium/session rules prevent showing, or activity blocked by predicate. Host apps can

 * navigate to the home screen from that callback without arbitrary timeouts.

 * <p>

 * <b>Important:</b> {@link Application#registerActivityLifecycleCallbacks} does not deliver

 * {@code onActivityStarted}/{@code onActivityResumed} for activities that were already started

 * <em>before</em> this manager was created. Pass the current foreground {@link Activity} as

 * {@code seedActivity}, or call {@link #notifyForegroundActivity(Activity)} after init, so

 * {@link #showAdIfAvailable()} has a non-null host activity.

 */

public class AppOpenAdManager implements

        Application.ActivityLifecycleCallbacks,

        DefaultLifecycleObserver {



    private static final String TAG = "AppOpenAdManager";



    /** Optional: return false for activities where app open must not cover the UI (e.g. Splash). */

    public interface ActivityPredicate {

        boolean allowAppOpenForActivity(@NonNull Activity activity);

    }



    private static volatile ActivityPredicate activityPredicate;



    public static void setActivityPredicate(@Nullable ActivityPredicate predicate) {

        activityPredicate = predicate;

    }



    private final Application application;

    private final String adUnitId;

    private final GoogleMobileAdsConsentManager consentManager;

    private final MySharedPref mySharedPref;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());



    private AppOpenAd appOpenAd;

    private Activity currentActivity;



    private boolean isShowingAd = false;

    private long loadTime = 0;



    /** True after we successfully show an app open this process; no more app opens until cold start. */

    private boolean sessionImpressionShown = false;



    private boolean isAppInForeground = false;



    /** Prevents overlapping load attempts while a load is in flight. */

    private volatile boolean isLoadingAd = false;



    /**

     * Last constructed manager (typical single app-open per process). Used when Navigation changes

     * destination inside the same Activity — {@code onActivityResumed} is not called again.

     */

    private static volatile AppOpenAdManager sInstance;



    /** Optional: invoked once when cold-start splash flow ends (see class javadoc). */

    private static volatile OnSplashCallBack sOnSplashCallBack;



    /** Ensures {@link #notifySplashCallBack()} runs at most once per registered callback. */

    private static final AtomicBoolean splashCallbackDelivered = new AtomicBoolean(false);



    public static void setOnSplashCallBack(@Nullable OnSplashCallBack callback) {

        sOnSplashCallBack = callback;

        splashCallbackDelivered.set(false);

    }



    @Nullable

    public static OnSplashCallBack getOnSplashCallBack() {

        return sOnSplashCallBack;

    }



    private static void notifySplashCallBack() {

        OnSplashCallBack cb = sOnSplashCallBack;

        if (cb == null) {

            return;

        }

        if (!splashCallbackDelivered.compareAndSet(false, true)) {

            return;

        }

        sOnSplashCallBack = null;

        try {

            cb.afterOpenAddCallBack();

        } catch (Exception e) {

            Log.e(TAG, "OnSplashCallBack failed", e);

        }

    }



    public AppOpenAdManager(Application application, String adUnitId, GoogleMobileAdsConsentManager consentManager) {

        this(application, adUnitId, consentManager, new MySharedPref(application), null);

    }



    /**

     * @param seedActivity The activity visible when consent completes (e.g. Splash). Required for

     *                     reliable first show when this manager is created after that activity already resumed.

     */

    public AppOpenAdManager(Application application, String adUnitId, GoogleMobileAdsConsentManager consentManager, @Nullable Activity seedActivity) {

        this(application, adUnitId, consentManager, new MySharedPref(application), seedActivity);

    }



    public AppOpenAdManager(Application application, String adUnitId, GoogleMobileAdsConsentManager consentManager, MySharedPref mySharedPref) {

        this(application, adUnitId, consentManager, mySharedPref, null);

    }



    public AppOpenAdManager(Application application, String adUnitId, GoogleMobileAdsConsentManager consentManager, MySharedPref mySharedPref, @Nullable Activity seedActivity) {

        this.application = application;

        this.adUnitId = adUnitId;

        this.consentManager = consentManager;

        this.mySharedPref = mySharedPref;



        application.registerActivityLifecycleCallbacks(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);



        if (seedActivity != null) {

            currentActivity = seedActivity;

            Log.d(TAG, "Seeded current activity: " + seedActivity.getClass().getSimpleName());

        }



        sInstance = this;



        if (consentManager.canRequestAds() && !mySharedPref.isPurshed()) {

            loadAd();

        }



        // Manager may be created after consent while the process is already foreground — trigger once.

        mainHandler.post(() -> {

            Lifecycle.State state = ProcessLifecycleOwner.get().getLifecycle().getCurrentState();

            if (state.isAtLeast(Lifecycle.State.STARTED)) {

                isAppInForeground = true;

                showAdIfAvailable();

            }

        });

    }



    /**

     * Call when the foreground activity is known but was not passed as {@code seedActivity}

     * (e.g. single-activity flows or custom navigation).

     */

    public void notifyForegroundActivity(@NonNull Activity activity) {

        mainHandler.post(new Runnable() {

            @Override

            public void run() {

                currentActivity = activity;

                Log.d(TAG, "notifyForegroundActivity: " + activity.getClass().getSimpleName());

                showAdIfAvailable();

            }

        });

    }



    /**

     * Retry show on the main thread (e.g. after NavController navigates to a destination that allows app open).

     * Same Activity does not receive another {@code onResume} when only fragments change.

     */

    public void requestShowIfReady() {

        mainHandler.post(this::showAdIfAvailable);

    }



    /**

     * Like {@link #requestShowIfReady()} but safe when you do not hold a manager reference yet.

     * No-op if no {@link AppOpenAdManager} has been constructed.

     */

    public static void requestShowAfterNavigationOrDestinationChange() {

        AppOpenAdManager m = sInstance;

        if (m != null) {

            m.requestShowIfReady();

        }

    }



    boolean shouldRunAppOpenFlow() {

        return consentManager.canRequestAds() && !mySharedPref.isPurshed() && !sessionImpressionShown;

    }



    /** Process is in foreground (user-visible), safe to show full-screen app open. */

    private boolean isForegroundProcess() {

        Lifecycle.State state = ProcessLifecycleOwner.get().getLifecycle().getCurrentState();

        return state.isAtLeast(Lifecycle.State.STARTED);

    }



    private void loadAd() {

        if (!shouldRunAppOpenFlow()) {

            notifySplashCallBack();

            return;

        }

        if (TextUtils.isEmpty(adUnitId)) {

            Log.e(TAG, "App Open: ad unit id is empty. Set MySharedPref.appOpenID (release) or use debug build for test ids.");

            notifySplashCallBack();

            return;

        }

        if (isAdAvailable()) {

            return;

        }

        if (isLoadingAd) {

            Log.d(TAG, "App Open: load already in progress");

            return;

        }



        isLoadingAd = true;

        // Ensure Mobile Ads SDK is ready (host apps often call initialize() off the main thread).

        MobileAds.initialize(application, new OnInitializationCompleteListener() {

            @Override

            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

                mainHandler.post(new Runnable() {

                    @Override

                    public void run() {

                        if (!shouldRunAppOpenFlow()) {

                            isLoadingAd = false;

                            notifySplashCallBack();

                            return;

                        }

                        loadAppOpenAdAfterSdkReady();

                    }

                });

            }

        });

    }



    private void loadAppOpenAdAfterSdkReady() {

        if (!shouldRunAppOpenFlow()) {

            isLoadingAd = false;

            notifySplashCallBack();

            return;

        }

        if (isAdAvailable()) {

            isLoadingAd = false;

            return;

        }



        AdRequest request = new AdRequest.Builder().build();



        AppOpenAd.load(

                application,

                adUnitId,

                request,

                new AppOpenAd.AppOpenAdLoadCallback() {

                    @Override

                    public void onAdLoaded(@NonNull AppOpenAd ad) {

                        isLoadingAd = false;

                        appOpenAd = ad;

                        loadTime = new Date().getTime();

                        Log.d(TAG, "App Open Ad loaded");

                        // Do not rely on isAppInForeground: it can lag behind the first frame; use process lifecycle.

                        mainHandler.post(new Runnable() {

                            @Override

                            public void run() {

                                if (!shouldRunAppOpenFlow()) {

                                    notifySplashCallBack();

                                    return;

                                }

                                if (isForegroundProcess()) {

                                    showAdIfAvailable();

                                } else {

                                    Log.d(TAG, "App Open loaded while process in background; will show on next foreground.");

                                }

                            }

                        });

                    }



                    @Override

                    public void onAdFailedToLoad(@NonNull LoadAdError error) {

                        isLoadingAd = false;

                        Log.e(TAG, "App Open failed: code=" + error.getCode() + " " + error.getMessage());

                        notifySplashCallBack();

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



    private boolean isActivityAllowedForShow(@NonNull Activity activity) {

        ActivityPredicate p = activityPredicate;

        return p == null || p.allowAppOpenForActivity(activity);

    }



    private void showAdIfAvailable() {

        if (!shouldRunAppOpenFlow()) {

            notifySplashCallBack();

            return;

        }

        if (isShowingAd) {

            return;

        }

        if (currentActivity == null) {

            Log.d(TAG, "show skipped: no current activity (pass seed Activity in constructor or call notifyForegroundActivity)");

            notifySplashCallBack();

            return;

        }

        if (!isActivityAllowedForShow(currentActivity)) {

            Log.d(TAG, "show skipped: blocked by predicate / nav destination (" + currentActivity.getClass().getSimpleName() + ")");

            notifySplashCallBack();

            return;

        }

        if (!isAdAvailable()) {

            loadAd();

            return;

        }



        appOpenAd.setFullScreenContentCallback(

                new FullScreenContentCallback() {

                    @Override

                    public void onAdShowedFullScreenContent() {

                        isShowingAd = true;

                        sessionImpressionShown = true;

                        Log.d(TAG, "App Open shown (session impression recorded)");

                    }



                    @Override

                    public void onAdDismissedFullScreenContent() {

                        appOpenAd = null;

                        isShowingAd = false;

                        Log.d(TAG, "App Open dismissed");

                        notifySplashCallBack();

                    }



                    @Override

                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {

                        appOpenAd = null;

                        isShowingAd = false;

                        Log.e(TAG, "App Open failed to show");

                        notifySplashCallBack();

                    }





                }

        );

        try {

            appOpenAd.setOnPaidEventListener(new OnPaidEventListener() {

                @Override

                public void onPaidEvent(@NonNull AdValue adValue) {

                    try {

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

                    } catch (Exception e) {

                        e.printStackTrace();

                    }



                }

            });

        } catch (Exception e) {

            e.printStackTrace();

        }



        try {

            appOpenAd.show(currentActivity);

        } catch (Exception e) {

            Log.e(TAG, "show() failed", e);

            appOpenAd = null;

            isShowingAd = false;

            notifySplashCallBack();

        }

    }



    @Override

    public void onStart(@NonNull LifecycleOwner owner) {

        isAppInForeground = true;

        showAdIfAvailable();

    }



    @Override

    public void onStop(@NonNull LifecycleOwner owner) {

        isAppInForeground = false;

    }



    @Override

    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }



    @Override

    public void onActivityStarted(@NonNull Activity activity) {

        currentActivity = activity;

    }



    @Override

    public void onActivityResumed(@NonNull Activity activity) {

        currentActivity = activity;

        // Moving Splash → Main does not fire another process onStart; retry here for first impression.

        if (shouldRunAppOpenFlow()) {

            showAdIfAvailable();

        }

    }



    @Override

    public void onActivityPaused(@NonNull Activity activity) {

    }



    @Override

    public void onActivityStopped(@NonNull Activity activity) {

    }



    @Override

    public void onActivitySaveInstanceState(@NonNull Activity activity, @Nullable Bundle outState) {

    }



    @Override

    public void onActivityDestroyed(@NonNull Activity activity) {

        if (activity == currentActivity) {

            currentActivity = null;

        }

    }



    /** Whether this session has already shown an app open successfully. */

    public boolean hasSessionImpression() {

        return sessionImpressionShown;

    }

}

