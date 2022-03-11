package com.ados.mstrotrematch2

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.ViewPager
import com.ados.mstrotrematch2.dialog.NoticeDialog
import com.ados.mstrotrematch2.dialog.QuestionDialog
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
import kotlinx.android.synthetic.main.notice_dialog.*
import kotlinx.android.synthetic.main.notice_dialog.button_cancel
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
            // 업데이트 확인
            versionCheck() // 시즌 변경 작업 // 시즌교체 작업

            fragmentPageMusic = FragmentPageMusic()
            val adapter = MyViewPagerAdapter(supportFragmentManager)
            adapter.addFragment(FragmentPageRank(), "랭킹")
            adapter.addFragment(FragmentPageVote(), "투표하기")
            adapter.addFragment(fragmentPageMusic, "노래듣기")
            adapter.addFragment(FragmentPageCheering(), "응원하기")

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
                            // 전체 화면으로 동영상 시청 중 탭 변경 시 화면 원상복귀 시킴
                            showMainCtrl(true) // 메인 컨트롤 복구
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //세로 화면으로 고정
                            fragmentPageMusic.onPause()
                        }
                        else -> {
                            showAdCtrl(false)
                        }
                    }
                }
            })

            showNoticeDialog()

            // 토큰 ID 획득
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
                "날짜 및 시간 자동설정이 꺼져있습니다.",
                "날짜 및 시간 자동설정을 설정 후 앱을 이용해 주세요.\n설정 방법은 아래 링크를 참조하세요.\nhttps://info-hub.tistory.com/13"
            )

            val dialog = QuestionDialog(this, question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.showButtonOk(false)
            dialog.setButtonCancel("확인")
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

    fun InitAd() {

        // 애드몹 - 배너
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        var adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // 광고 종류 획득
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                adType = task.result!!["ad_banner2"].toString() // 관리자모드

                adviewVisible()
            }
        }
    }

    fun adviewVisible() {
        when(adType) {
            getString(R.string.adtype_admob) -> {
                // 애드몹 활성
                mAdView.setVisible(true)

                // 나머지 비활성
                mAdViewCauly.setVisible(false)
                adView_kakao.setVisible(false)
            }
            getString(R.string.adtype_cauly) -> {
                // 카울리 활성
                mAdViewCauly.setVisible(true)

                // 나머지 비활성
                mAdView.setVisible(false)
                adView_kakao.setVisible(false)
            }
            getString(R.string.adtype_adfit) -> {
                // 애드핏 활성
                adView_kakao.setVisible(true)

                val adView_kakao = adView_kakao!!  // 배너 광고 뷰
                adView_kakao.setClientId("DAN-r8oaHTGIRZIZzKuH")  // 할당 받은 광고 단위(clientId) 설정
                adView_kakao.setAdListener(object : AdListener {  // 광고 수신 리스너 설정

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

                // lifecycle 사용 가능한 경우
                // 참조 :: https://developer.android.com/topic/libraries/architecture/lifecycle
                // 사용 불가능한 경우는 BannerJava320x50Activity 참조
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

                adView_kakao.loadAd()  // 광고 요청

                // 나머지 비활성
                mAdView.setVisible(false)
                mAdViewCauly.setVisible(false)
            }
            else -> {
                // 모두 비활성
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
                    // 모두 비활성
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

        println("로컬 버전:$localVersion")
        // 서버에서 버전 확인
        firestore?.collection("preferences")?.document("update")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                var updateDTO = task.result.toObject(UpdateDTO::class.java)!!
                var question : QuestionDTO
                var isEssential = false

                // 시즌교체 작업
                // 서버 점검 중
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
                    dialog.setButtonCancel("확인")
                    dialog.button_question_cancel.setOnClickListener { // No
                        dialog.dismiss()
                        appExit()
                    }
                } else {
                    if (updateDTO.visibility!!) {
                        //if (!localVersion.equals(updateDTO.version)) {
                        if (localVersion < updateDTO.version.toString()) {
                            if (updateDTO.essential.equals("Y", ignoreCase = true)) { // 필수 업데이트
                                isEssential = true
                                question = QuestionDTO(
                                    QuestionDTO.STAT.ERROR,
                                    "새 버전이 있습니다.",
                                    "현재 버전에서 실행 불가능합니다.\n업데이트 해주세요."
                                )
                            } else { // 필수는 아님, 권장
                                question = QuestionDTO(
                                    QuestionDTO.STAT.WARNING,
                                    "새 버전이 있습니다.",
                                    "지금 업데이트 하시겠습니까?"
                                )
                            }

                            val dialog = QuestionDialog(this, question)
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.show()
                            dialog.setButtonOk("업데이트")
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
        finishAffinity() //해당 앱의 루트 액티비티를 종료시킨다. (API  16미만은 ActivityCompat.finishAffinity())
        System.runFinalization() //현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.
        System.exit(0) // 현재 액티비티를 종료시킨다.
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