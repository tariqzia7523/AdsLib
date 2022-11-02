package com.module.ads

import android.R
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat.finishAffinity

class Utility {

    companion object {
        fun getOnboardStatus(context: Context): String {
            return context.getSharedPreferences("getOnboardStatus", Context.MODE_PRIVATE)
                .getString("getOnboardStatus", "yes").toString()
        }

        fun setOnboardStatus(ct: Context, is_first_run: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("getOnboardStatus", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("getOnboardStatus", is_first_run.toString())
            editor.commit()
        }

        fun getTheme(context: Context): String {
            return context.getSharedPreferences("app_theme", Context.MODE_PRIVATE)
                .getString("app_theme", "1").toString()
        }

        fun setTheme(ct: Context, app_theme: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("app_theme", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("app_theme", app_theme.toString())
            editor.commit()
        }

        fun getFirstRun(context: Context): String {
            return context.getSharedPreferences("is_first_run", Context.MODE_PRIVATE)
                .getString("is_first_run", "no").toString()
        }

        fun setFirstRun(ct: Context, is_first_run: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("is_first_run", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("is_first_run", is_first_run.toString())
            editor.commit()
        }

        fun getTimestamp(context: Context): String {
            return context.getSharedPreferences("setTimestamp", Context.MODE_PRIVATE)
                .getString("setTimestamp", "yes").toString()
        }

        fun setTimestamp(ct: Context, is_first_run: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("setTimestamp", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("setTimestamp", is_first_run.toString())
            editor.commit()
        }


        fun getFirstInstall(context: Context): String {
            return context.getSharedPreferences("is_first_install", Context.MODE_PRIVATE)
                .getString("is_first_install", "yes").toString()
        }

        fun setFirstInstall(ct: Context, is_first_run: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("is_first_install", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("is_first_install", is_first_run.toString())
            editor.commit()
        }


        fun getJsonFileName(context: Context): String {
            return context.getSharedPreferences("json_file_name_cloud", Context.MODE_PRIVATE)
                .getString("json_file_name_cloud", "nill").toString()
        }

        fun setJsonFileName(ct: Context, file_name: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("json_file_name_cloud", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("json_file_name_cloud", file_name.toString())
            editor.commit()
        }

        fun getAllJsonFileName(context: Context): String {
            return context.getSharedPreferences("json_file_name_phone", Context.MODE_PRIVATE)
                .getString("json_file_name_phone", "nill").toString()
        }

        fun setAllJsonFileName(ct: Context, file_name: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("json_file_name_phone", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("json_file_name_phone", file_name.toString())
            editor.commit()
        }


        fun getFolderName(context: Context): String {
            return context.getSharedPreferences("get_folder_name", Context.MODE_PRIVATE)
                .getString("get_folder_name", "nill").toString()
        }

        fun setFolderName(ct: Context, file_name: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("get_folder_name", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("get_folder_name", file_name.toString())
            editor.commit()
        }


        fun getPackageStatus(context: Context): String {
            return context.getSharedPreferences("package", Context.MODE_PRIVATE)
                .getString("package", "basic").toString()
        }

        fun setPackageStatus(ct: Context, package_id: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("package", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("package", package_id.toString())
            editor.commit()
        }

        fun getPackageAck(context: Context): String {
            return context.getSharedPreferences("packageAck", Context.MODE_PRIVATE)
                .getString("packageAck", "").toString()
        }

        fun setPackageAck(ct: Context, package_id: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("packageAck", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("packageAck", package_id.toString())
            editor.commit()
        }

        fun getAppUsername(context: Context): String {
            return context.getSharedPreferences("username", Context.MODE_PRIVATE)
                .getString("username", "").toString()
        }

        fun saveAppUsername(ct: Context, username: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("username", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("username", username.toString())
            editor.apply()
        }

        fun getUsernameProfilePicUrl(context: Context): String {
            return context.getSharedPreferences("username", Context.MODE_PRIVATE)
                .getString("url", "").toString()
        }

        fun setUsernameProfilePicUrl(ct: Context, url: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("username", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("url", url.toString())
            editor.apply()
        }

        fun saveCognitoIdentityId(ct: Context, identityId: String?) {
            val sharedPreferences: SharedPreferences =
                ct.getSharedPreferences("IdentityDetails", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("identityId", identityId)
            editor.apply()
        }

        fun getCognitoIdentityId(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences("IdentityDetails", Context.MODE_PRIVATE)
            return sharedPreferences.getString("identityId", "")
        }

    }
}