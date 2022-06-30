package com.module.ads

import android.app.Application
import com.google.android.gms.ads.MobileAds

class App : Application() {

    var appOpenManager: AppOpenManager? = null
    override fun onCreate() {
        super.onCreate()
        appOpenManager = AppOpenManager(this, MySharedPref(this),BuildConfig.DEBUG)
        MobileAds.initialize(this
        ) { initializationStatus -> //Showing a simple Toast Message to the user when The Google AdMob Sdk Initialization is Completed
        }
    }
}