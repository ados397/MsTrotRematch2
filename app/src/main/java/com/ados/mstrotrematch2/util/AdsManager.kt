package com.ados.mstrotrematch2.util

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.ados.mstrotrematch2.model.AdPolicyDTO
import com.fsn.cauly.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.kakao.adfit.ads.AdListener
import com.kakao.adfit.ads.ba.BannerAdView

open class AdsManager() {
    companion object {
        // 광고 종류
        const val AD_TYPE_ADMOB = "admob"
        const val AD_TYPE_CAULY = "cauly"
        const val AD_TYPE_ADFIT = "adfit"
        const val AD_TYPE_FACEBOOK = "facebook"
        const val AD_TYPE_UNITY = "unity"

        // 광고 ID
        const val ADMOB_ID_BANNER = "ca-app-pub-1859147676618347/2788922085"
        const val ADMOB_ID_INTERSTITIAL = "ca-app-pub-1859147676618347/5678631694"
        const val ADMOB_ID_INTERSTITIAL2 = "ca-app-pub-1859147676618347/9343594204"
        const val ADMOB_ID_INTERSTITIAL3 = "ca-app-pub-1859147676618347/1454460893"
        const val ADMOB_ID_NATIVE = "ca-app-pub-1859147676618347/2613348252"
        const val ADMOB_ID_REWARD = "ca-app-pub-1859147676618347/9945357373"
        const val ADMOB_ID_REWARD2 = "ca-app-pub-1859147676618347/1752330054"
        const val ADMOB_ID_REWARD3 = "ca-app-pub-1859147676618347/5992471707"
        const val ADMOB_ID_REWARD_INTERSTITIAL = "ca-app-pub-1859147676618347/5426345553"
        const val ADMOB_ID_REWARD_USER_GEM = "ca-app-pub-1859147676618347/1390738250"
        const val ADMOB_ID_REWARD_USER_EXP = "ca-app-pub-1859147676618347/7026208312"
        const val ADMOB_ID_REWARD_FAN_CLUB_GEM = "ca-app-pub-1859147676618347/6963656802"
        const val ADMOB_ID_REWARD_FAN_CLUB_EXP = "ca-app-pub-1859147676618347/9398248450"
        const val ADMOB_ID_REWARD_GAMBLE_COUNT = "ca-app-pub-1859147676618347/3956306705"
        const val CAULY_ID_INTERSTITIAL = "avhXxFUQ"
        const val CAULY_ID_BANNER = "avhXxFUQ"
        const val ADFIT_ID_BANNER = "DAN-r8oaHTGIRZIZzKuH"

        // 광고 테스트 ID
        const val ADMOB_TEST_ID_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val ADMOB_TEST_ID_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val ADMOB_TEST_ID_NATIVE = "ca-app-pub-3940256099942544/2247696110"
        const val ADMOB_TEST_ID_REWARD = "ca-app-pub-3940256099942544/5224354917"
        const val ADMOB_TEST_ID_REWARD_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
        const val CAULY_TEST_ID_INTERSTITIAL = "CAULY"
    }

    var admobID = ""
    var admobID2 = ""
    var admobID3 = ""
    var caulyID = ""
    var adfitID = ""
}

class AdsInterstitialManager(val activity: Activity, private val adPolicyDTO: AdPolicyDTO) : AdsManager() {
    private var mInterstitialAd: InterstitialAd? = null

    init {
        admobID = ADMOB_ID_INTERSTITIAL
        admobID2 = ADMOB_ID_INTERSTITIAL2
        admobID3 = ADMOB_ID_INTERSTITIAL3
        //admobID = ADMOB_TEST_ID_INTERSTITIAL // @테스트
        //admobID2 = ADMOB_TEST_ID_INTERSTITIAL // @테스트
        //admobID3 = ADMOB_TEST_ID_INTERSTITIAL // @테스트

        caulyID = CAULY_ID_INTERSTITIAL
        //caulyID = CAULY_TEST_ID_INTERSTITIAL // @테스트
    }

    fun callInterstitial(myCallback: (Boolean) -> Unit) {
        //println("전면 광고 종류 : ${adPolicyDTO.ad_interstitial}")
        when (adPolicyDTO.ad_interstitial) {
            AD_TYPE_ADMOB -> {
                interstitialAdmob(admobID) { success ->
                    if (!success) { // 광고 호출 실패 시 다른 광고 ID 시도
                        interstitialAdmob(admobID2) { success2 ->
                            if (!success2) { // 광고 호출 실패 시 다른 광고 ID 시도
                                interstitialAdmob(admobID3) { success3 ->
                                    if (!success3) { // 광고 호출 실패 시 다른 광고 ID 시도
                                        interstitialCauly { // 마지막으로 카울리 광고 시도 후 종료
                                            myCallback(it)
                                        }
                                    } else {
                                        myCallback(success3)
                                    }
                                }
                            } else {
                                myCallback(success2)
                            }
                        }
                    } else {
                        myCallback(success)
                    }
                }
            }
            AD_TYPE_CAULY -> {
                interstitialCauly { success ->
                    if (!success) { // 광고 호출 실패 시 다른 광고 한번 더 시도
                        interstitialAdmob(admobID) {
                            myCallback(it)
                        }
                    } else {
                        myCallback(success)
                    }
                }
            }
            else -> {

            }
        }
    }

    private fun interstitialAdmob(admobID: String, myCallback: (Boolean) -> Unit) {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(activity, admobID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                //Log.d(TAG, adError?.message)
                //println("[Admob] 광고 로드 실패 ${adError.message}")
                mInterstitialAd = null
                myCallback(false)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                //Log.d(TAG, 'Ad was loaded.')
                //println("[Admob] 광고 로드 성공")
                mInterstitialAd = interstitialAd

                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        //Log.d(TAG, 'Ad was dismissed.')
                        //println("[Admob] 광고 Ad was dismissed.")
                        myCallback(true)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        //Log.d(TAG, 'Ad failed to show.')
                        //println("[Admob] 광고 Ad failed to show.")
                        myCallback(false)
                    }

                    override fun onAdShowedFullScreenContent() {
                        //Log.d(TAG, 'Ad showed fullscreen content.')
                        //println("[Admob] 광고 Ad showed fullscreen content.")
                        mInterstitialAd = null
                    }
                }

                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(activity)
                    //println("[Admob] 광고 Show")
                } else {
                    //Log.d("TAG", "The interstitial ad wasn't ready yet.")
                    //println("[Admob] 광고 null")
                    myCallback(false)
                }
            }
        })
    }

    private fun interstitialCauly(myCallback: (Boolean) -> Unit) {
        var adInfo: CaulyAdInfo = CaulyAdInfoBuilder(caulyID).build()
        var interstial = CaulyInterstitialAd()
        interstial.setAdInfo(adInfo)

        val adCallback = object : CaulyInterstitialAdListener {
            override fun onReceiveInterstitialAd(ad: CaulyInterstitialAd?, isChargeableAd: Boolean) {
                // 광고 수신 성공한 경우 호출됨.
                // 수신된 광고가 무료 광고인 경우 isChargeableAd 값이 false 임.
                if (!isChargeableAd) {
                    //println("[Caluy] free interstitial AD received.")
                } else {
                    //println("[Caluy] normal interstitial AD received.")
                }
                // 광고 노출
                ad?.show()
                //println("[Caluy] 광고 Show")
            }

            override fun onFailedToReceiveInterstitialAd(ad: CaulyInterstitialAd?, errorCode: Int, errorMsg: String?) {
                // 전면 광고 수신 실패할 경우 호출됨.
                //println("[Caluy] 광고 로드 실패 ($errorCode, $errorMsg)")
                myCallback(false)
            }

            override fun onClosedInterstitialAd(ad: CaulyInterstitialAd?) {
                // 전면 광고가 닫힌 경우 호출됨.
                //println("[Caluy] 광고 Ad was dismissed.")
                myCallback(true)
            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {

            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(activity)
    }
}

class AdsRewardManager(val activity: Activity, private val adPolicyDTO: AdPolicyDTO, rewardType: RewardType) : AdsManager() {
    enum class RewardType {
        REWARD_GEM, REWARD_USER_GEM, REWARD_USER_EXP, REWARD_FAN_CLUB_GEM, REWARD_FAN_CLUB_EXP, REWARD_GAMBLE_COUNT
    }

    private var adsInterstitialManager = AdsInterstitialManager(activity, adPolicyDTO)
    private var rewardedInterstitialAd = AdsRewardInterstitialManager(activity, adPolicyDTO)

    private var mRewardedAdmob: RewardedAd? = null
    private var isRunReward = false

    init {
        admobID = when (rewardType) {
            RewardType.REWARD_GEM -> ADMOB_ID_REWARD
            RewardType.REWARD_USER_GEM -> ADMOB_ID_REWARD_USER_GEM
            RewardType.REWARD_USER_EXP -> ADMOB_ID_REWARD_USER_EXP
            RewardType.REWARD_FAN_CLUB_GEM -> ADMOB_ID_REWARD_FAN_CLUB_GEM
            RewardType.REWARD_FAN_CLUB_EXP -> ADMOB_ID_REWARD_FAN_CLUB_EXP
            RewardType.REWARD_GAMBLE_COUNT -> ADMOB_ID_REWARD_GAMBLE_COUNT
        }
        admobID = ADMOB_ID_REWARD
        //admobID = ADMOB_TEST_ID_REWARD // @테스트

        loadRewardedAdmob()
    }

    fun callReward(myCallback: (Boolean) -> Unit) {
        //println("광고 - isRunReward = $isRunReward")
        if (!isRunReward) {
            isRunReward = true
            admobID = ADMOB_ID_REWARD
            //admobID = ADMOB_TEST_ID_REWARD // @테스트
            loadRewardedAdmob()
            showReward(adPolicyDTO.ad_reward1.toString()) { reward1 ->
                //println("광고 결과1 $reward1")
                if (!reward1) {
                    rewardedInterstitialAd.showAd { rewardInterstitial ->
                        //println("광고 보상형 전면 결과 $rewardInterstitial")
                        if (!rewardInterstitial) {
                            admobID = ADMOB_ID_REWARD2
                            //admobID = ADMOB_TEST_ID_REWARD // @테스트
                            loadRewardedAdmob()
                            showReward(adPolicyDTO.ad_reward1.toString()) { reward2 ->
                                //println("광고 결과2 $reward2")
                                if (!reward2) {
                                    admobID = ADMOB_ID_REWARD3
                                    //admobID = ADMOB_TEST_ID_REWARD // @테스트
                                    loadRewardedAdmob()
                                    showReward(adPolicyDTO.ad_reward1.toString()) { reward3 ->
                                        //println("광고 결과3 $reward3")
                                        if (!reward3) {
                                            adsInterstitialManager.callInterstitial {
                                                //println("광고 결과4 $it")
                                                isRunReward = false
                                                myCallback(it)
                                            }
                                        } else {
                                            isRunReward = false
                                            myCallback(true)
                                        }
                                    }
                                } else {
                                    isRunReward = false
                                    myCallback(true)
                                }
                            }
                        } else {
                            isRunReward = false
                            myCallback(true)
                        }
                    }
                } else {
                    isRunReward = false
                    myCallback(true)
                }
            }
        } else {
            //Toast.makeText(activity, "아직 광고를 시청할 수 없습니다.", Toast.LENGTH_SHORT).show()
            myCallback(false)
        }
    }

    private fun showReward(adType: String, myCallback: (Boolean) -> Unit) {
        //println("광고 - adType = $adType")
        when (adType) {
            AD_TYPE_ADMOB -> {
                showRewardAdmob {
                    //println("광고 결과 $it")
                    myCallback(it)
                }
            }
            AD_TYPE_FACEBOOK -> {
                showRewardAdmob {
                    myCallback(it)
                }
            }
            else -> {

            }
        }
    }

    private fun loadRewardedAdmob() {
        var adRequest = AdRequest.Builder().build()

        RewardedAd.load(activity, admobID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                //Log.d(TAG, adError?.message)
                mRewardedAdmob = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                //Log.d(TAG, "Ad was loaded.")
                mRewardedAdmob = rewardedAd
            }
        })
    }

    private fun showRewardAdmob(myCallback: (Boolean) -> Unit) {
        //println("광고 - showRewardAdmob")
        if (mRewardedAdmob != null) {
            //println("광고 - mRewardedAdmob != null")
            mRewardedAdmob?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    //Log.d(TAG, "Ad was shown.")
                    loadRewardedAdmob()
                    //println("광고 - onAdShowedFullScreenContent")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    // Called when ad fails to show.
                    //Log.d(TAG, "Ad failed to show.")
                    loadRewardedAdmob()
                    //println("광고 - onAdFailedToShowFullScreenContent")
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    //Log.d(TAG, "Ad was dismissed.")
                    isRunReward = false
                    mRewardedAdmob = null
                    loadRewardedAdmob()
                    //println("광고 - onAdDismissedFullScreenContent")
                }
            }
            mRewardedAdmob?.show(activity) { _ ->
                //var rewardAmount = rewardItem.amount
                //var rewardType = rewardItem.type
                //println("광고 - mRewardedAdmob?.show(activity)")
                myCallback(true)
            }
        } else {
            //Toast.makeText(activity, "아직 광고를 시청할 수 없습니다.", Toast.LENGTH_SHORT).show()
            //Log.d(TAG, "The rewarded ad wasn't ready yet.")
            //Toast.makeText(activity, "The rewarded ad wasn't ready yet.", Toast.LENGTH_SHORT).show()
            //println("광고 - mRewardedAdmob == null")
            myCallback(false)
        }
    }
}

class AdsBannerManager(val activity: Activity, private val lifecycle: Lifecycle, private val adPolicyDTO: AdPolicyDTO, adViewAdmob: AdView, adViewCauly: CaulyAdView, adViewAdfit: BannerAdView) : AdsManager() {
    private var mAdViewAdmob : AdView? = null
    private var mAdViewCauly : CaulyAdView? = null
    private var mAdViewAdfit : BannerAdView? = null

    init {
        mAdViewAdmob = adViewAdmob
        mAdViewCauly = adViewCauly
        mAdViewAdfit = adViewAdfit

        // 애드몹과 카울리는 xml에서만 ID 지정 가능
        //admobID = ADMOB_ID_BANNER
        //admobID = ADMOB_TEST_ID_BANNER // @테스트
        //caulyID = CAULY_ID_BANNER
        //caulyID = CAULY_TEST_ID_INTERSTITIAL // @테스트
        adfitID = ADFIT_ID_BANNER
    }

    fun callBanner(myCallback: (Boolean) -> Unit) {
        mAdViewAdmob?.visibility = View.GONE
        mAdViewCauly?.visibility = View.GONE
        mAdViewAdfit?.visibility = View.GONE

        when(adPolicyDTO.ad_banner) {
            AD_TYPE_ADMOB -> {
                MobileAds.initialize(activity)
                var adRequest = AdRequest.Builder().build()
                mAdViewAdmob?.loadAd(adRequest)
                mAdViewAdmob?.visibility = View.VISIBLE
            }
            AD_TYPE_CAULY -> {
                mAdViewCauly?.visibility = View.VISIBLE
            }
            AD_TYPE_ADFIT -> {
                mAdViewAdfit?.visibility = View.VISIBLE
                mAdViewAdfit?.setClientId(adfitID)  // 할당 받은 광고 단위(clientId) 설정
                mAdViewAdfit?.setAdListener(object : AdListener {  // 광고 수신 리스너 설정
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
                        mAdViewAdfit?.resume()
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                    fun onPause() {
                        mAdViewAdfit?.pause()
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    fun onDestroy() {
                        mAdViewAdfit?.destroy()
                    }

                })

                mAdViewAdfit?.loadAd()  // 광고 요청
            }
        }
        myCallback(true)
    }
}

class AdsRewardInterstitialManager(val activity: Activity, private val adPolicyDTO: AdPolicyDTO) : AdsManager() {
    private var rewardedInterstitialAd : RewardedInterstitialAd? = null

    init {
        admobID = ADMOB_ID_REWARD_INTERSTITIAL
        //admobID = ADMOB_TEST_ID_REWARD_INTERSTITIAL // @테스트

        MobileAds.initialize(activity) { initializationStatus ->
            loadAd()
        }
    }

    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(activity, admobID, adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                //println("광고 로드 성공")
                rewardedInterstitialAd = ad
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Error.
                //println("광고 로드 실패")
                rewardedInterstitialAd = null
            }
        })
    }

    fun showAd(myCallback: (Boolean) -> Unit) {
        if (rewardedInterstitialAd != null) {
            rewardedInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    //println("광고 - Ad was clicked.")
                    loadAd()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    //println("광고 - Ad dismissed fullscreen content.")
                    rewardedInterstitialAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when ad fails to show.
                    //println("광고 - Ad failed to show fullscreen content.")
                    rewardedInterstitialAd = null
                    loadAd()
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    //println("광고 - Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    //println("광고 - Ad showed fullscreen content.")
                }
            }
            rewardedInterstitialAd?.show(activity) { it->
                myCallback(true)
            }
        } else {
            //println("광고 - mRewardedAdmob == null")
            myCallback(false)
        }
    }
}