# AdsLib
 
This project contains library for easy implementation of ad mob according to ad policies.
 
 
 # implementation 
 Add flowing in 
  maven { url 'https://jitpack.io' }
  
  then following line in app gradle
  
 implementation 'com.github.tariqzia7523:AdsLib:1.0.0'

# Usage 
you can refer to MainActivity in sample project
first of all set orignal id and ad count on splash using following code,orignal id will only be called on release version 

        val mySharedPref = MySharedPref(this)
        mySharedPref.bannerID = "orignal_banner_id"
        mySharedPref.rewardID = "orignal_reward_id"
        mySharedPref.nativeID = "orignal_native_id"
        mySharedPref.interID = "orignal_interstitial_id"
        mySharedPref.appOpenID = "orignal_app_opn_id"
        AddInitilizer.adCounter = 0
        //ad count refers to show interstial ad after these number of clicks

make a global variable 
  
  addInitilizer = AddInitilizer(applicationContext,this, BuildConfig.DEBUG){
             // on add close call back will run in this fun


             val tag = it // this is the tag passed while displaying ad calling
             // now place checks and use it for further call


         }
         
     by passing interface will start loading interstitial ad
     or pass null if interstitial ad is not required for the activity
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
        
        use following code in layouts
        
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

        <TextView
            android:id="@+id/temp_add_text"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="@string/ad_will_display_hare" />
    </RelativeLayout>

you can use 
app:gnt_template_type="@layout/gnt_small_template_view" or app:gnt_template_type="@layout/gnt_medium_template_view"

you can also use TemplateView sepratly


 <com.module.ads.TemplateView
   android:id="@+id/nativeTemplateView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:visibility="gone"
    android:background="@drawable/gnt_outline_shape"
    app:gnt_template_type="@layout/gnt_medium_template_view"/>
            

to add open app add following line of code in Application class oncreate method
val appOpenManager = AppOpenManager(this, MySharedPref(this), BuildConfig.DEBUG)


# InApp purcheses
project has in app purcheses to remove ad calls use following method.
addInitilizer.goAddFree()


## Showing ad in list (RecylerView)

  val list = ArrayList<String>()
        val myAdapterForAppList = MyAdapter(this@MainActivity, list)
        val admobNativeAdAdapter: AdmobNativeAdAdapter = AdmobNativeAdAdapter.Builder.with( this@MainActivity, myAdapterForAppList, "small", BuildConfig.DEBUG).adItemInterval(8).setContext(this@MainActivity).build()
        recylerView.adapter = admobNativeAdAdapter
        recylerView.layoutManager = LinearLayoutManager(this@MainActivity)



