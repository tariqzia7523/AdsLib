package com.module.ads

import android.content.Context
import android.content.SharedPreferences

class MySharedPref(var context: Context) {
    var sharedPreferences: SharedPreferences
    fun setPurcheshed(value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(context.getString(R.string.is_purchsed), value).apply()
    }

    var bannerID: String
        get() = sharedPreferences.getString(context.getString(R.string.banner_id), "")!!
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(context.getString(R.string.banner_id), value).apply()
        }

    var interID: String
        get() = sharedPreferences.getString(context.getString(R.string.inter_id), "")!!
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(context.getString(R.string.inter_id), value).apply()
        }

    var nativeID: String
        get() = sharedPreferences.getString(context.getString(R.string.native_id), "")!!
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(context.getString(R.string.native_id), value).apply()
        }

    var rewardID: String
        get() = sharedPreferences.getString(context.getString(R.string.reward_id), "")!!
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(context.getString(R.string.reward_id), value).apply()
        }

    var appOpenID: String
        get() = sharedPreferences.getString(context.getString(R.string.app_open_id), "")!!
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(context.getString(R.string.app_open_id), value).apply()
        }


    var isContestGiven: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.is_consect_given), false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(context.getString(R.string.is_consect_given), value).apply()
        }

    val isPurshed: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.is_purchsed), false)
    var isUserReviwed: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.user_reviwed), false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(context.getString(R.string.user_reviwed), value).apply()
        }
    val userReview: Boolean
        get() = if (!isUserReviwed && appCount > 2) {
            true
        } else false
    var userIntro: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.user_intro), false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(context.getString(R.string.user_intro), value).apply()
        }
    var appCount: Int
        get() = sharedPreferences.getInt(context.getString(R.string.app_count), 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(context.getString(R.string.app_count), value).apply()
        }
    var privacyAccepted: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.privacy_accepted), false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(context.getString(R.string.privacy_accepted), value).apply()
        }

    var rewaredVideoCout: Int
        get() = sharedPreferences.getInt(context.getString(R.string.rewarded_video_add_count), 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(context.getString(R.string.rewarded_video_add_count), value).apply()
        }

    var rewaredVideocurrentCount: Int
        get() = sharedPreferences.getInt(context.getString(R.string.rewarded_video_add_count_cuurent), 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(context.getString(R.string.rewarded_video_add_count_cuurent), value).apply()
        }
    init {
        sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.my_pref), Context.MODE_PRIVATE)
    }
}