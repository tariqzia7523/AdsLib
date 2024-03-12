package com.module.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.multidex.MultiDex
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import java.util.*


class Application : Application() , Application.ActivityLifecycleCallbacks, LifecycleObserver {
    var sApplication: Application? = null




    fun getApplication(): Application? {
        return sApplication
    }

    fun getContext(): Context? {
        return getApplication()!!.applicationContext
    }


    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    private inner class AppOpenAdManager {

        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        private var loadTime: Long = 0

        fun loadAd(context: Context) {
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()

            if(Utility.getPackageStatus(applicationContext).equals("basic"))
            {
                AppOpenAd.load(
                    context,
                    AD_UNIT_ID,
                    request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    object : AppOpenAd.AppOpenAdLoadCallback() {

                        override fun onAdLoaded(ad: AppOpenAd) {
                            appOpenAd = ad
                            isLoadingAd = false
                            loadTime = Date().time
                            Log.e(LOG_TAG, "onAdLoaded.")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            isLoadingAd = false
                            Log.e(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
                        }
                    }
                )
            }
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }


        fun showAdIfAvailable(activity: Activity) {
            showAdIfAvailable(
                activity,
                object : OnShowAdCompleteListener {
                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onShowAdComplete() {
                        if(globalOnShowAdCompleteListener != null){
                            globalOnShowAdCompleteListener!!.onShowAdComplete()
                        }
                    }
                }
            )
        }


        fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
            if (isShowingAd) {
                Log.e(LOG_TAG, "The app open ad is already showing.")
                return
            }

            if (!isAdAvailable()) {
                Log.e(LOG_TAG, "The app open ad is not ready yet.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

            Log.e(LOG_TAG, "Will show ad.")

            appOpenAd!!.setFullScreenContentCallback(
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingAd = false
                        Log.e(LOG_TAG, "onAdDismissedFullScreenContent.")

                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        isShowingAd = false
                        Log.e(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.message)

                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity)
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.e(LOG_TAG, "onAdShowedFullScreenContent.")
                    }
                }
            )
            isShowingAd = true
            appOpenAd!!.show(activity)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        currentActivity?.let { appOpenAdManager.showAdIfAvailable(it) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {

        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {

        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener)
    }





    companion object {
        var mInterstitialAd: InterstitialAd? = null
        lateinit var adRequest: AdRequest
        var context: Context? = null

        var globalOnShowAdCompleteListener : OnShowAdCompleteListener? = null

        var AD_UNIT_ID = ""

        private const val LOG_TAG = "MyApplication"
    }


    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this@Application)

        registerActivityLifecycleCallbacks(this)

        MobileAds.initialize(this) {}
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()


        AD_UNIT_ID = AddIds.getAppOpenId(MySharedPref(this),BuildConfig.DEBUG)


        sApplication = this

        context = getContext()
//        loadInterstitialAd()
    }
    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }
}
