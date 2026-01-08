package com.module.ads

import com.google.android.gms.ads.AdValue

interface AdRevenueListener {
    fun onAdRevenuePaid(
        revenue: Double,        // actual money value
        currencyCode: String,   // USD, EUR, etc.
        adFormat: String,       // app_open, interstitial, banner
        screenName: String,
        isColdStart: Boolean
    )
}