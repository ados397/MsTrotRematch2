package com.ados.mstrotrematch2

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.fsn.cauly.CaulyAdInfo
import com.fsn.cauly.CaulyAdInfoBuilder
import com.fsn.cauly.CaulyInterstitialAd
import com.fsn.cauly.CaulyInterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast

class SplashActivity : AppCompatActivity() {

    var firestore : FirebaseFirestore? = null
    var mainIntent : Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainIntent = Intent(this, MainActivity::class.java)

        // 광고 종류 획득
        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                var ad_interstitial = task.result!!["ad_interstitial"]
                callInterstitial(ad_interstitial as String)
            }
        }


        //SystemClock.sleep(300)


    }

    fun callInterstitial(interstitial : String) {
        when (interstitial) {
            getString(R.string.adtype_admob) -> {
                interstitialAdmob(true)
            }
            getString(R.string.adtype_cauly) -> {
                interstitialCauly(true)
            }
            else -> {

            }
        }
    }

    fun interstitialAdmob(isFirst : Boolean) {
        // 애드몹 - 전면
        var InterstitialAd = InterstitialAd(this)
        InterstitialAd.adUnitId = getString(R.string.admob_Interstitial_ad_unit_id) // 관리자모드
        InterstitialAd.loadAd(AdRequest.Builder().build())

        InterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (InterstitialAd.isLoaded) {
                    InterstitialAd.show()
                } else {
                    // 광고 호출 실패
                    if (isFirst) {
                        interstitialCauly(false)
                    } else {
                        startActivity(mainIntent)
                        finish()
                    }
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                if (isFirst) {
                    interstitialCauly(false)
                } else {
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                startActivity(mainIntent)
                finish()
            }
        }
    }

    fun interstitialCauly(isFirst : Boolean) {
        var adInfo: CaulyAdInfo
        adInfo = CaulyAdInfoBuilder("avhXxFUQ").build()
        var interstial = CaulyInterstitialAd()
        interstial.setAdInfo(adInfo)

        val adCallback = object : CaulyInterstitialAdListener {
            override fun onReceiveInterstitialAd(p0: CaulyInterstitialAd?, p1: Boolean) {
                p0?.show()
            }

            override fun onFailedToReceiveInterstitialAd(p0: CaulyInterstitialAd?, p1: Int, p2: String?) {
                if (isFirst) {
                    interstitialAdmob(false)
                } else {
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun onClosedInterstitialAd(p0: CaulyInterstitialAd?) {
                startActivity(mainIntent)
                finish()
            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {

            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(this)
    }
}