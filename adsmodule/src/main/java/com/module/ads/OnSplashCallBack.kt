package com.module.ads

/**
 * Optional global callback after the app-open flow **ends** (user dismissed the ad, or show failed).
 * Set via [AppOpenAdManager.setOnSplashCallBack] from your Application or splash screen so you can
 * continue navigation (e.g. `NavController.navigate`) in MVVM apps.
 *
 * Not invoked when no ad was shown because the session already had an impression or the user is premium.
 */
interface OnSplashCallBack {
    fun afterOpenAddCallBack()
}