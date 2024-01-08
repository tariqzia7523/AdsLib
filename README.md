# AdsLib

[![](https://jitpack.io/v/tariqzia7523/Adslib.svg)](https://jitpack.io/#tariqzia7523/Adslib)


 
This project contains library for easy implementation of ad mob according to ad policies.
 
 
 # implementation 
 Add flowing in 
  maven { url 'https://jitpack.io' }
  
  then following line in app gradle
  
 implementation 'com.github.tariqzia7523:AdsLib:tag'

# Usage


## OpenApp Add
Open app Ad is with different flow. For this make an application class in that add following code

    class App : Application() {
        override fun onCreate() {
            super.onCreate()
            val mySharedPref = MySharedPref(this)
            mySharedPref.appOpenID ="orignal_app_id"
            AppOpenManager(this, mySharedPref,BuildConfig.DEBUG)
        }
    }

In SplashClass implement OnSplashCallBack on class level and following line in oncreate

    AppOpenManager.onSplashCallBack = this

        val mySharedPref = MySharedPref(this)
        mySharedPref.bannerID = "orignal_banner_id"
        mySharedPref.rewardID = "orignal_reward_id"
        mySharedPref.nativeID = "orignal_native_id"
        mySharedPref.interID = "orignal_interstitial_id"
        AddInitilizer.adCounter = 0

And

     AddInitilizer.getInstance(applicationContext,this,BuildConfig.DEBUG)


[//]: # (For older versions )

[//]: # (And if you need to use interstial add which will be required in most of cases)

[//]: # ()
[//]: # (    AddInitilizer.getInstance&#40;applicationContext,this,BuildConfig.DEBUG&#41;.loadIntersitialAdd&#40;&#41;)

[//]: # ( )

you will get method afterOpenAddCallBack. In this must add line

    AppOpenManager.onSplashCallBack = null

add adjust furtherFlow 


## Interstitial Ad

implement interface on class level "OnAdsClosedCallBack" 
it will give you a method "onCallBack" which will trigger when interstitial call is closed

add following line in onResume Method

    val addInitilizer = AddInitilizer.getInstance(applicationContext,this,BuildConfig.DEBUG)

### To show Interstitial Ad

    addInitilizer.showInterstailAdd("Any tag")

[//]: # (for older verion)

[//]: # ()
[//]: # (    if&#40;!addInitilizer.showInterstailAdd&#40;"Any tag"&#41;&#41;{)

[//]: # (        Log.e&#40;"***InACt","Add not calleed"&#41;)

[//]: # (    })

#### onCallBack

you will get same tag in this method which is passed while calling the add yu can use that tag to differ in all further call

## Banner Ad

    addInitilizer.loadBanner(findViewById(R.id.banner_container))

## Native ad

Add Following block in layout

     <RelativeLayout
        android:id="@+id/add_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@drawable/gnt_outline_shape"
        android:minHeight="@dimen/_130sdp"
        android:layout_marginLeft="@dimen/_10sdp"
        android:layout_marginRight="@dimen/_10sdp"
        android:layout_marginTop="@dimen/_10sdp"
        android:layout_alignParentBottom="true">

        <com.module.ads.TemplateView
            android:id="@+id/nativeTemplateView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:visibility="gone"
            app:gnt_template_type="@layout/gnt_small_template_view" />

            <!--  app:gnt_template_type="@layout/gnt_medium_template_view"  -->

        <TextView
            android:id="@+id/temp_add_text"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="@string/ad_will_display_hare" />
    </RelativeLayout>

Add Following line in backend code

     addInitilizer.loadNativeAdd(findViewById(R.id.nativeTemplateView),findViewById(R.id.temp_add_text),findViewById(R.id.add_container))



# InApp purcheses
project has in app purcheses to remove ad calls use following method.
addInitilizer.goAddFree()


## Showing ad in list (RecylerView)

  val list = ArrayList<String>()
 
        val myAdapterForAppList = MyAdapter(this@MainActivity, list)
 
        val admobNativeAdAdapter: AdmobNativeAdAdapter = AdmobNativeAdAdapter.Builder.with( this@MainActivity, myAdapterForAppList, "small", BuildConfig.DEBUG).adItemInterval(8).setContext(this@MainActivity).build()
 
        recylerView.layoutManager = LinearLayoutManager(this@MainActivity)
 
       recylerView.adapter = admobNativeAdAdapter

# To Display add with madiation (fb-admob)

 Add following in Application class
    
    AddInitilizer.madiationInitilization(this)





 



