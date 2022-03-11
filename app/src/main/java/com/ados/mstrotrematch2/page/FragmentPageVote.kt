package com.ados.mstrotrematch2.page


import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.dialog.*
import com.ados.mstrotrematch2.model.*
import com.bumptech.glide.Glide
import com.facebook.ads.*
import com.fsn.cauly.CaulyAdInfo
import com.fsn.cauly.CaulyAdInfoBuilder
import com.fsn.cauly.CaulyInterstitialAd
import com.fsn.cauly.CaulyInterstitialAdListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_fragment_page_vote.*
import kotlinx.android.synthetic.main.fragment_fragment_page_vote.layout_button_gift
import kotlinx.android.synthetic.main.lotto_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import kotlinx.android.synthetic.main.question_dialog_vote.*
import kotlinx.android.synthetic.main.question_dialog_vote.button_cancel
import kotlinx.android.synthetic.main.question_dialog_vote.button_ok
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class FragmentPageVote : Fragment(), OnVoteItemClickListener {

    var firestore : FirebaseFirestore? = null
    private var people : ArrayList<RankDTO> = arrayListOf()
    lateinit var recyclerView : RecyclerView
    lateinit var countDownTimer : CountDownTimer
    lateinit var countDownTimer2 : CountDownTimer

    private var isrefresh = true

    var ticketCount : Int = 0
    var lottoCount : Int = 0
    var quest_Count : Int = 0
    var intervalTime = 0L
    var rewardIntervalTime = 0L
    var isTimerStart = false
    var isTimerStart2 = false
    var preferencesDTO : PreferencesDTO? = null
    var runEvents : MutableSet<String> = mutableSetOf()

    var loadingDialog : LoadingDialog? = null

    var rewardOder1 = "admob"
    var rewardOder2 = "facebook"
    var rewardOder3 = "unity"
    var ad_interstitial = "admob"

    private lateinit var rewardedAdmob: RewardedAd
    var reward_Count = 0
    var isReward = false
    var runReward = false

    var lottoDialog : LottoDialog? = null
    var voteDialog : QuestionDialogVote? = null

    private var rewardedFacebook: RewardedVideoAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_fragment_page_vote, container, false)

        var rootView = inflater.inflate(R.layout.fragment_fragment_page_vote, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview_vote!!)as RecyclerView
        recyclerView?.layoutManager = GridLayoutManager(activity, 3)
        //recyclerView.layoutManager = LinearLayoutManager(requireContext())

        firestore = FirebaseFirestore.getInstance()
        /*firestore?.collection("people")?.orderBy("name", Query.Direction.ASCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            people.clear()
            if (querySnapshot == null) return@addSnapshotListener

            // document 수만큼 획득
            for (snapshot in querySnapshot) {
                var person = snapshot.toObject(RankDTO::class.java)!!
                people.add(person)
            }
            recyclerView.adapter = RecyclerViewAdapterVote(people, this)
        }*/
        refreshPeople()

        // 옵션 값 획득
        firestore?.collection("preferences")?.document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var oldRewardCount = 0
            if (preferencesDTO != null)
                oldRewardCount = preferencesDTO?.rewardCount!!

            preferencesDTO = documentSnapshot?.toObject(PreferencesDTO::class.java)
            intervalTime = 1000 * 60 * preferencesDTO?.IntervalTime!!.toLong()
            //rewardIntervalTime = 1000 * 60 * preferencesDTO?.rewardIntervalTime!!.toLong()
            rewardIntervalTime = 1000 * preferencesDTO?.rewardIntervalTimeSec!!.toLong()

            // 티켓 수 획득 (값이 없다면 최대치로 초기화)
            var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
            ticketCount = pref.getInt("TicketCount", preferencesDTO?.ticketChargeCount!!)
            lottoCount = pref.getInt("LottoCount", 0)
            text_lotto_count.text = "${lottoCount}개"

            quest_Count = getQuestCount()

            reward_Count = getRewardCount()
            if (reward_Count > preferencesDTO?.rewardCount!!) { // 최대 카운트 보다 클 경우 조절
                reward_Count = preferencesDTO?.rewardCount!!
                regRewardCount(reward_Count)
            } else if (oldRewardCount >= 0 && oldRewardCount < preferencesDTO?.rewardCount!!) { // 카운트 추가해줘야 할 경우
                reward_Count = reward_Count.plus(preferencesDTO?.rewardCount!! - oldRewardCount)
                regRewardCount(reward_Count)
            }
            text_reward_count.text = "남은횟수:$reward_Count"

            println(preferencesDTO)
            if (preferencesDTO?.runHotTime == true) {
                layout_hot_time.visibility = View.VISIBLE
                layout_ticket_count.setBackgroundColor(Color.parseColor("#4E1212"))
                recyclerview_vote.setBackgroundColor(Color.parseColor("#812631"))
                layout_reward.setBackgroundColor(Color.parseColor("#812631"))
            } else {
                layout_hot_time.visibility = View.GONE
                layout_ticket_count.setBackgroundColor(Color.parseColor("#262A35"))
                recyclerview_vote.setBackgroundColor(Color.parseColor("#5526CD"))
                layout_reward.setBackgroundColor(Color.parseColor("#5526CD"))
            }

            // 프로그램 종료된 동안 충전된 티켓 수 적용
            var marginCount = preferencesDTO?.ticketChargeCount!! - ticketCount // 현재 충전 가능한 티켓 수
            if (marginCount > 0) {
                var chargeTime = getChargeTime()
                var chargedTicketCount = ((System.currentTimeMillis() - chargeTime) / intervalTime).toInt()

                if (chargedTicketCount > 0) { // 프로그램 종료된 동안 충전된 티켓 수가 있을 경우 현재 티켓수에 누적 시켜 준다
                    var cargeCount = chargedTicketCount // 추가 될 티켓 수
                    if (chargedTicketCount > marginCount) { // 최대 충전 가능 수 보다 클 경우
                        cargeCount = marginCount
                    }

                    ticketCount += cargeCount // 실제 티켓 수에 누적

                    // 추가 충전을 했다면 충전 시간 계산해서 저장
                    chargeTime = chargeTime + (cargeCount * intervalTime)
                    var editor = pref.edit()
                    editor.putLong("ChargeTime", chargeTime).apply()

                    //Toast.makeText(getActivity(),"티켓 수 :${ticketcount}, 충전 티켓 수 : $chargedTicketCount, 실체 추가된 티켓 수 : $cargeCount",Toast.LENGTH_LONG).show()
                }
            }
            initTimer()
            resetTimer2()
        }

        // 리워드 순서 획득
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                rewardOder1 = task.result!!["ad_reward1"] as String
                rewardOder2 = task.result!!["ad_reward2"] as String
                rewardOder3 = task.result!!["ad_reward3"] as String
                ad_interstitial = task.result!!["ad_interstitial"] as String
            }
        }

        // 리워드 광고 설정
        rewardedAdmob = createAndLoadRewardedAd()
        AudienceNetworkAds.initialize(getActivity())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Glide.with(img_burning_time.context)
            .asBitmap()
            .load(R.drawable.burning_time) ///feed in path of the image
            .fitCenter()
            .into(img_burning_time)
        Glide.with(img_burning_time_title.context)
            .asBitmap()
            .load(R.drawable.burning_time_title) ///feed in path of the image
            .fitCenter()
            .into(img_burning_time_title)
        Glide.with(img_button_gift.context)
            .asBitmap()
            .load(R.drawable.gift_box) ///feed in path of the image
            .fitCenter()
            .into(img_button_gift)
        Glide.with(img_ticket.context)
            .asBitmap()
            .load(R.drawable.ticket2) ///feed in path of the image
            .fitCenter()
            .into(img_ticket)

        layout_button_reward.setOnClickListener {
            reward_Count = getRewardCount()
            text_reward_count.text = "남은횟수:$reward_Count"

            if (lottoCount >= preferencesDTO?.lottoMaxCount!!) {
                Toast.makeText(getActivity(), "가지고 있는 뽑기권을 사용 후 시청해 주세요.", Toast.LENGTH_SHORT).show()
            }
            else if (reward_Count <= 0) {
                Toast.makeText(getActivity(), "오늘은 더이상 광고를 시청할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else if (!runReward) {
                runReward = true
                CallRewardAd()
            }
        }

        swipe_refresh_layout.setOnRefreshListener {
            if (isrefresh == false) {
                Toast.makeText(getActivity(), "새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
                swipe_refresh_layout.setRefreshing(false)
            } else {
                isrefresh = false

                var second = 1
                timer(period = 1000)
                {
                    if (second > 5) {
                        cancel()
                        isrefresh = true
                    }
                    second++
                }

                refreshPeople()
                swipe_refresh_layout.setRefreshing(false)
            }
        }

        button_refresh.setOnClickListener {
            if (isrefresh == false) {
                Toast.makeText(getActivity(), "새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                isrefresh = false

                var second = 1;
                timer(period = 1000)
                {
                    if (second > 5) {
                        cancel()
                        isrefresh = true
                    }
                    second++
                }

                refreshPeople()
            }
        }

        layout_button_gift.setOnClickListener {
            if (ticketCount >= preferencesDTO?.ticketSaveMaxCount!!) {
                Toast.makeText(getActivity(), "가지고 있는 투표권을 사용 후 뽑기 할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
            else if (lottoCount <= 0) {
                Toast.makeText(getActivity(), "뽑기권이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                /*val dialog = LottoDialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                dialog.button_cancel.setOnClickListener { // No
                    var resultCount = dialog.text_result.text.toString().toInt()
                    dialog.dismiss()

                    lottoCount--
                    regLottoCount()
                    text_lotto_count.text = "${lottoCount}개"

                    ticketCount += resultCount
                    regTicketCount()
                    initTimer()
                    Toast.makeText(getActivity(), "투표권이 ${resultCount}장 추가되었습니다.", Toast.LENGTH_SHORT).show()
                }*/
                if (lottoDialog == null) {
                    lottoDialog = LottoDialog(requireContext())
                    lottoDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    lottoDialog?.setCanceledOnTouchOutside(false)
                }

                if (lottoDialog != null && lottoDialog?.isShowing == false) {
                    lottoDialog?.show()
                    lottoDialog?.button_cancel?.setOnClickListener { // No
                        var resultCount = lottoDialog?.text_result?.text.toString().toInt()
                        lottoDialog?.dismiss()
                        lottoDialog = null

                        lottoCount--
                        regLottoCount()
                        text_lotto_count.text = "${lottoCount}개"

                        ticketCount += resultCount
                        regTicketCount()
                        initTimer()
                        Toast.makeText(
                            getActivity(),
                            "투표권이 ${resultCount}장 추가되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        /*button_hottime_start.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 15,
                runHotTime = true,
                rewardCount = 40,
                rewardIntervalTime = 3
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
            }
        }

        button_hottime_stop.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 60,
                runHotTime = false,
                rewardCount = 20,
                rewardIntervalTime = 5
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
            }
        }*/

        eventTicket() // 시즌교체 작업
        checkMaintainance() // 시즌교체 작업
    }

    fun createAndLoadRewardedAd(): RewardedAd {
        val rewardedAd = RewardedAd(getActivity(), getString(R.string.admob_Reward_ad_id)) // 관리자모드
        val adLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
            }
            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        return rewardedAd
    }

    fun refreshPeople() {
        loading()
        firestore?.collection("people")?.orderBy("name", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            people.clear()
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                people.add(person)
            }
            recyclerView.adapter = RecyclerViewAdapterVote(people, this)
        }
        ?.addOnFailureListener { exception ->

        }
        loadingEnd()
    }

    override fun onItemClick(item: RankDTO, position: Int) {
        if (ticketCount > 0) { // 티켓이 존재 하면 투표 가능
            quest_Count = getQuestCount()
            /*val dialog = QuestionDialogVote(requireContext(), item, ticketCount, quest_Count, preferencesDTO?.questCount!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
            }

            dialog.button_ok.setOnClickListener { // Yes
                //refreshPeople()
                var voteCount = dialog.text_input_count.text.toString().toInt()
                dialog.dismiss()
                //ticketcount--
                ticketCount = ticketCount - voteCount

                regTicketCount()
                //Toast.makeText(getActivity(),"${item.name},${ticketcount}/${preferencesDTO?.ticketChargeCount}",Toast.LENGTH_SHORT).show()

                if (preferencesDTO?.ticketChargeCount!! > ticketCount) {
                    if (!isTimerStart) // 타이머가 중지중일 때만 기록 (타이머 돌고 있을 때 기록하면 충전시간이 초기화 됨
                        regChargeTime() // 티켓 사용 시간 기록
                }

                initTimer()

                // 전체 득표 수 증가
                //item.count = item.count?.plus(1)
                item.count = item.count?.plus(voteCount)

                // Firestore에 기록
                //firestore?.collection("people")?.document(item.docname.toString())?.set(item)

                var tsDoc = firestore?.collection("people")?.document(item.docname.toString())
                firestore?.runTransaction { transaction ->
                    val rankDTO = transaction.get(tsDoc!!).toObject(RankDTO::class.java)
                    //rankDTO?.count = rankDTO?.count?.plus(1)
                    rankDTO?.count = rankDTO?.count?.plus(voteCount)
                    transaction.set(tsDoc, rankDTO!!)
                    //refreshPeople()
                }?.addOnSuccessListener { result ->
                    refreshPeople()
                }?.addOnFailureListener { e ->

                }

                Toast.makeText(getActivity(), "$voteCount 표 투표 성공!", Toast.LENGTH_SHORT).show()
            }*/
             if (voteDialog == null) {
                voteDialog = QuestionDialogVote(requireContext(), item, ticketCount, quest_Count, preferencesDTO?.questCount!!)
                voteDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                voteDialog?.setCanceledOnTouchOutside(false)
            }

            if (voteDialog != null && voteDialog?.isShowing == false) {
                voteDialog?.show()
                voteDialog?.button_cancel?.setOnClickListener { // No
                    voteDialog?.dismiss()
                    voteDialog = null
                }

                voteDialog?.button_ok?.setOnClickListener { // Yes
                    //refreshPeople()
                    var voteCount = voteDialog?.text_input_count?.text.toString().toInt()
                    voteDialog?.dismiss()
                    voteDialog = null
                    //ticketcount--
                    ticketCount = ticketCount - voteCount

                    regTicketCount()
                    //Toast.makeText(getActivity(),"${item.name},${ticketcount}/${preferencesDTO?.ticketChargeCount}",Toast.LENGTH_SHORT).show()

                    if (preferencesDTO?.ticketChargeCount!! > ticketCount) {
                        if (!isTimerStart) // 타이머가 중지중일 때만 기록 (타이머 돌고 있을 때 기록하면 충전시간이 초기화 됨
                            regChargeTime() // 티켓 사용 시간 기록
                    }

                    initTimer()

                    // 전체 득표 수 증가
                    //item.count = item.count?.plus(1)
                    item.count = item.count?.plus(voteCount)

                    // Firestore에 기록
                    //firestore?.collection("people")?.document(item.docname.toString())?.set(item)

                    var tsDoc = firestore?.collection("people")?.document(item.docname.toString())
                    firestore?.runTransaction { transaction ->
                        val rankDTO = transaction.get(tsDoc!!).toObject(RankDTO::class.java)
                        //rankDTO?.count = rankDTO?.count?.plus(1)
                        rankDTO?.count = rankDTO?.count?.plus(voteCount)
                        transaction.set(tsDoc, rankDTO!!)
                        //refreshPeople()
                    }?.addOnSuccessListener { result ->
                        refreshPeople()
                    }?.addOnFailureListener { e ->

                    }

                    Toast.makeText(getActivity(), "$voteCount 표 투표 성공!", Toast.LENGTH_SHORT).show()
                }
            }
        } else { // 티켓이 없으면 투표 불가능
            Toast.makeText(getActivity(), "투표할 수 있는 투표권이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun initTimer() {
        if (preferencesDTO?.ticketChargeCount!! > ticketCount) { // 이미 충전된 투표권을 뺏지 않기 위해서 조건 추가
            resetTimer()
        } else {
            if (isTimerStart)
                countDownTimer.cancel()
            isTimerStart = false
            text_ticket_count.text = "${ticketCount}개"
            text_ticket_timer.text = ""
        }
    }

    fun regEventTicket(uid: String) {
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var editor = pref.edit()
        editor.putString(uid, "ok").apply()
    }

    fun regTicketCount() {
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var editor = pref.edit()
        editor.putInt("TicketCount", ticketCount).apply()
    }

    fun regLottoCount() {
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var editor = pref.edit()
        editor.putInt("LottoCount", lottoCount).apply()
    }

    fun regChargeTime() {
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var editor = pref.edit()
        editor.putLong("ChargeTime", System.currentTimeMillis()).apply()
    }

    fun getChargeTime() : Long {
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var chargeTime = pref.getLong("ChargeTime", 0)

        return chargeTime
    }

    fun regRewardTime() {
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var editor = pref.edit()
        editor.putLong("RewardTime", System.currentTimeMillis()).apply()
    }

    fun getRewardTime() : Long {
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var rewardTime = pref.getLong("RewardTime", 0)

        return rewardTime
    }

    fun regRewardCount(count: Int) {
        var key = "${SimpleDateFormat("yyyyMMdd").format(Date())}RewardCount"
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var editor = pref.edit()
        editor.putInt(key, count).apply()
    }

    fun getRewardCount() : Int {
        var key = "${SimpleDateFormat("yyyyMMdd").format(Date())}RewardCount"
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var rewardCount = pref.getInt(key, preferencesDTO?.rewardCount!!)

        return rewardCount
    }

    fun regQuestCount(count: Int) {
        var key = "${SimpleDateFormat("yyyyMMdd").format(Date())}QuestCount"
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var editor = pref.edit()
        editor.putInt(key, count).apply()
    }

    fun getQuestCount() : Int {
        var key = "${SimpleDateFormat("yyyyMMdd").format(Date())}QuestCount"
        var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
        var questCount = pref.getInt(key, 0)

        return questCount
    }

    fun resetTimer() {
        // 티켓 충전시간 파일에서 읽기
        var chargeTime = getChargeTime()
        var interval = (chargeTime + intervalTime) - System.currentTimeMillis()

        //Toast.makeText(getActivity(),"${interval}, ${ticketcount}/${preferencesDTO?.ticketChargeCount}",Toast.LENGTH_LONG).show()

        // 타이머가 동작중이면 종료 후 다시 실행
        if (isTimerStart)
            countDownTimer.cancel()

        isTimerStart = true
        countDownTimer = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                ticketCount++

                // 충전 시간, 티켓 카운트 기록
                regChargeTime()
                regTicketCount()

                isTimerStart = false

                if (preferencesDTO?.ticketChargeCount!! > ticketCount) { // 티켓 충전이 가능하다면 타이머 다시 호출
                    text_ticket_count.text = "${ticketCount}개"
                    resetTimer()
                } else { // 티켓 충전 Full
                    text_ticket_count.text = "${ticketCount}개"
                    text_ticket_timer.text = ""
                }

                /*text_ticket_count.text = "${ticketcount}개"
                text_ticket_timer.text = ""
                isTimerStart = false*/
            }

            override fun onTick(millisUntilFinished: Long) {
                if (text_ticket_count != null && text_ticket_timer != null) {
                    var totalsec = millisUntilFinished / 1000
                    var hour = totalsec / 3600
                    var min = (totalsec % 3600) / 60
                    var sec = totalsec % 60

                    text_ticket_count.text = "${ticketCount}개"
                    text_ticket_timer.text = "(${String.format("%02d", hour)}:${String.format(
                        "%02d",
                        min
                    )}:${String.format("%02d", sec)} 후 충전)"
                }
            }

        }.start()
    }

    fun resetTimer2() {
        isReward = false

        var rewardTime = getRewardTime()
        var interval = 0L

        // 인터벌 시간하고, 최대개수 디비로 설정하여 핫타임때 적용하도록

        if (rewardTime == 0L)
        {
            //interval = rewardIntervalTime
            //regRewardTime()
        }
        else
            interval = (rewardTime + rewardIntervalTime) - System.currentTimeMillis()

        // 타이머가 동작중이면 종료 후 다시 실행
        if (isTimerStart2)
            countDownTimer2.cancel()

        isTimerStart2 = true
        countDownTimer2 = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                isTimerStart2 = false
                isReward = true
                text_reward_timer.text = "티켓 충전"

                if (preferencesDTO != null) {
                    if (preferencesDTO?.rewardName2 != null) {
                        text_reward_timer.text = preferencesDTO?.rewardName2
                    }
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                if (text_ticket_count != null && text_ticket_timer != null) {
                    var totalsec = millisUntilFinished / 1000
                    var min = (totalsec % 3600) / 60
                    var sec = totalsec % 60

                    text_reward_timer.text = "${String.format("%02d", min)}:${String.format(
                        "%02d",
                        sec
                    )}"
                }
            }

        }.start()
    }

    fun eventTicket() {
        firestore?.collection("event")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (querySnapshot == null) return@addSnapshotListener
            var now = Date()
            // document 수만큼 획득
            for (snapshot in querySnapshot) {
                var event = snapshot.toObject(EventDTO::class.java)!!

                // 수령 시간이 지났는지 확인
                if (now < event.limit) {
                    var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    var uid = pref.getString(event.uid, "")

                    if (uid.isNullOrEmpty()) { // 값이 없어야 아직 수령안한 티켓이다.
                        showEventDialog(event)
                    }
                }
            }
        }
    }

    fun checkMaintainance() {
        // 시즌교체 작업
        firestore?.collection("preferences")?.document("update")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var updateDTO = documentSnapshot?.toObject(UpdateDTO::class.java)
            var question : QuestionDTO

            // 서버 점검 중
            if (updateDTO?.maintainance!!) {
                question = QuestionDTO(
                    QuestionDTO.STAT.ERROR,
                    updateDTO?.maintainanceTitle,
                    updateDTO?.maintainanceDesc
                )

                val dialog = QuestionDialog(requireContext(), question)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                dialog.showButtonOk(false)
                dialog.setButtonCancel("확인")
                dialog.button_question_cancel.setOnClickListener { // No
                    dialog.dismiss()
                    (activity as MainActivity?)!!.appExit()
                }
            }
        }
    }

    fun showEventDialog(event: EventDTO) {
        println("시간 : ${event.limit}, uid : ${event.uid}")
        // 이미 동일한 uid의 대화상자가 실행중인지 확인
        if (null == runEvents.find { it.startsWith(event.uid.toString()) } ) {
            runEvents.add(event.uid.toString())

            println("시간 : ${event.limit}, uid : ${event.uid}")
            val dialog = EventDialog(requireContext(), event)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
                runEvents.remove(event.uid.toString())
            }

            dialog.button_ok.setOnClickListener { // Yes
                dialog.dismiss()
                ticketCount += event.count!!

                regTicketCount()
                regEventTicket(event.uid!!)

                initTimer()

                runEvents.remove(event.uid.toString())
                Toast.makeText(getActivity(), "투표권이 ${event.count}장 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loading() {
        android.os.Handler().postDelayed({
            if (loadingDialog == null) {
                loadingDialog = LoadingDialog(requireContext())
                loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                loadingDialog?.setCanceledOnTouchOutside(false)
            }
            loadingDialog?.show()
        }, 0)
    }

    fun loadingEnd() {
        android.os.Handler().postDelayed({
            loadingDialog?.dismiss()
        }, 400)
    }

    /*fun addRewardTicket() {
        ticketCount = ticketCount.plus(preferencesDTO?.rewardBonus!!)
        regTicketCount()
        initTimer()

        regRewardTime()
        resetTimer2()

        reward_Count--
        regRewardCount(reward_Count)
        text_reward_count.text = "남은횟수:$reward_Count"

        Toast.makeText(
            getActivity(),
            "투표권이 ${preferencesDTO?.rewardBonus!!}장 추가되었습니다.",
            Toast.LENGTH_SHORT
        ).show()
        runReward = false
    }*/

    fun addRewardTicket() {
        lottoCount = lottoCount.plus(preferencesDTO?.rewardBonus2!!)
        regLottoCount()
        text_lotto_count.text = "${lottoCount}개"

        quest_Count = getQuestCount()
        quest_Count++
        regQuestCount(quest_Count)

        regRewardTime()
        resetTimer2()

        reward_Count--
        regRewardCount(reward_Count)
        text_reward_count.text = "남은횟수:$reward_Count"

        Toast.makeText(getActivity(), "뽑기권이 ${preferencesDTO?.rewardBonus2!!}장 추가되었습니다.", Toast.LENGTH_SHORT).show()
        runReward = false
    }

    fun rewardAdmob() : Boolean {
        if (rewardedAdmob.isLoaded) {
            val adCallback = object : RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    // Ad opened.
                    //runReward = false
                }

                override fun onRewardedAdClosed() {
                    // Ad closed.
                    rewardedAdmob = createAndLoadRewardedAd()
                    runReward = false
                }

                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    // User earned reward.
                    addRewardTicket()
                }

                override fun onRewardedAdFailedToShow(adError: AdError) {
                    // Ad failed to display.
                    Toast.makeText(getActivity(), "광고를 표시할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    runReward = false
                }
            }
            rewardedAdmob.show(getActivity(), adCallback)
            return true
        } else {
            rewardedAdmob = createAndLoadRewardedAd()
            return false
        }
    }

    fun rewardFacebook() : Boolean {
        rewardedFacebook = RewardedVideoAd(getActivity(), getString(R.string.facebook_Reward_ad_id))
        val rewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onError(p0: Ad?, p1: com.facebook.ads.AdError?) {
                // Rewarded video ad failed to load
                // 보상 형 동영상 광고를로드하지 못했습니다.
                //Toast.makeText(getActivity(), "광고를 표시할 수 없습니다.", Toast.LENGTH_SHORT).show()
                runReward = false
            }

            override fun onAdLoaded(ad: Ad?) {
                // Rewarded video ad is loaded and ready to be displayed
                // 보상 형 동영상 광고가로드되고 표시 할 준비가되었습니다.
                //Log.d(TAG, "Rewarded video ad is loaded and ready to be displayed!")
                rewardedFacebook!!.show()
            }

            override fun onAdClicked(ad: Ad?) {
                // Rewarded video ad clicked
                // 보상 형 동영상 광고 클릭
                //Log.d(TAG, "Rewarded video ad clicked!")
                runReward = false
            }

            override fun onLoggingImpression(ad: Ad?) {
                // Rewarded Video ad impression - the event will fire when the
                // video starts playing
                // 동영상 재생 시작
                //Log.d(TAG, "Rewarded video ad impression logged!")
                runReward = false
            }

            override fun onRewardedVideoCompleted() {
                // Rewarded Video View Complete - the video has been played to the end.
                // You can use this event to initialize your reward
                // 보상 형 동영상보기 완료-동영상이 끝까지 재생되었습니다.

                addRewardTicket()
            }

            override fun onRewardedVideoClosed() {
                // The Rewarded Video ad was closed - this can occur during the video
                // by closing the app, or closing the end card.
                // 보상 형 동영상 광고가 닫혔습니다. 동영상 도중에 발생할 수 있습니다.
                // 앱을 닫거나 엔드 카드를 닫습니다.
                //Log.d(TAG, "Rewarded video ad closed!")
                runReward = false
            }
        }
        rewardedFacebook!!.loadAd(
            rewardedFacebook!!.buildLoadAdConfig()
                .withAdListener(rewardedVideoAdListener)
                .build()
        )

        var isSuccess = true
        Handler().postDelayed({
            // Check if rewardedVideoAd has been loaded successfully
            // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
            if (rewardedFacebook == null || !rewardedFacebook!!.isAdLoaded || rewardedFacebook!!.isAdInvalidated) {
                rewardedFacebook!!.loadAd(
                    rewardedFacebook!!.buildLoadAdConfig()
                        .withAdListener(rewardedVideoAdListener)
                        .build()
                )
                isSuccess = false
            }
        }, 5000)

        return isSuccess
    }

    fun rewardUnity() : Boolean {
        return false
    }

    fun callReward(rewardOrder : String) : Boolean {
        when (rewardOrder) {
            getString(R.string.adtype_admob) -> {
                return rewardAdmob()
            }
            getString(R.string.adtype_facebook) -> {
                return rewardFacebook()
            }
            getString(R.string.adtype_unity) -> {
                return rewardUnity()
            }
        }
        return false
    }

    fun interstitialAdmob(isFirst : Boolean) {
        // 애드몹 - 전면
        var InterstitialAd = InterstitialAd(context)
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
                        Toast.makeText(getActivity(), "현재 볼 수 있는 광고가 없습니다.", Toast.LENGTH_SHORT).show()
                        runReward = false
                    }
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                if (isFirst) {
                    interstitialCauly(false)
                } else {
                    Toast.makeText(getActivity(), "현재 볼 수 있는 광고가 없습니다.", Toast.LENGTH_SHORT).show()
                    runReward = false
                }
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
                //runReward = false
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                //runReward = false
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                runReward = false
            }

            override fun onAdClosed() {
                addRewardTicket()
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
                    Toast.makeText(getActivity(), "현재 볼 수 있는 광고가 없습니다.", Toast.LENGTH_SHORT).show()
                    runReward = false
                }
            }

            override fun onClosedInterstitialAd(p0: CaulyInterstitialAd?) {
                addRewardTicket()
            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {
                runReward = false
            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(getActivity())
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

    fun CallRewardAd() {
        if (isReward) {
            if (!callReward(rewardOder1)) {
                if (!callReward(rewardOder2)) {
                    if (!callReward(rewardOder3)) {
                        callInterstitial(ad_interstitial)
                        /*if (!callInterstitial(ad_interstitial)) {
                            if (!callInterstitial(ad_interstitial)) {
                                Toast.makeText(getActivity(), "현재 볼 수 있는 광고가 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }*/
                    }
                }
            }
        } else {
            if (!isReward) {
                Toast.makeText(getActivity(), "아직 광고를 시청할 수 없습니다.", Toast.LENGTH_SHORT).show()
                runReward = false
            }
        }
    }

}
