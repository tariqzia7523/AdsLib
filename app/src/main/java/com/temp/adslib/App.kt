package com.temp.adslib

import android.app.Application
import com.module.ads.AddInitilizer
import com.module.ads.AppOpenManager
import com.module.ads.MySharedPref

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val mySharedPref = MySharedPref(this)
        mySharedPref.appOpenID ="orignal_app_id"
        AppOpenManager(this, this, mySharedPref,BuildConfig.DEBUG)
    }
}