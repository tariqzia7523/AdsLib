package com.temp.adslib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.module.ads.AddInitilizer
import com.module.ads.Application
import com.module.ads.MySharedPref

class SplashActivity : AppCompatActivity(), Application.OnShowAdCompleteListener {
    private var secondsRemaining: Long = 0L
    var ifcalledOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Application.globalOnShowAdCompleteListener = this //Important line

        loading_open_ad(2L)



    }
    fun loading_open_ad(seconds: Long) {
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000 + 1
            }
            override fun onFinish() {
                secondsRemaining = 0
                val application = application as? com.module.ads.Application
                if (application == null) {
                    gotoNextActivity()
                    return
                }
                application.showAdIfAvailable(
                    this@SplashActivity,this@SplashActivity)
            }
        }
        countDownTimer.start()
    }


    override fun onShowAdComplete() {
        gotoNextActivity()
    }

    fun gotoNextActivity() {
        if(!ifcalledOnce) {
            ifcalledOnce = true
            startActivity(Intent(this,MainActivity::class.java))
        }
       

    }

}