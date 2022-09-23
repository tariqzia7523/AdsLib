package com.temp.adslib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.module.ads.AddInitilizer
import com.module.ads.MySharedPref


class MainActivity : AppCompatActivity() {
    lateinit var addInitilizer: AddInitilizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val mySharedPref = MySharedPref(this)
        mySharedPref.bannerID = "orignal_banner_id"
        mySharedPref.rewardID = "orignal_reward_id"
        mySharedPref.nativeID = "orignal_native_id"
        mySharedPref.interID = "orignal_interstitial_id"
        AddInitilizer.adCounter = 0


        addInitilizer = AddInitilizer(applicationContext,this,BuildConfig.DEBUG){
            // on add close call back will run in this fun


            val tag = it // this is the tag passed while displaying ad calling
            // now place checks and use it for further call


        }

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
            if(!addInitilizer.showInterstailAdd("Any tag")){
                // TODO actual task
            }
        }

    }
}