package com.temp.adslib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.module.ads.*

class SplashActivity : AppCompatActivity(),OnAdsClosedCallBack , OnSplashCallBack{
    private var secondsRemaining: Long = 0L
    var ifcalledOnce = false
    var addInitilizer : AddInitilizer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppOpenManager.onSplashCallBack = this
        AppOpenManager.callAppOpenAddOnlyOnce = false
        val mySharedPref = MySharedPref(this)
        mySharedPref.bannerID = "orignal_banner_id"
        mySharedPref.rewardID = "orignal_reward_id"
        mySharedPref.nativeID = "orignal_native_id"
        mySharedPref.interID = "orignal_interstitial_id"
        AddInitilizer.adCounter = 0
        AddInitilizer.startAppOpenAd(application,this,mySharedPref,true)
        AddInitilizer(applicationContext,this,true).getGDPRConsent("ca-app-pub-3940256099942544~3347511713",object : OnConsentResponse{
            override fun onConsentSuccess() {
                Toast.makeText(this@SplashActivity, "Suusess", Toast.LENGTH_SHORT).show()
                //TODO("Not yet implemented")
            }

            override fun onConsentFailure(code: Int, message: String) {
                Toast.makeText(this@SplashActivity, "failure", Toast.LENGTH_SHORT).show()
                //TODO("Not yet implemented")
            }
        })




//        Application.globalOnShowAdCompleteListener = this //Important line

        findViewById<View>(R.id.move_text).setOnClickListener {
            addInitilizer!!.showInterstailAdd("test")

        }



    }

    override fun onResume() {
        super.onResume()


    }
//    fun loading_open_ad(seconds: Long) {
//        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                secondsRemaining = millisUntilFinished / 1000 + 1
//            }
//            override fun onFinish() {
//                secondsRemaining = 0
//                val application = application as? com.module.ads.Application
//                if (application == null) {
//                    gotoNextActivity()
//                    return
//                }
//                application.showAdIfAvailable(
//                    this@SplashActivity,this@SplashActivity)
//            }
//        }
//        countDownTimer.start()
//    }
//
//
//    override fun onShowAdComplete() {
//        gotoNextActivity()
//    }

    fun gotoNextActivity() {
        if(!ifcalledOnce) {
            ifcalledOnce = true
            startActivity(Intent(this,MainActivity::class.java))
        }


    }

    override fun onCallBack(key: String?) {
        AppOpenManager.onSplashCallBack =  null
        startActivity(Intent(this@SplashActivity,MainActivity::class.java))
    }

    override fun afterOpenAddCallBack() {
        AppOpenManager.onSplashCallBack =  null
        startActivity(Intent(this@SplashActivity,MainActivity::class.java))
    }

}