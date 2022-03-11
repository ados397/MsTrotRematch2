package com.ados.mstrotrematch2.dialog


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.ados.mstrotrematch2.DatabaseHelper
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.BoardDTO
import com.fsn.cauly.CaulyAdInfo
import com.fsn.cauly.CaulyAdInfoBuilder
import com.fsn.cauly.CaulyInterstitialAd
import com.fsn.cauly.CaulyInterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.report_dialog.*
import java.util.*

class ReportDialog(context: Context, val item: BoardDTO, val parentActivity: Activity) : Dialog(context), View.OnClickListener {

    //var firestore : FirebaseFirestore? = null
    private val layout = R.layout.report_dialog
    private var adminString = ""
    var dbHandler : DatabaseHelper? = null
    var isReport = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        //firestore = FirebaseFirestore.getInstance()
        dbHandler = DatabaseHelper(context)

        edit_etc.visibility = View.GONE
        layout_admin.visibility = View.GONE

        button_report.setOnClickListener {
            when (radio_group.checkedRadioButtonId){
                R.id.radio_1 -> item.report = "욕설 및 불쾌한 내용"
                R.id.radio_2 -> item.report = "다른 가수 비난"
                R.id.radio_3 -> item.report = "음란하거나 선정적 내용"
                R.id.radio_4 -> item.report = "관련 없는 광고성 내용"
                R.id.radio_etc -> item.report = edit_etc.text.toString()
            }

            if (item.report.isNullOrEmpty()) {
                Toast.makeText(context,"신고 내용을 선택 해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val alphabat = arrayOf("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z")
                val rand_word = alphabat.get(Random().nextInt(26))

                var docname = "$rand_word${System.currentTimeMillis()}"
                item.time = Date()
                var firestore = FirebaseFirestore.getInstance()
                var tsDoc = firestore.collection("cheeringboard_report")?.document(docname)
                firestore?.runTransaction { transaction ->
                    transaction.set(tsDoc, item)
                }

                Toast.makeText(context,"신고가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                isReport = true
                if (radio_cheer.isChecked) { // 응원글 신고
                    if (dbHandler?.getblock(item.docname.toString()) == false) {
                        dbHandler?.updateBlock(item.docname.toString(), 1)
                        //Toast.makeText(context,"응원글 차단", Toast.LENGTH_SHORT).show()
                    } else {
                        dbHandler?.updateBlock(item.docname.toString(), 0)
                        //Toast.makeText(context,"응원글 차단 해제", Toast.LENGTH_SHORT).show()
                    }
                } else { // 사용자 신고
                    if (dbHandler?.getblock(item.name.toString()) == false) {
                        dbHandler?.updateBlock(item.name.toString(), 1)
                        //Toast.makeText(context,"응원글 차단", Toast.LENGTH_SHORT).show()
                    } else {
                        dbHandler?.updateBlock(item.name.toString(), 0)
                        //Toast.makeText(context,"응원글 차단 해제", Toast.LENGTH_SHORT).show()
                    }
                }


                showAd()

                dismiss()
            }
        }

        radio_group.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.radio_etc -> edit_etc.visibility = View.VISIBLE
                else -> edit_etc.visibility = View.GONE
            }
        }

        radio_1.setOnClickListener {
            adminString = adminString + "1"
            visibleAdminMode()
        }
        radio_2.setOnClickListener {
            adminString = adminString + "2"
            visibleAdminMode()
        }
        radio_3.setOnClickListener {
            adminString = adminString + "3"
            visibleAdminMode()
        }
        radio_4.setOnClickListener {
            adminString = adminString + "4"
            visibleAdminMode()
        }
        radio_etc.setOnClickListener {
            adminString = adminString + "5"
            visibleAdminMode()
        }

        button_block.setOnClickListener {
            Toast.makeText(context,"사용자 차단", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        button_release.setOnClickListener {
            Toast.makeText(context,"사용자 차단 해제", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        radio_cheer.setOnClickListener {
            text_user.text = "해당 응원글을 신고 합니다."
        }

        radio_user.setOnClickListener {
            text_user.text = "작성자 '${item.name}'(을)를 신고 합니다."
        }
    }

    private fun visibleAdminMode() {
        if (adminString.equals("1115551111111")) {
            layout_admin.visibility = View.VISIBLE
            Toast.makeText(context,"관리자모드 실행", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(v: View?) {

    }

    fun showAd() {
        // 광고 종류 획득
        var firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var ad_interstitial = task.result!!["ad_interstitial"]
                callInterstitial(ad_interstitial as String)
            }
        }
    }

    fun callInterstitial(interstitial : String) {
        when (interstitial) {
            parentActivity.getString(R.string.adtype_admob) -> {
                interstitialAdmob(true)
            }
            parentActivity.getString(R.string.adtype_cauly) -> {
                interstitialCauly(true)
            }
            else -> {

            }
        }
    }

    fun interstitialAdmob(isFirst : Boolean) {
        // 애드몹 - 전면
        var InterstitialAd = InterstitialAd(parentActivity)
        InterstitialAd.adUnitId = parentActivity.getString(R.string.admob_Interstitial_ad_unit_id) // 관리자모드
        InterstitialAd.loadAd(AdRequest.Builder().build())

        InterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (InterstitialAd.isLoaded) {
                    InterstitialAd.show()
                } else {
                    // 광고 호출 실패
                    if (isFirst) {
                        interstitialCauly(false)
                    }
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                if (isFirst) {
                    interstitialCauly(false)
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
                }
            }

            override fun onClosedInterstitialAd(p0: CaulyInterstitialAd?) {

            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {

            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(parentActivity)
    }
}