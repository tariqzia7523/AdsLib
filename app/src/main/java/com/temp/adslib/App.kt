package com.temp.adslib

import android.app.Application
import com.module.ads.AddInitilizer
import com.module.ads.AppOpenManager
import com.module.ads.MySharedPref

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppOpenManager(this, MySharedPref(this),BuildConfig.DEBUG)
    }
}