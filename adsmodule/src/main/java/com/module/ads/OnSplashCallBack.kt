package com.module.ads

/**
 * Set via [AppOpenAdManager.setOnSplashCallBack] **before** creating [AppOpenAdManager] on cold start
 * (e.g. from your splash activity right after consent). The library invokes
 * [afterOpenAddCallBack] **exactly once** when the app-open attempt for that launch is finished:
 * the user dismissed the ad, show failed, load failed, no ad unit, consent/premium/session rules
 * block showing, the host activity is missing, or the activity predicate blocked the show.
 *
 * You should navigate to the home screen from this callback (no arbitrary timeouts needed).
 */
interface OnSplashCallBack {
    fun afterOpenAddCallBack()
}
