package com.module.ads

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object appUtils {
    fun toastCall(context: Context,string: String){
        Toast.makeText(context,string,Toast.LENGTH_LONG).show()
    }

    fun moreApps(context: Context,accoutfullLink : String){
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(accoutfullLink))
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun feedback(context: Context,emailAddress : String) {
        try{
            val intent = Intent(Intent.ACTION_SEND)
            val recipients = arrayOf(emailAddress)
            intent.putExtra(Intent.EXTRA_EMAIL, recipients)
            intent.type = "text/html"
            intent.setPackage("com.google.android.gm")
            context.startActivity(Intent.createChooser(intent, "Send mail"))
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }

    fun shareApp(context: Context,appName : String,appId : String){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName)
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                """
        ${shareMessage}https://play.google.com/store/apps/details?id=${appId}
        
        
        """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: java.lang.Exception) {
            //e.toString();
        }
    }

    fun rateUsOnPlayStore(context: Context,appName : String,appId : String){
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${appId}")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${appId}")
                )
            )
        }
    }


}