package com.temp.adslib

import android.app.Application
import android.util.Log
import com.module.ads.AdRevenueDispatcher
import com.module.ads.AddIds
import com.module.ads.AddInitilizer
import com.module.ads.AppOpenAdManager
import com.module.ads.GoogleMobileAdsConsentManager
import com.module.ads.MySharedPref

class App : Application() {
    lateinit var appOpenAdManager: AppOpenAdManager
    lateinit var consentManager: GoogleMobileAdsConsentManager
    private var isLoadingCalled : Boolean= false
    override fun onCreate() {
        super.onCreate()
//
        val mySharedPref = MySharedPref(this)
        mySharedPref.appOpenID ="orignal_app_id"

        AddInitilizer.initAd(applicationContext)
        consentManager = GoogleMobileAdsConsentManager.getInstance(this)


        // ***** this is optional you can use it if you want to log revenue details in firebase
        // Classes are available within the code.
        // For Firebase analytics use following line
//        AdRevenueDispatcher.listener = FirebaseAdRevenueLogger(FirebaseAnalytics.getInstance(this))

        // just for logging screen or testing
        AdRevenueDispatcher.listener = FirebaseAdRevenueLogger()

    }

    fun initAppOpenAfterConsent(adUnitId: String) {
        if(!isLoadingCalled){
            Log.e("***AppOpen","app open called ")
            appOpenAdManager = AppOpenAdManager(
                this,
                adUnitId,
                consentManager
            )
            isLoadingCalled = true
        }
    }
}