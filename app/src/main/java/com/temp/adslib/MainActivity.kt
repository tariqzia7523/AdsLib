package com.temp.adslib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.module.ads.AddInitilizer
import com.module.ads.GoogleMobileAdsConsentManager
import com.module.ads.MySharedPref
import com.module.ads.OnAdsClosedCallBack
import com.module.ads.OnRewardedAddCloseCallBack


class MainActivity : AppCompatActivity() , OnAdsClosedCallBack,OnRewardedAddCloseCallBack{
    lateinit var addInitilizer: AddInitilizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        addInitilizer = AddInitilizer(applicationContext,this,BuildConfig.DEBUG)
        addInitilizer.activity = this
        addInitilizer.onAdsClosedCallBack = this
        addInitilizer.onRewardedAddCloseCallBack = this


//        addInitilizer = AddInitilizer(applicationContext,this,BuildConfig.DEBUG){
//            // on add close call back will run in this fun
//
//
//            val tag = it // this is the tag passed while displaying ad calling
//            // now place checks and use it for further call
//
//
//        }

        //by passing interface will start loading interstitial ad
        // or pass null if interstitial ad is not required for the activity
        // addInitilizer = AddInitilizer(applicationContext,this,null)


        //loading banner
        addInitilizer.loadBanner(findViewById(R.id.banner_container))

        // pass three paramenters for loading asnd displaying native add templateView, placeholder text, and ad container
        addInitilizer.loadNativeAdd(findViewById(R.id.nativeTemplateView),findViewById(R.id.temp_add_text),findViewById(R.id.add_container))

        // or if add loading is required,that will be displayed later then call
        //addInitilizer.loadNativeAdd(null,null,null)
        //for displaying native add call
        //addInitilizer.setnativeAddOnView(findViewById(R.id.nativeTemplateView))

        findViewById<View>(R.id.show_intestial).setOnClickListener {

//            addInitilizer.showInterstailAdd("Any tag")
            addInitilizer.showRewardVideo()
        }

    }

    override fun onCallBack(key: String?) {
        if(key.equals("Any tag"))
            Log.e("***InACt","Add closed "+key)
    }

    override fun onRewardSuccess() {
        Log.e("***Reward","Reward success")


    }

    override fun onRewardFailure() {
        Log.e("***Reward","Reward Failure")
    }

    override fun onLoadFailure() {
        Log.e("***Reward","load Failure")
    }

    override fun onLoadSuccess() {
        Log.e("***Reward","load Success")
        addInitilizer.showRewardVideo()
    }
}