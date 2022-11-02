//package com.module.ads
//
//import android.app.Application
//
//
//class App : Application() {
//
//    var appOpenManager: AppOpenManager? = null
//    override fun onCreate() {
//        super.onCreate()
//        appOpenManager = AppOpenManager(this, MySharedPref(this),BuildConfig.DEBUG)
//        AddInitilizer.madiationInitilization(this)
//    }
//}