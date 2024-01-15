package com.module.ads

interface OnConsentResponse {
    fun onConsentSuccess()
    fun onConsentFailure(code : Int, message : String)
}