package com.module.ads

import android.app.Activity
import android.provider.Settings
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale

class HashedDeviceIdHelper {
    fun deviceHashId(context: Activity) : String{
        val android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
        val deviceId = md5(android_id)!!.uppercase(Locale.getDefault())
        Log.i("device id=", deviceId)
        return deviceId
    }

    private fun md5(s: String): String? {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}