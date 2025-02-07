package com.module.ads

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.content.IntentSender
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.NonNull
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.Arrays
import java.util.concurrent.atomic.AtomicBoolean


class AddInitilizer {
    // new version
    lateinit var context: Context
    private val isMobileAdsInitializeCalled : AtomicBoolean
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager


    var mInterstitialAd: InterstitialAd? = null
    var progressDialog : ProgressDialog? = null
    val TAG = "***Ads"
    var onAdsClosedCallBack : OnAdsClosedCallBack? = null
    var onRewardedAddCloseCallBack : OnRewardedAddCloseCallBack? = null
    var adLoader: AdLoader? = null
    var nativeAd : NativeAd? = null
    var mRewardedAd : RewardedAd? = null
    var mySharedPref : MySharedPref
    var isDebagRunning = true
    var gloabalSelectedKey = ""
    var activity: Activity? = null
    var isInterAddLoading = false


    lateinit var mAppUpdateManager: AppUpdateManager
    private val RC_APP_UPDATE = 11
    var installStateUpdatedListener: InstallStateUpdatedListener =
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
//                        popupSnackbarForCompleteUpdate();
                } else if (state.installStatus() == InstallStatus.INSTALLED) {
                    mAppUpdateManager.unregisterListener(this)
                } else {
                    Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus())
                }
            }
        }


    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                && purchases != null
            ) {
                MySharedPref(context).setPurcheshed(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    activity!!.recreate()
                };
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }


    constructor(context: Context, activity: Activity,isDebugRunning : Boolean) {
        this.context = context
        this.activity = activity
        this.isDebagRunning = isDebugRunning
        mySharedPref = MySharedPref(context)
        isMobileAdsInitializeCalled = AtomicBoolean(false)
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)

        Log.e("***Constr","consturcor called")
    }

    fun getGDPRConsent(application: Application,applicationContext: Context,appid: String,onConsentResponse: OnConsentResponse){
        googleMobileAdsConsentManager.gatherConsent(activity!!,appid,isDebagRunning) { error ->
            if (error != null) {
                Log.e(TAG+"GDPR", "Consent not obtained in current session.")
                Log.e(TAG+"GDPR", "GDPR Logs ${error.errorCode}: ${error.message}")
                onConsentResponse.onConsentFailure(error.errorCode,error.message)
            }else{
                Log.e(TAG+"GDPR","Error == null")
                if(!mySharedPref.isContestGiven) {
                    AppOpenManager(application, applicationContext,mySharedPref,BuildConfig.DEBUG)
                    mySharedPref.isContestGiven = true
                }
                onConsentResponse.onConsentSuccess()
            }
        }
//        googleMobileAdsConsentManager.gatherConsent(activity!!,appid,isDebagRunning,object : GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener{
//            override fun consentGatheringComplete(error: FormError?) {
//                //TODO("Not yet implemented")
//            }
//        })
    }


    fun setOnAdsClosedListener(onAdsClosedCallBack: OnAdsClosedCallBack){
        this.onAdsClosedCallBack = onAdsClosedCallBack
    }


    fun loadRewardedAdd(onRewardedAddCloseCallBack: OnRewardedAddCloseCallBack){
        this.onRewardedAddCloseCallBack  = onRewardedAddCloseCallBack
        val adRequest: AdRequest = AdRequest.Builder().build()

        if(mySharedPref.isPurshed){
            return
        }

        if(!canRequestAd()){
            onRewardedAddCloseCallBack.onLoadFailure()
            return
        }


        if(mySharedPref.rewaredVideocurrentCount >= mySharedPref.rewaredVideoCout){
            RewardedAd.load(context, AddIds.getRewardId(context,isDebagRunning),
                adRequest, object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                        // Handle the error.
                        Log.e(TAG, loadAdError.message)
                        mRewardedAd = null
                        onRewardedAddCloseCallBack.onLoadFailure()
                    }

                    override fun onAdLoaded(@NonNull rewardedAd: RewardedAd) {
                        mRewardedAd = rewardedAd
                        onRewardedAddCloseCallBack.onLoadSuccess()
                        Log.e(TAG, "Ad was loaded.")
                        mRewardedAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.e(TAG, "Ad was shown.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show.")
                                onRewardedAddCloseCallBack.onRewardFailure()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                onRewardedAddCloseCallBack.onRewardSuccess()
                                mySharedPref.rewaredVideocurrentCount = 0
                                Log.e(TAG, "Ad was dismissed.")
                                mRewardedAd = null
                            }
                        }
                    }
                })
        }

    }



    fun showRewardVideo(){

        if(mySharedPref.isPurshed){
           try{
               onRewardedAddCloseCallBack!!.onRewardSuccess()
               return
           }catch (e : java.lang.Exception){
               e.printStackTrace()
           }
        }
        if(mySharedPref.rewaredVideocurrentCount >= mySharedPref.rewaredVideoCout){
            if(mRewardedAd != null && onRewardedAddCloseCallBack != null){
                if (mRewardedAd != null) {
                    mRewardedAd!!.show(activity!!) { rewardItem -> // Handle the reward.
                        Log.e(TAG, "The user earned the reward.")

                    }
                } else {
                    Log.e(TAG, "The rewarded ad wasn't ready yet.")
                    onRewardedAddCloseCallBack!!.onRewardFailure()
                }
            }else{
                if(onRewardedAddCloseCallBack != null) {
                    onRewardedAddCloseCallBack!!.onLoadFailure()
                    loadRewardedAdd(onRewardedAddCloseCallBack!!)
                }

            }
        }else{
            mySharedPref.rewaredVideocurrentCount += 1
            onRewardedAddCloseCallBack!!.onRewardSuccess()
            loadRewardedAdd(onRewardedAddCloseCallBack!!)
        }

    }


    fun checkUpdatesAndReviews(){
        try{
            mAppUpdateManager = AppUpdateManagerFactory.create(activity!!)
            mAppUpdateManager.registerListener(installStateUpdatedListener)
            mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE /*AppUpdateType.FLEXIBLE*/)) {
                    try {
                        mAppUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE /*AppUpdateType.FLEXIBLE*/, activity!!, RC_APP_UPDATE)
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }
                } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
//                popupSnackbarForCompleteUpdate();
                } else {
                    Log.e(TAG, "checkForAppUpdateAvailability: something else")
                }
            }
            if (MySharedPref(activity!!).userReview) {
                val manager = ReviewManagerFactory.create(activity!!)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task: Task<ReviewInfo?> ->
                    if (task.isSuccessful) {
                        // We can get the ReviewInfo object
                        val reviewInfo = task.result
                        val flow = manager.launchReviewFlow(activity!!, reviewInfo!!)
                        flow.addOnCompleteListener { task2: Task<Void?>? ->
                            // The flow has finished. The API does not indicate whether the user
                            // reviewed or not, or even whether the review dialog was shown. Thus, no
                            // matter the result, we continue our app flow.
                            MySharedPref(activity!!).isUserReviwed = true
                        }
                    } else {
                        // There was some problem, log or handle the error code.
                        task.exception!!.printStackTrace()
                    }
                }
            }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }

    }


    fun loadBanner(bannerContainer: FrameLayout, tempView : View? = null) {
        if(!canRequestAd()){
            bannerContainer.visibility = View.GONE
            return
        }

        if (!MySharedPref(activity!!).isPurshed) {
            val adView = AdView(activity!!)
            adView.adUnitId = AddIds.getBannerId(context,isDebagRunning)
            adView.adListener = object : AdListener() {
                override fun onAdClosed() {
                    super.onAdClosed()
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    bannerContainer.visibility = View.GONE
                }
                override fun onAdOpened() {
                    super.onAdOpened()
                }
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    try{
                        if(tempView != null)
                            tempView.visibility = View.GONE
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                }
                override fun onAdClicked() {
                    super.onAdClicked()
                }
                override fun onAdImpression() {
                    super.onAdImpression()
                }
            }
            val adRequest = AdRequest.Builder().build()
            adView.setAdSize(getAdSize())
            adView.loadAd(adRequest)
            bannerContainer.addView(adView)
        }else{
            bannerContainer.visibility = View.GONE
        }
    }
    private fun getAdSize(): AdSize {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display = activity!!.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity!!, adWidth)
    }


//    fun showInterstailAdd(key :String) : Boolean{
//        gloabalSelectedKey = key
//        if(currentAdCounter < adCounter){
//            currentAdCounter += 1;
//            Log.e(TAG,"currant ad counter "+ currentAdCounter)
//            return false
//        }
//        if(mInterstitialAd == null){
//            Log.e(TAG,"mInterstitialAd  "+ "null")
//            return false
//        }else{
//            Log.e(TAG,"mInterstitialAd  "+ "add call ")
//            showProgressDialog()
//            Handler(Looper.getMainLooper()).postDelayed({
//                try{
//                    hidePRogressDialog()
//                    currentAdCounter = 0;
//                    mInterstitialAd!!.show(activity!!)
//                    mInterstitialAd!!.setFullScreenContentCallback(object :
//                        FullScreenContentCallback() {
//                        override fun onAdDismissedFullScreenContent() {
//                            // Called when fullscreen content is dismissed.
//                            Log.e(TAG, "The ad was dismissed.")
////                                startactivity!!(globelintent)
//                            isInterAddLoading = false
//                            hidePRogressDialog()
//                            onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
//                            loadIntersitialAdd()
//                        }
//
//                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                            // Called when fullscreen content failed to show.
//                            Log.e(TAG, "The ad failed to show.")
//                            isInterAddLoading = false
//                            hidePRogressDialog()
//                            onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
//                        }
//
//                        override fun onAdShowedFullScreenContent() {
//                            // Called when fullscreen content is shown.
//                            // Make sure to set your reference to null so you don't
//                            // show it a second time.
//                            isInterAddLoading = false
//                            mInterstitialAd = null
//                            Log.e(TAG, "The ad was shown.")
//                        }
//                    })
//                }catch (e : Exception){
//                    e.printStackTrace()
//                    onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
//                }
//
//            }, 800)
//            return true
//        }
//    }

    fun showInterstailAdd(key :String){
        gloabalSelectedKey = key
        if(!canRequestAd()){
            onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
            return
        }
        if(currentAdCounter < adCounter){
            currentAdCounter += 1;
            Log.e(TAG,"currant ad counter "+ currentAdCounter)
            onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
            return
        }

        loadIntersitialAdd()

//        if(mInterstitialAd == null){
//            Log.e(TAG,"mInterstitialAd  "+ "null")
//            if(!isInterAddLoading) loadIntersitialAdd()
//            return false
//        }else{
//            Log.e(TAG,"mInterstitialAd  "+ "add call ")
//            showProgressDialog()
//            Handler(Looper.getMainLooper()).postDelayed({
//                progressDialog!!.dismiss()
//                currentAdCounter = 0;
//                mInterstitialAd!!.show(activity!!)
//            }, 900)
//            return true
//        }
    }
    fun showProgressDialog(){
        try{
            progressDialog = ProgressDialog(activity)
            progressDialog!!.setMessage(activity!!.getString(R.string.displaying_ad))
            progressDialog!!.show()
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
    fun hidePRogressDialog(){
        try{
            progressDialog!!.dismiss()
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

     private fun loadIntersitialAdd() {

        if(!MySharedPref(activity!!).isPurshed){
            showProgressDialog()
            isInterAddLoading = true
            currentAdCounter = 0;
            Log.e("***addLoading", "Add Loading started.")
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(activity!!, AddIds.getInterstitialId(context,isDebagRunning), adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        isInterAddLoading = false
                        mInterstitialAd = interstitialAd
                        Log.e(TAG, "onAdLoaded")
                        try{
                            hidePRogressDialog()
                            currentAdCounter = 0;
                            mInterstitialAd!!.show(activity!!)
                            mInterstitialAd!!.setFullScreenContentCallback(object :
                                FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    // Called when fullscreen content is dismissed.
                                    Log.e(TAG, "The ad was dismissed.")
//                                startactivity!!(globelintent)
                                    isInterAddLoading = false
                                    hidePRogressDialog()
                                    onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
//                                    loadIntersitialAdd()
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when fullscreen content failed to show.
                                    Log.e(TAG, "The ad failed to show.")
                                    isInterAddLoading = false
                                    hidePRogressDialog()
                                    onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
                                }

                                override fun onAdShowedFullScreenContent() {
                                    // Called when fullscreen content is shown.
                                    // Make sure to set your reference to null so you don't
                                    // show it a second time.
                                    isInterAddLoading = false
                                    mInterstitialAd = null
                                    Log.e(TAG, "The ad was shown.")
                                }
                            })
                        }catch (e : Exception){
                            e.printStackTrace()
                            onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
                        }

//                        mInterstitialAd!!.show(activity!!)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.message)
                        isInterAddLoading = false
                        mInterstitialAd = null
                        hidePRogressDialog()
                        onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
                    }
                })
        }else{
            onAdsClosedCallBack!!.onCallBack(gloabalSelectedKey)
        }

    }

    fun getNativeAddIfAvalible(): NativeAd?{
        return nativeAd
    }

    fun setnativeAddOnView(templateView: TemplateView?) : Boolean{
        if(nativeAd != null ){
            val styles = NativeTemplateStyle.Builder().build()
            templateView!!.setStyles(styles)
            templateView.setNativeAd(nativeAd)
            templateView.visibility = View.VISIBLE
            return true
        }else{
            templateView!!.visibility = View.GONE
            return false
        }


    }

     fun loadNativeAdd(templateView : TemplateView?,placeHolderView : View?,relativeLayout: RelativeLayout?) {
        if(!canRequestAd()){
            try{
                relativeLayout!!.visibility = View.GONE
            }catch (e : Exception){
                e.printStackTrace()
            }
            return
        }
         if(!MySharedPref(activity!!).isPurshed){
            adLoader = AdLoader.Builder(activity!!, AddIds.getNativeId(context,isDebagRunning))
                .withAdListener(object : AdListener() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        Log.e(TAG, "onAdClosed")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        Log.e(TAG, "onAdFailedToLoad " + loadAdError.message)
                        Log.e(TAG, "onAdFailedToLoad $loadAdError")
                        try{
                            relativeLayout!!.visibility = View.GONE
                        }catch (e : Exception){
                            e.printStackTrace()
                        }
                    }

                    override fun onAdOpened() {
                        super.onAdOpened()
                        Log.e(TAG, "onAdOpened")
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.e(TAG, "onAdLoaded")
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        Log.e(TAG, "onAdClicked")
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        Log.e(TAG, "onAdImpression")
                    }
                })
                .forNativeAd(object : NativeAd.OnNativeAdLoadedListener {
                    private val background: ColorDrawable? = null
                    override fun onNativeAdLoaded(ad: NativeAd) {
                        // Assumes you have a placeholder FrameLayout in your View layout
                        // (with id fl_adplaceholder) where the ad is to be placed.
                        nativeAd = ad
                        val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(background).build()
                        try{
                            templateView!!.setStyles(styles)
                            templateView.setNativeAd(ad)
                            templateView.visibility = View.VISIBLE

                        }catch (e : Exception){
                            e.printStackTrace()
                        }
                        try{
                            placeHolderView!!.visibility = View.GONE
                        }catch (e : Exception){
                            e.printStackTrace()
                        }


                    }
                }).build()
            val adRequest = AdRequest.Builder().build()
            adLoader!!.loadAd(adRequest)
            Log.e(TAG, "add is loading")
            Log.e(TAG, "build called")
        }else{
            try{
                relativeLayout!!.visibility = View.GONE
            }catch (e : Exception){
                e.printStackTrace()
            }
        }

    }

    fun goAddFree() {
        showProgressDialog()
        Log.e(TAG, "add free metyhod called")
        val billingClient = BillingClient.newBuilder(activity!!)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.e(TAG, "ready to purchess")
                    val skuList: MutableList<String> = ArrayList()
                    skuList.add("premium_upgrade")
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                        hidePRogressDialog()
                        Log.e(TAG, "sku details " + skuDetailsList!!.size)
                        // Process the result.
                        Log.e(TAG, "skuDetailsList.get(0).getTitle() " + skuDetailsList[0].title)
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList[0])
                            .build()
                        val responseCode = billingClient.launchBillingFlow(
                            activity!!,
                            billingFlowParams
                        ).responseCode
                        Log.e(TAG, "responseCode $responseCode")
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.e(TAG, "service disconnected")
            }
        })
    }

    fun isNetAvailable(): Boolean{
        var isNetAvlibel = false
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = mgr.activeNetworkInfo

        if (netInfo != null) {
            isNetAvlibel = netInfo.isConnected
        } else {
            isNetAvlibel = false
        }
        return isNetAvlibel
    }

    companion object{
        var adCounter : Int = 2
        private var currentAdCounter : Int = 0
        fun madiationInitilization(context: Context){
            MobileAds.initialize(context,
                OnInitializationCompleteListener { initializationStatus ->
                    val statusMap = initializationStatus.adapterStatusMap
                    for (adapterClass in statusMap.keys) {
                        val status = statusMap[adapterClass]
                        Log.e(
                            "MyApp", String.format(
                                "Adapter name: %s, Description: %s, Latency: %d",
                                adapterClass, status!!.description, status.latency
                            )
                        )
                    }

                    // Start loading ads here...
                })
        }

        fun initAd(context: Context){
            MobileAds.initialize(context) {}
        }

        fun startAppOpenAd(application: android.app.Application,applicationContext : Context,mySharedPref: MySharedPref,isDebugRunning: Boolean){
            if(mySharedPref.isContestGiven)
                AppOpenManager(application, applicationContext,mySharedPref,BuildConfig.DEBUG)
        }
//        var instance : AddInitilizer? = null
//        fun getInstance(context: Context,activity: Activity? ,isDebugRunning : Boolean) : AddInitilizer{
//            if(instance == null)
//                instance = AddInitilizer(context,activity,isDebugRunning)
//            instance!!.activity = activity
//            return instance!!
//        }

        fun addDevice(id: String){
            RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(id))
        }
    }

    fun canRequestAd() : Boolean{
        val canRequestAd =  googleMobileAdsConsentManager.canRequestAds
        Log.e(TAG,"can request ad $canRequestAd")
        return canRequestAd
    }

    fun showGDPR(link : String){
        googleMobileAdsConsentManager.showPrivacyOptionsForm(activity!!) { formError ->
            if (formError != null) {
                Toast.makeText(activity, formError.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

}