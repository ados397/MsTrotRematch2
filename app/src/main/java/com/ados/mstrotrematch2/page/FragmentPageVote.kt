package com.ados.mstrotrematch2.page


import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.adapter.OnVoteItemClickListener
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterVote
import com.ados.mstrotrematch2.databinding.FragmentPageVoteBinding
import com.ados.mstrotrematch2.dialog.*
import com.ados.mstrotrematch2.firebase.FirebaseRepository
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.*
import com.ados.mstrotrematch2.util.AdsRewardManager
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.bumptech.glide.Glide
import okhttp3.internal.userAgent
import java.util.*
import kotlin.concurrent.timer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPageVote.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPageVote : Fragment(), OnVoteItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentPageVoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    var recyclerViewAdapter: RecyclerViewAdapterVote? = null
    private var isrefresh = true
    private var questCount : Int = 0
    var voteDialog : QuestionDialogVote? = null

    private var adminString = ""

    // 뷰모델 연결
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var toast : Toast? = null

    // AD
    private var adsRewardManager: AdsRewardManager? = null
    private lateinit var rewardTimer : CountDownTimer
    var isRunRewardTimer = false

    private var oldRewardCount = 0

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPageVoteBinding.inflate(inflater, container, false)
        //binding = FragmentPageVoteBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        binding.recyclerviewVote.layoutManager = GridLayoutManager(activity, 3)
        binding.recyclerviewVote.itemAnimator = null

        val adPolicyDTO = (activity as MainActivity?)?.getAdPolicy()!!
        adsRewardManager = AdsRewardManager(requireActivity(), adPolicyDTO, AdsRewardManager.RewardType.REWARD_GEM)

        // 핫타임 및 옵션 즉시 적용을 위해 자체적으로 모니터링 함
        firebaseViewModel.getPreferencesListen()
        observePreferences()

        // 프리미엄 패키지 즉시 적용을 위해 자체적으로 모니터링 함
        val userDTO = (activity as MainActivity?)?.getUser()!!
        firebaseViewModel.getUserListen(userDTO.uid.toString())
        observeUser()

        refreshPeople()

        return rootView
    }

    override fun onDestroyView() {
        if (isRunRewardTimer)
            rewardTimer.cancel()
        _binding = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity?)?.backPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.layoutHotTimeInfo.visibility = View.GONE

        Glide.with(binding.imgBurningTime.context)
            .asBitmap()
            .load(R.drawable.burning_time) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgBurningTime)
        Glide.with(binding.imgBurningTimeTitle.context)
            .asBitmap()
            .load(R.drawable.burning_time_title) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgBurningTimeTitle)
        Glide.with(binding.imgButtonGift.context)
            .asBitmap()
            .load(R.drawable.lottery) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgButtonGift)
        Glide.with(binding.imgTicket.context)
            .asBitmap()
            .load(R.drawable.diamond) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgTicket)

        binding.layoutButtonReward.setOnClickListener {
            val rewardGemCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_REWARD_COUNT, firebaseViewModel.preferencesDTO.value!!.rewardCount!!)
            when {
                rewardGemCount <= 0 -> {
                    callToast("오늘은 더 이상 광고를 시청할 수 없습니다.")
                }
                isRunRewardTimer!! -> { // 타이머가 동작중이면 광고 시청 불가능
                    callToast("아직 광고를 시청할 수 없습니다.")
                }
                else -> {
                    if (adsRewardManager != null) {
                        adsRewardManager?.callReward {
                            if (it) {
                                rewardGem()
                            } else {
                                callToast("아직 광고를 시청할 수 없습니다.")
                            }

                        }
                    }
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (!isrefresh) {
                Toast.makeText(requireActivity(), "새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
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
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        binding.buttonRefresh.setOnClickListener {
            if (!isrefresh) {
                Toast.makeText(requireActivity(), "새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
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

        binding.layoutButtonGift.setOnClickListener {
            if (isRunRewardTimer)
                rewardTimer.cancel()
            val fragment = FragmentGamble()
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }

            /*if (ticketCount >= preferencesDTO?.ticketSaveMaxCount!!) {
                Toast.makeText(requireActivity(), "가지고 있는 투표권을 사용 후 뽑기 할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
            else if (lottoCount <= 0) {
                Toast.makeText(requireActivity(), "뽑기권이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                if (lottoDialog == null) {
                    lottoDialog = LottoDialog(requireContext())
                    lottoDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    lottoDialog?.setCanceledOnTouchOutside(false)
                }

                if (lottoDialog != null && lottoDialog?.isShowing == false) {
                    lottoDialog?.show()
                    lottoDialog?.binding?.buttonCancel?.setOnClickListener { // No
                        var resultCount = lottoDialog?.binding?.textResult?.text.toString().toInt()
                        lottoDialog?.dismiss()
                        lottoDialog = null

                        lottoCount--
                        regLottoCount()
                        binding.textLottoCount.text = "${lottoCount}개"

                        ticketCount += resultCount
                        regTicketCount()
                        Toast.makeText(
                            requireActivity(),
                            "투표권이 ${resultCount}장 추가되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }*/
        }

        checkMaintainance() // 시즌교체 작업

        // 관리자 모드
        binding.layoutReward.setOnClickListener {
            println("$adminString")
            if (adminString == "39753179") {
                questCount = 5
                sharedPreferences.putIntDate(MySharedPreferences.PREF_KEY_QUEST_COUNT, questCount)
                Toast.makeText(context,"관리자모드 실행", Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutHotTime.setOnClickListener {
            if (binding.layoutHotTimeInfo.visibility == View.GONE) {
                val anim = AnimationUtils.loadAnimation(context, R.anim.translate_up)
                binding.layoutHotTimeInfo.startAnimation(anim)
                binding.layoutHotTimeInfo.visibility = View.VISIBLE
                binding.layoutHotTimeInfo.isSelected = true
                binding.layoutHotTimeInfo.requestFocus()
            } else {
                closeHotTimeInfo()
            }
        }

        binding.layoutHotTimeInfo.setOnClickListener {
            closeHotTimeInfo()
        }
    }

    private fun closeHotTimeInfo() {
        if (binding.layoutHotTimeInfo.visibility == View.VISIBLE) {
            val anim = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutHotTimeInfo.startAnimation(anim)
            binding.layoutHotTimeInfo.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun observePreferences() {
        firebaseViewModel.preferencesDTO.observe(viewLifecycleOwner) {
            if (firebaseViewModel.preferencesDTO.value != null) {
                if (oldRewardCount == 0)
                    oldRewardCount = firebaseViewModel.preferencesDTO.value?.rewardCount!!

                questCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_QUEST_COUNT, 0)

                // 남은 광고 카운트 또는 날짜가 바뀌면 최대값으로 초기화 됨
                var rewardMaxCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_REWARD_MAX_COUNT, 0)
                var rewardCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_REWARD_COUNT, firebaseViewModel.preferencesDTO.value?.rewardCount!!)
                if (rewardMaxCount < firebaseViewModel.preferencesDTO.value?.rewardCount!!) { // 카운트 추가해줘야 할 경우 (핫타임 등으로 광고 횟수가 늘어남)
                    rewardCount = rewardCount.plus(firebaseViewModel.preferencesDTO.value?.rewardCount!! - rewardMaxCount)
                    sharedPreferences.putIntDate(MySharedPreferences.PREF_KEY_REWARD_COUNT, rewardCount)
                }

                // 최대 카운트를 기록해 줘야 늘었다가 감소했을 경우에도 적용이 됨 (100 -> 50 -> 150 이면 max 카운트가 계속 증가만 하기 때문에 오후 8시 핫타임 적용이 안될 수 있음)
                sharedPreferences.putIntDate(MySharedPreferences.PREF_KEY_REWARD_MAX_COUNT, firebaseViewModel.preferencesDTO.value?.rewardCount!!)

                if (rewardCount > firebaseViewModel.preferencesDTO.value?.rewardCount!!) { // 최대 카운트 보다 클 경우(핫타임 종료 등) 최대 카운트 까지만
                    rewardCount = firebaseViewModel.preferencesDTO.value?.rewardCount!!
                    sharedPreferences.putIntDate(MySharedPreferences.PREF_KEY_REWARD_COUNT, rewardCount)
                }

                binding.textRewardCount.text = "남은횟수:$rewardCount"

                // 핫타임 설정
                if (firebaseViewModel.preferencesDTO.value?.runHotTime!!) {
                    binding.layoutHotTime.visibility = View.VISIBLE
                    binding.layoutFireBack.visibility = View.VISIBLE
                    binding.layoutMain.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.hot_time_back))
                    //binding.layoutTicketCount.setBackgroundColor(Color.parseColor("#4E1212"))
                    //binding.recyclerviewVote.setBackgroundColor(Color.parseColor("#812631"))
                    //binding.recyclerviewVote.setBackgroundResource(R.drawable.gradient_gold)
                    //binding.layoutReward.setBackgroundColor(Color.parseColor("#4E1212"))

                    var totalsec = firebaseViewModel.preferencesDTO.value?.rewardIntervalTimeSec!!
                    var min = (totalsec % 3600) / 60
                    var sec = totalsec % 60
                    var rewardTime = ""
                    if (min > 0)
                        rewardTime = "${min}분"
                    if (sec > 0)
                        rewardTime += "${sec}초"
                    if (!firebaseViewModel.preferencesDTO.value?.hotTimeTitle.isNullOrEmpty()) {
                        binding.textHotTimeTitle.text = firebaseViewModel.preferencesDTO.value?.hotTimeTitle
                    }
                    binding.textHotTimeTicket.text = "\uD83D\uDD25티켓 충전 시간 : ${firebaseViewModel.preferencesDTO.value?.IntervalTime}분"
                    binding.textHotTimeRewardTime.text = "\uD83D\uDD25광고 충전 시간 : $rewardTime"
                    binding.textHotTimeRewardCount.text = "\uD83D\uDD25광고 티켓 : ${firebaseViewModel.preferencesDTO.value?.rewardCount}개"

                } else {
                    binding.layoutHotTime.visibility = View.GONE
                    binding.layoutFireBack.visibility = View.GONE
                    binding.layoutMain.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.normal_time_back))

                    //binding.layoutTicketCount.setBackgroundColor(Color.parseColor("#262A35"))
                    //binding.recyclerviewVote.setBackgroundColor(Color.parseColor("#0C276A"))
                    //binding.layoutReward.setBackgroundColor(Color.parseColor("#262A35"))
                }
                setRewardTimer()
            }
        }
    }

    private fun observeUser() {
        firebaseViewModel.userDTO.observe(viewLifecycleOwner) {
            if (firebaseViewModel.userDTO.value != null) {
                val preferencesDTO = (activity as MainActivity?)?.getPreferences()!! // 타이밍상 획득 못할 수도 있기 때문에 메인에서 사용
                if (firebaseViewModel.userDTO.value!!.isPremium()) {
                    binding.imgPremiumDouble.visibility = View.VISIBLE
                    if (!isRunRewardTimer)
                        binding.textRewardTimer.text = preferencesDTO.rewardNamePremium
                } else {
                    binding.imgPremiumDouble.visibility = View.GONE
                    if (!isRunRewardTimer)
                        binding.textRewardTimer.text = preferencesDTO.rewardName
                }
            }
        }
    }

    private fun refreshPeople() {
        (activity as MainActivity?)?.loading()
        val userDTO = (activity as MainActivity?)?.getUser()!!
        firebaseViewModel.getPeople(FirebaseRepository.PeopleOrder.NAME_ASC)
        firebaseViewModel.peopleDTOs.observe(viewLifecycleOwner) {
            if (firebaseViewModel.peopleDTOs.value != null) {
                var peopleExDTOs : ArrayList<RankExDTO> = arrayListOf()
                for (person in firebaseViewModel.peopleDTOs.value!!) {
                    var isFavorite = false
                    if (userDTO.favorites.contains(person.docname)) {
                        isFavorite = true
                    }
                    peopleExDTOs.add(RankExDTO(person, isFavorite))
                }
                peopleExDTOs.sortByDescending { it.favorite }

                //recyclerViewAdapter = RecyclerViewAdapterVote(firebaseViewModel.peopleDTOs.value!!, this)
                recyclerViewAdapter = RecyclerViewAdapterVote(peopleExDTOs, this)
                binding.recyclerviewVote.adapter = recyclerViewAdapter
            }
        }
        (activity as MainActivity?)?.loadingEnd()
    }

    override fun onItemClick(item: RankExDTO, position: Int) {
        adminString += "${position+1}"

        var userDTO = (activity as MainActivity?)?.getUser()!!
        if (userDTO.ticketCount!! > 0) { // 티켓이 존재 하면 투표 가능
            questCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_QUEST_COUNT, 0)

            if (voteDialog != null )
                voteDialog = null

            if (voteDialog == null) {
                voteDialog = QuestionDialogVote(requireContext(), item, userDTO.ticketCount!!, questCount, firebaseViewModel.preferencesDTO.value?.questCount!!)
                voteDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                voteDialog?.setCanceledOnTouchOutside(false)
            }

            if (voteDialog != null && voteDialog?.isShowing == false) {
                voteDialog?.userDTO = userDTO
                voteDialog?.show()
                voteDialog?.binding?.buttonCancel?.setOnClickListener { // No
                    voteDialog?.dismiss()
                    voteDialog = null
                }

                voteDialog?.binding?.layoutFavorite?.setOnClickListener {
                    val isAdd = !userDTO.favorites.contains(item.rankDTO?.docname)

                    val question = QuestionDTO(QuestionDTO.Stat.INFO, "[${item.rankDTO?.name}] 최애로 등록"
                        , "최애로 등록된 가수는 가장 먼저 표시됩니다. 등록하시겠습니까?\n(새로고침 필요)")
                    if (!isAdd) { // 이미 등록되어 있으면 삭제
                        question.title = "[${item.rankDTO?.name}] 최애 취소"
                        question.content = "최애로 등록되어 있습니다. 취소 하시겠습니까?"
                    }

                    val dialog = QuestionDialog(requireContext(), question)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                    dialog.binding.buttonQuestionCancel.setOnClickListener { // No
                        dialog.dismiss()
                    }
                    dialog.binding.buttonQuestionOk.setOnClickListener { // No
                        dialog.dismiss()
                        val msg = if (isAdd) { // 최애 등록
                            item.favorite = true
                            userDTO.favorites.add(item.rankDTO?.docname.toString())
                            "[${item.rankDTO?.name}] 가수님이 최애 가수로 등록되었습니다."
                        } else { // 최애 삭제
                            item.favorite = false
                            userDTO.favorites.remove(item.rankDTO?.docname.toString())
                            "[${item.rankDTO?.name}] 가수님이 최애에서 취소되었습니다."
                        }
                        firebaseViewModel.updateUserFavorites(userDTO) {
                            voteDialog?.item = item
                            voteDialog?.setFavorite()
                            callToast(msg)
                        }
                    }
                }

                voteDialog?.binding?.buttonOk?.setOnClickListener { // Yes
                    val seasonDTO = (activity as MainActivity?)?.getSeason()!!
                    var voteCount = voteDialog?.binding?.textInputCount?.text.toString().toInt()
                    voteDialog?.dismiss()
                    voteDialog = null

                    (activity as MainActivity?)?.loading()
                    val oldTicketCount = userDTO.ticketCount
                    val ticketCount = userDTO.ticketCount?.minus(voteCount)
                    firebaseViewModel.useUserTicket(userDTO.uid.toString(), voteCount, seasonDTO.seasonNum!!, item.rankDTO?.docname.toString()) {

                    }

                    // 전체 득표 수 증가
                    //item.count = item.count?.plus(1)
                    val oldCount = item.rankDTO?.count
                    item.rankDTO?.count = item.rankDTO?.count?.plus(voteCount)

                    firebaseViewModel.addVoteTicket(item.rankDTO?.docname.toString(), voteCount) {
                        item.rankDTO?.count = it?.count

                        binding.recyclerviewVote.adapter?.notifyItemChanged(position)

                        var log = LogDTO("${item.rankDTO?.name} 에게 투표 (${oldCount} -> ${item.rankDTO?.count}, 보유 티켓 $oldTicketCount -> ${ticketCount})", Date())
                        firebaseViewModel.writeUserLog(userDTO.uid.toString(), log) { }
                        (activity as MainActivity?)?.loadingEnd()
                    }

                    if (!QuestDTO("투표하기", "최애에게 1표 이상 투표하기", 1, userDTO.questSuccessTimes["1"], userDTO.questGemGetTimes["1"]).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
                        userDTO.questSuccessTimes["1"] = Date()
                        firebaseViewModel.updateUserQuestSuccessTimes(userDTO) {
                            callToast("일일 과제 달성! 보상을 획득하세요!")
                        }
                    } else {
                        callToast("$voteCount 표 투표 성공!")
                    }

                    // Firestore에 기록
                    //firestore?.collection("people")?.document(item.docname.toString())?.set(item)
                }
            }
        } else { // 티켓이 없으면 투표 불가능
            Toast.makeText(requireActivity(), "투표할 수 있는 투표권이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkMaintainance() {
        // 시즌교체 작업
        /*firestore?.collection("preferences")?.document("update")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
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
                dialog.binding.buttonQuestionCancel.setOnClickListener { // No
                    dialog.dismiss()
                    (activity as MainActivity?)!!.appExit()
                }
            }
        }*/
    }

    private fun setRewardTimer() {
        var rewardTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_REWARD_TIME, 0L)

        // 인터벌 시간하고, 최대개수 디비로 설정하여 핫타임때 적용하도록
        var intervalMillis = firebaseViewModel.preferencesDTO.value!!.getRewardIntervalTimeMillis()

        var interval = 0L
        if (rewardTime != 0L) {
            interval = (rewardTime + intervalMillis) - System.currentTimeMillis()
        }

        println("데이터 확인 rewardTime = $rewardTime, intervalMillis = $intervalMillis, interval = $interval ")

        // 타이머가 동작중이면 종료 후 다시 실행
        if (isRunRewardTimer)
            rewardTimer.cancel()

        isRunRewardTimer = true
        rewardTimer = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                isRunRewardTimer = false
                //isReward = true

                val userDTO = (activity as MainActivity?)?.getUser()!!
                if (userDTO.isPremium()) {
                    binding.imgPremiumDouble.visibility = View.VISIBLE
                    binding.textRewardTimer.text = firebaseViewModel.preferencesDTO.value!!.rewardNamePremium
                } else {
                    binding.imgPremiumDouble.visibility = View.GONE
                    binding.textRewardTimer.text = firebaseViewModel.preferencesDTO.value!!.rewardName
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                var totalsec = millisUntilFinished / 1000
                var min = (totalsec % 3600) / 60
                var sec = totalsec % 60

                binding.textRewardTimer.text = "${String.format("%02d", min)}:${String.format("%02d", sec)}"
            }

        }.start()
    }

    private fun rewardGem() {
        questCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_QUEST_COUNT, 0)
        questCount++
        sharedPreferences.putIntDate(MySharedPreferences.PREF_KEY_QUEST_COUNT, questCount)

        var rewardCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_REWARD_COUNT, firebaseViewModel.preferencesDTO.value!!.rewardCount!!)
        rewardCount = rewardCount.minus(1)
        sharedPreferences.putIntDate(MySharedPreferences.PREF_KEY_REWARD_COUNT, rewardCount)

        sharedPreferences.putLong(MySharedPreferences.PREF_KEY_REWARD_TIME, System.currentTimeMillis())
        setRewardTimer()

        val userDTO = (activity as MainActivity?)?.getUser()!!
        if (userDTO.isPremium()) { // 프리미엄 패키지 보상 2배
            addGem(firebaseViewModel.preferencesDTO.value!!.rewardBonus!!.times(2))
        } else {
            addGem(firebaseViewModel.preferencesDTO.value!!.rewardBonus!!)
        }

        binding.textRewardCount.text = "남은횟수:$rewardCount"

        // 일일 퀘스트 - 무료 다이아 광고 시청 시 적용
        if (!QuestDTO("무료 다이아", "광고 보고 무료 다이아 1회 받기 ", 1, userDTO.questSuccessTimes["4"], userDTO.questGemGetTimes["4"]).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
            userDTO.questSuccessTimes["4"] = Date()
            firebaseViewModel.updateUserQuestSuccessTimes(userDTO) {
                callToast("일일 과제 달성! 보상을 획득하세요!")
            }
        }
    }

    private fun addGem(gemCount: Int) {
        val user = (activity as MainActivity?)?.getUser()!!
        val oldFreeGemCount = user.freeGem!!
        firebaseViewModel.addUserGem(user.uid.toString(), 0, gemCount) { userDTO ->
            if (userDTO != null) {
                var log = LogDTO("[사용자 무료 다이아 획득] 다이아 $gemCount 획득 (freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                val getDialog = GetItemDialog(requireContext())
                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                getDialog.setCanceledOnTouchOutside(false)
                getDialog.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
                getDialog.show()

                getDialog.binding.buttonGetItemOk.setOnClickListener {
                    getDialog.dismiss()
                }
            }
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentPageVote.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentPageVote().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
