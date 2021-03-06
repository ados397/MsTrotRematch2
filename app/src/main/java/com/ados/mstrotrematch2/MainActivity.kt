package com.ados.mstrotrematch2

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.ViewPager
import com.ados.mstrotrematch2.dialog.NoticeDialog
import com.ados.mstrotrematch2.dialog.NoticeSubDialog
import com.ados.mstrotrematch2.dialog.QuestionDialog
import com.ados.mstrotrematch2.model.NoticeDTO
import com.ados.mstrotrematch2.model.QuestionDTO
import com.ados.mstrotrematch2.model.UpdateDTO
import com.ados.mstrotrematch2.page.FragmentPageCheering
import com.ados.mstrotrematch2.page.FragmentPageMusic
import com.ados.mstrotrematch2.page.FragmentPageRank
import com.ados.mstrotrematch2.page.FragmentPageVote
import com.fsn.cauly.CaulyAdView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.adfit.ads.AdListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notice_dialog.button_cancel
import kotlinx.android.synthetic.main.notice_sub_dialog.button_notice_ok
import kotlinx.android.synthetic.main.question_dialog.*

class MainActivity : AppCompatActivity() {

    var firestore : FirebaseFirestore? = null
    lateinit var mAdView : AdView
    lateinit var mInterstitialAd : InterstitialAd
    lateinit var mAdViewCauly : CaulyAdView
    var adType: String? = null
    lateinit var context : Context
    lateinit var fragmentPageMusic: FragmentPageMusic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        firestore = FirebaseFirestore.getInstance()

        // AD
        mAdView = findViewById(R.id.adView_admob)
        mAdViewCauly = findViewById(R.id.xmladview)

        InitAd()

        if (dateCheck()) {
            // ???????????? ??????
            versionCheck() // ?????? ?????? ?????? // ???????????? ??????

            fragmentPageMusic = FragmentPageMusic()
            val adapter = MyViewPagerAdapter(supportFragmentManager)
            adapter.addFragment(FragmentPageRank(), "??????")
            adapter.addFragment(FragmentPageVote(), "????????????")
            adapter.addFragment(fragmentPageMusic, "????????????")
            adapter.addFragment(FragmentPageCheering(), "????????????")

            viewPager.adapter = adapter
            tabs.setupWithViewPager(viewPager)
            tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))

            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {

                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    when (position) {
                        0, 1, 3 -> {
                            showAdCtrl(true)
                            // ?????? ???????????? ????????? ?????? ??? ??? ?????? ??? ?????? ???????????? ??????
                            showMainCtrl(true) // ?????? ????????? ??????
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //?????? ???????????? ??????
                            fragmentPageMusic.onPause()
                        }
                        else -> {
                            showAdCtrl(false)
                        }
                    }
                }
            })

            showNoticeDialog()
            showNoticeSubDialog()

            // ?????? ID ??????
            /*FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result?.token

                    // Log and toast
                    val msg = getString(R.string.msg_token_fmt, token)
                    Log.d(TAG, msg)
                    //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                })*/
        }


    }

    fun dateCheck() : Boolean {
        val result = android.provider.Settings.Global.getInt(contentResolver, android.provider.Settings.Global.AUTO_TIME, 0)
        if (result == 1) {
            println("DateTime Sync: On")
            return true
        } else {
            var question = QuestionDTO(
                QuestionDTO.STAT.ERROR,
                "?????? ??? ?????? ??????????????? ??????????????????.",
                "?????? ??? ?????? ??????????????? ?????? ??? ?????? ????????? ?????????.\n?????? ????????? ?????? ????????? ???????????????.\nhttps://info-hub.tistory.com/13"
            )

            val dialog = QuestionDialog(this, question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.showButtonOk(false)
            dialog.setButtonCancel("??????")
            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()
                appExit()
            }
            return false
        }
        return false
    }

    fun showNoticeDialog() {
        val dialog = NoticeDialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        dialog.button_cancel.setOnClickListener { // No
            dialog.dismiss()
        }
    }

    fun showNoticeSubDialog() {
        firestore?.collection("preferences")?.document("notice")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var noticeDTO = task.result.toObject(NoticeDTO::class.java)!!
                if (noticeDTO.visibility!!) {
                    val dialog = NoticeSubDialog(this)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.noticeDTO = noticeDTO
                    dialog.show()

                    dialog.button_notice_ok.setOnClickListener { // No
                        dialog.dismiss()
                    }
                }
            }
        }

    }

    fun InitAd() {

        // ????????? - ??????
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        var adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // ?????? ?????? ??????
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                adType = task.result!!["ad_banner2"].toString() // ???????????????

                adviewVisible()
            }
        }
    }

    fun adviewVisible() {
        when(adType) {
            getString(R.string.adtype_admob) -> {
                // ????????? ??????
                mAdView.setVisible(true)

                // ????????? ?????????
                mAdViewCauly.setVisible(false)
                adView_kakao.setVisible(false)
            }
            getString(R.string.adtype_cauly) -> {
                // ????????? ??????
                mAdViewCauly.setVisible(true)

                // ????????? ?????????
                mAdView.setVisible(false)
                adView_kakao.setVisible(false)
            }
            getString(R.string.adtype_adfit) -> {
                // ????????? ??????
                adView_kakao.setVisible(true)

                val adView_kakao = adView_kakao!!  // ?????? ?????? ???
                adView_kakao.setClientId("DAN-r8oaHTGIRZIZzKuH")  // ?????? ?????? ?????? ??????(clientId) ??????
                adView_kakao.setAdListener(object : AdListener {  // ?????? ?????? ????????? ??????

                    override fun onAdLoaded() {
                        //toast("Banner is loaded")
                    }

                    override fun onAdFailed(errorCode: Int) {
                        //toast("Failed to load banner :: errorCode = $errorCode")
                    }

                    override fun onAdClicked() {
                        //toast("Banner is clicked")
                    }

                })

                // lifecycle ?????? ????????? ??????
                // ?????? :: https://developer.android.com/topic/libraries/architecture/lifecycle
                // ?????? ???????????? ????????? BannerJava320x50Activity ??????
                lifecycle.addObserver(object : LifecycleObserver {

                    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                    fun onResume() {
                        adView_kakao.resume()
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                    fun onPause() {
                        adView_kakao.pause()
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    fun onDestroy() {
                        adView_kakao.destroy()
                    }

                })

                adView_kakao.loadAd()  // ?????? ??????

                // ????????? ?????????
                mAdView.setVisible(false)
                mAdViewCauly.setVisible(false)
            }
            else -> {
                // ?????? ?????????
                layout_adview.visibility  = View.GONE
            }
        }
    }

    fun View.setVisible(visible: Boolean) {
        visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun showMainCtrl(show: Boolean) {
        if (show) {
            tabs.visibility = View.VISIBLE
            //layout_adview.visibility  = View.VISIBLE
        } else {
            tabs.visibility = View.GONE
            //layout_adview.visibility  = View.GONE
        }
    }
    fun showAdCtrl(show: Boolean) {
        if (show) {
            when(adType) {
                getString(R.string.adtype_admob),
                getString(R.string.adtype_cauly),
                getString(R.string.adtype_adfit) -> {
                    layout_adview.visibility = View.VISIBLE
                }
                else -> {
                    // ?????? ?????????
                    layout_adview.visibility = View.GONE
                }
            }
        } else {
            layout_adview.visibility  = View.GONE
        }
    }

    fun callHallOfFameActivity() {
        var intent = Intent(this, HallOfFameActivity::class.java)
        //intent.putExtra("title", item.title)
        //intent.putExtra("docname", item.docname)
        startActivity(intent)
    }

    fun versionCheck() {
        val info: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val localVersion = info.versionName

        println("?????? ??????:$localVersion")
        // ???????????? ?????? ??????
        firestore?.collection("preferences")?.document("update")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                var updateDTO = task.result.toObject(UpdateDTO::class.java)!!
                var question : QuestionDTO
                var isEssential = false

                // ???????????? ??????
                // ?????? ?????? ???
                if (updateDTO.maintainance!!) {
                    question = QuestionDTO(
                        QuestionDTO.STAT.ERROR,
                        updateDTO.maintainanceTitle,
                        updateDTO.maintainanceDesc
                    )

                    val dialog = QuestionDialog(this, question)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                    dialog.showButtonOk(false)
                    dialog.setButtonCancel("??????")
                    dialog.button_question_cancel.setOnClickListener { // No
                        dialog.dismiss()
                        appExit()
                    }
                } else {
                    if (updateDTO.visibility!!) {
                        //if (!localVersion.equals(updateDTO.version)) {
                        if (localVersion < updateDTO.version.toString()) {
                            if (updateDTO.essential.equals("Y", ignoreCase = true)) { // ?????? ????????????
                                isEssential = true
                                question = QuestionDTO(
                                    QuestionDTO.STAT.ERROR,
                                    "??? ????????? ????????????.",
                                    "?????? ???????????? ?????? ??????????????????.\n???????????? ????????????."
                                )
                            } else { // ????????? ??????, ??????
                                question = QuestionDTO(
                                    QuestionDTO.STAT.WARNING,
                                    "??? ????????? ????????????.",
                                    "?????? ???????????? ???????????????????"
                                )
                            }

                            val dialog = QuestionDialog(this, question)
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.show()
                            dialog.setButtonOk("????????????")
                            dialog.button_question_cancel.setOnClickListener { // No
                                dialog.dismiss()
                                if (isEssential) {
                                    appExit()
                                }
                            }
                            dialog.button_question_ok.setOnClickListener { // Ok
                                dialog.dismiss()
                                moveMarket(updateDTO.updateUrl!!)
                                appExit()
                            }
                        }
                    }
                }
            }
        }

    }

    fun appExit() {
        finishAffinity() //?????? ?????? ?????? ??????????????? ???????????????. (API  16????????? ActivityCompat.finishAffinity())
        System.runFinalization() //?????? ???????????? ???????????? ??? ????????????, ?????? ???????????? ???????????????.
        System.exit(0) // ?????? ??????????????? ???????????????.
    }

    fun moveMarket(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    class MyViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        private val fragmentList: MutableList<Fragment> = ArrayList()
        private val titletList: MutableList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            titletList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titletList[position]

        }
    }
}