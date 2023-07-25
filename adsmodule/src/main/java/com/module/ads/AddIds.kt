package com.module.ads

import android.content.Context

object AddIds {
    // orignal id
    // test id
    fun getBannerId(context: Context,isDebugRunning : Boolean) :String{
        var id = ""
        id = if (isDebugRunning) {
            "ca-app-pub-3940256099942544/6300978111" // test id
        } else {
           MySharedPref(context).bannerID // orignal id
        }
        return id
    }

    fun getInterstitialId(context: Context, isDebugRunning : Boolean) :String{
        var id = ""
        id = if (isDebugRunning) {
            "ca-app-pub-3940256099942544/1033173712" // test id
        } else {
            MySharedPref(context).interID // orignal id
        }
        return id
    }


    fun getNativeId(context: Context, isDebugRunning : Boolean) :String{
        var id = ""
        id = if (isDebugRunning) {
            "ca-app-pub-3940256099942544/2247696110" // test id
        } else {
            MySharedPref(context).nativeID // orignal id
        }
        return id
    }

    fun getRewardId(context: Context, isDebugRunning : Boolean) :String{
        var id = ""
        id = if (isDebugRunning) {
            "ca-app-pub-3940256099942544/5224354917" // test id
        } else {
            MySharedPref(context).rewardID // orignal id
        }
        return id
    }

    @JvmStatic
    fun getAppOpenId(mySharedPref: MySharedPref, isDebugRunning : Boolean) :String{
        var id = ""
        id = if (isDebugRunning) {
            "ca-app-pub-3940256099942544/3419835294" // test id
//            "ca-app-pub-3940256099942544/34153434294" // test id
        } else {
            mySharedPref.appOpenID // orignal id
        }
        return id
    }

}