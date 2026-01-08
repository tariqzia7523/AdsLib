package com.temp.adslib

import android.os.Bundle
import android.util.Log
import com.module.ads.AdRevenueListener

class FirebaseAdRevenueLogger(
    // You may use any type of logging system
//    private val firebaseAnalytics: FirebaseAnalytics
) : AdRevenueListener {

    override fun onAdRevenuePaid(
        revenue: Double,        // actual money value
        currencyCode: String,   // USD, EUR, etc.
        adFormat: String,       // app_open, interstitial, banner
        screenName: String,
        isColdStart: Boolean
    ) {


        val bundle = Bundle().apply {
            putString("currencyCode", currencyCode)
            putString("ad_format", adFormat)
            putString("screenName", screenName)
            putDouble("revenue", revenue)
            putBoolean("isColdStart", isColdStart)
        }

//        firebaseAnalytics.logEvent("ad_impression_revenue", bundle)
        // for now we are just logging the values
        Log.e("****Loging values","currencyCode : $currencyCode , adFormat : $adFormat , screenName : $screenName , revenue : $revenue , isColdStart : $isColdStart")
    }
}