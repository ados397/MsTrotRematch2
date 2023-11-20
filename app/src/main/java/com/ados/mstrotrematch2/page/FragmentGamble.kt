package com.ados.mstrotrematch2.page

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.FragmentGambleBinding
import com.ados.mstrotrematch2.dialog.GemQuestionDialog
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.GemQuestionDTO
import com.ados.mstrotrematch2.model.LogDTO
import com.ados.mstrotrematch2.model.UserDTO
import com.ados.mstrotrematch2.util.AdsRewardManager
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentGamble.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentGamble : Fragment() {
    enum class GambleType {
        GAMBLE_10, GAMBLE_30, GAMBLE_100
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentGambleBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var adsRewardManager: AdsRewardManager? = null

    private lateinit var callback: OnBackPressedCallback
    private var toast : Toast? = null

    private var gemQuestionDialog: GemQuestionDialog? = null

    private var mIsBusy = false
    private var mGambleType = GambleType.GAMBLE_10
    private var mGambleCount = 0L
    private var mGambleCompleteCount = 0L
    private var currentDate = "" // 12시 지나서 날짜 변경을 체크하기 위한 변수

    private var safeStatus = false

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
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGambleBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        val adPolicyDTO = (activity as MainActivity?)?.getAdPolicy()!!
        adsRewardManager = AdsRewardManager(requireActivity(), adPolicyDTO, AdsRewardManager.RewardType.REWARD_GAMBLE_COUNT)
        //currentDate  = SimpleDateFormat("yyyyMMdd").format(Date())

        binding.layoutResult.visibility = View.GONE
        binding.buttonGambleResult.visibility = View.GONE
        binding.buttonGambleFinish.visibility = View.GONE
        binding.imgDiamond.visibility = View.GONE
        binding.textGambleCount.visibility = View.GONE
        //binding.layoutPremium.visibility = View.GONE

        showGambleCount {
            binding.textGambleCount.visibility = View.VISIBLE
        }

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonGamble10.setOnClickListener {
            mGambleType = GambleType.GAMBLE_10
            gambleStart()
        }

        binding.buttonGamble30.setOnClickListener {
            mGambleType = GambleType.GAMBLE_30
            gambleStart()
        }

        binding.buttonGamble100.setOnClickListener {
            mGambleType = GambleType.GAMBLE_100
            gambleStart()
        }

        binding.buttonGambleResult.setOnClickListener {
            (binding.imgDiamond.background as AnimationDrawable).stop()
            binding.buttonGambleResult.visibility = View.GONE

            gambleResult()
        }

        binding.buttonGambleFinish.setOnClickListener {
            Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_back).optionalFitCenter().into(binding.imgBackground)
            binding.imgDiamond.visibility = View.GONE
            binding.buttonGambleResult.visibility = View.GONE
            binding.buttonGambleFinish.visibility = View.GONE
            binding.layoutResult.visibility = View.GONE
            binding.layoutButton.visibility = View.VISIBLE
            binding.textInfo2.text = "투표권 100장"
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun callBackPressed() {
        if (mIsBusy) {
            callToast("뽑기 중에는 종료할 수 없습니다.")
        } else {
            finishFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        safeStatus = true
    }

    private fun finishFragment() {
        if (safeStatus) {
            val fragment = FragmentPageVote()
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun showGambleCount(myCallback: (Boolean) -> Unit) {
        /*checkGambleCount {
            if (it) {
                var user = (activity as MainActivity?)?.getUser()!!
                val usedGambleCount = (activity as MainActivity?)?.getPreferences()?.usedGambleCount!!
                var gambleCount = usedGambleCount
                if (user.isPremium()) {  // 프리미엄 패키지 사용중이라면 뽑기 횟수 두배
                    gambleCount = gambleCount.times(2)
                    binding.layoutPremium.visibility = View.VISIBLE
                } else {
                    binding.layoutPremium.visibility = View.GONE
                }
                mGambleCount = gambleCount.minus(mGambleCompleteCount)

                if (user.isPremium()) {  // 프리미엄 패키지 사용중이라면 뽑기 횟수 두배
                    binding.textGambleCount.text = "오늘 남은 뽑기 횟수 : $mGambleCount / $gambleCount(${usedGambleCount}+${usedGambleCount})"
                } else {
                    binding.textGambleCount.text = "오늘 남은 뽑기 횟수 : $mGambleCount / $gambleCount"
                }
            }
            myCallback(it)
        }*/
    }

    private fun checkGambleCount(myCallback: (Boolean) -> Unit) {
        /*val checkDate = SimpleDateFormat("yyyyMMdd").format(Date())
        if (currentDate != checkDate) { // 날짜가 바뀌었다면 남은 횟수 다시 체크
            currentDate = checkDate
            var user = (activity as MainActivity?)?.getUser()!!
            firebaseViewModel.getTodayCompleteGambleCount(user.uid.toString()) {
                mGambleCompleteCount = it
                myCallback(true)
            }
        } else {
            myCallback(true)
        }*/
    }

    private fun gambleStart() {

            val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
            var price = 0
            var maxDiamond = 0
            when (mGambleType) {
                GambleType.GAMBLE_10 -> {
                    price = preferencesDTO.priceGamble10!!
                    maxDiamond = 10
                }
                GambleType.GAMBLE_30 -> {
                    price = preferencesDTO.priceGamble30!!
                    maxDiamond = 30
                }
                GambleType.GAMBLE_100 -> {
                    price = preferencesDTO.priceGamble100!!
                    maxDiamond = 100
                }
            }

            val question = GemQuestionDTO("다이아로 1~${maxDiamond}장의 투표권 뽑기를 하시겠습니까?", price)
            if (gemQuestionDialog == null) {
                gemQuestionDialog = GemQuestionDialog(requireContext(), question)
                gemQuestionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                gemQuestionDialog?.setCanceledOnTouchOutside(false)
            } else {
                gemQuestionDialog?.question = question
            }
            gemQuestionDialog?.show()
            gemQuestionDialog?.setInfo()
            gemQuestionDialog?.binding?.buttonGemQuestionCancel?.setOnClickListener { // No
                gemQuestionDialog?.dismiss()
            }
            gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                gemQuestionDialog?.dismiss()

                var user = (activity as MainActivity?)?.getUser()!!
                if ((user.getTotalGem()) < price) {
                    callToast("다이아가 부족합니다.")
                } else {
                    /*(activity as MainActivity?)?.loading()
                    // 다이아 차감
                    val oldPaidGemCount = user.paidGem!!
                    val oldFreeGemCount = user.freeGem!!
                    firebaseViewModel.useUserGem(user.uid.toString(), price!!) {
                        var log = LogDTO("[다이아 차감] ${maxDiamond}다이아 뽑기로 ${price} 다이아 사용 (paidGem : $oldPaidGemCount -> ${user?.paidGem}, freeGem : $oldFreeGemCount -> ${user?.freeGem})", Date())
                        firebaseViewModel.writeUserLog(user?.uid.toString(), log) { }

                        (activity as MainActivity?)?.loadingEnd()
                        callToast("뽑기에 다이아가 소모되었습니다.")

                        binding.buttonGamble10.visibility = View.GONE
                        binding.buttonGamble30.visibility = View.GONE
                        binding.buttonGamble100.visibility = View.GONE
                        binding.buttonGambleResult.visibility = View.VISIBLE
                        binding.imgDiamond.visibility = View.VISIBLE

                        (binding.imgDiamond.background as AnimationDrawable).start()
                    }*/

                    mIsBusy = true
                    binding.layoutButton.visibility = View.GONE
                    binding.buttonGambleResult.visibility = View.VISIBLE
                    binding.imgDiamond.visibility = View.VISIBLE

                    binding.textInfo2.text = "투표권 ${maxDiamond}장"

                    (binding.imgDiamond.background as AnimationDrawable).start()
                }
            }

    }

    private fun gambleResult() {
        var setPercent = arrayListOf<Int>()
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
        var price = 0
        var maxDiamond = 0
        when (mGambleType) { // 뽑기 확률 설정 (총 합이 10000이어야 함)
            GambleType.GAMBLE_10 -> {
                price = preferencesDTO.priceGamble10!!
                maxDiamond = 10
                setPercent.add(80)      // 1, 0.8%
                setPercent.add(300)     // 2, 3%
                setPercent.add(500)     // 3, 5%
                setPercent.add(1000)    // 4, 10%
                setPercent.add(3000)    // 5, 30%
                setPercent.add(3000)    // 6, 30%
                setPercent.add(1500)    // 7, 15%
                setPercent.add(500)     // 8, 5%
                setPercent.add(100)     // 9, 1%
                setPercent.add(20)      // 10, 0.2%
            }
            GambleType.GAMBLE_30 -> {
                price = preferencesDTO.priceGamble30!!
                maxDiamond = 30
                setPercent.add(10)      // 1, 0.1%
                setPercent.add(50)      // 2, 0.5%
                setPercent.add(50)      // 3, 0.5%
                setPercent.add(60)      // 4, 0.6%
                setPercent.add(100)     // 5, 1%
                setPercent.add(100)     // 6, 1%
                setPercent.add(150)     // 7, 1.5%
                setPercent.add(200)     // 8, 2%
                setPercent.add(300)     // 9, 3%
                setPercent.add(400)     // 10, 4%
                setPercent.add(400)     // 11, 4%
                setPercent.add(500)     // 12, 5%
                setPercent.add(500)     // 13, 5%
                setPercent.add(1100)    // 14, 11%
                setPercent.add(2000)    // 15, 20%
                setPercent.add(1500)    // 16, 15%
                setPercent.add(550)     // 17, 5.5%
                setPercent.add(450)     // 18, 4.5%
                setPercent.add(350)     // 19, 3.5%
                setPercent.add(300)     // 20, 3%
                setPercent.add(250)     // 21, 2.5%
                setPercent.add(200)     // 22, 2%
                setPercent.add(100)     // 23, 1%
                setPercent.add(90)      // 24, 0.9%
                setPercent.add(80)      // 25, 0.8%
                setPercent.add(70)      // 26, 0.7%
                setPercent.add(50)      // 27, 0.5%
                setPercent.add(40)      // 28, 0.4%
                setPercent.add(30)      // 29, 0.3%
                setPercent.add(20)      // 30, 0.2%
            }
            GambleType.GAMBLE_100 -> {
                price = preferencesDTO.priceGamble100!!
                maxDiamond = 100
                setPercent.add(0)       // 1, 0.00%
                setPercent.add(0)       // 2, 0.00%
                setPercent.add(0)       // 3, 0.00%
                setPercent.add(0)       // 4, 0.00%
                setPercent.add(0)       // 5, 0.00%
                setPercent.add(0)       // 6, 0.00%
                setPercent.add(0)       // 7, 0.00%
                setPercent.add(0)       // 8, 0.00%
                setPercent.add(0)       // 9, 0.00%
                setPercent.add(0)       // 10, 0.00%
                setPercent.add(0)       // 11, 0.00%
                setPercent.add(0)       // 12, 0.00%
                setPercent.add(0)       // 13, 0.00%
                setPercent.add(0)       // 14, 0.00%
                setPercent.add(0)       // 15, 0.00%
                setPercent.add(0)       // 16, 0.00%
                setPercent.add(0)       // 17, 0.00%
                setPercent.add(0)       // 18, 0.00%
                setPercent.add(0)       // 19, 0.00%
                setPercent.add(0)       // 20, 0.00%
                setPercent.add(0)       // 21, 0.00%
                setPercent.add(0)       // 22, 0.00%
                setPercent.add(0)       // 23, 0.00%
                setPercent.add(0)       // 24, 0.00%
                setPercent.add(0)       // 25, 0.00%
                setPercent.add(0)       // 26, 0.00%
                setPercent.add(0)       // 27, 0.00%
                setPercent.add(0)       // 28, 0.00%
                setPercent.add(0)       // 29, 0.00%
                setPercent.add(0)       // 30, 0.00%
                setPercent.add(38)      // 31, 0.38%
                setPercent.add(39)      // 32, 0.39%
                setPercent.add(40)      // 33, 0.40%
                setPercent.add(56)      // 34, 0.56%
                setPercent.add(57)      // 35, 0.57%
                setPercent.add(58)      // 36, 0.58%
                setPercent.add(59)      // 37, 0.59%
                setPercent.add(60)      // 38, 0.6%
                setPercent.add(61)      // 39, 0.61%
                setPercent.add(110)     // 40, 1.1%
                setPercent.add(120)     // 41, 1.2%
                setPercent.add(130)     // 42, 1.3%
                setPercent.add(140)     // 43, 1.4%
                setPercent.add(150)     // 44, 1.5%
                setPercent.add(160)     // 45, 1.6%
                setPercent.add(170)     // 46, 1.7%
                setPercent.add(180)     // 47, 1.8%
                setPercent.add(190)     // 48, 1.9%
                setPercent.add(200)     // 49, 2%
                setPercent.add(737)     // 50, 7.37%
                setPercent.add(636)     // 51, 6.36%
                setPercent.add(585)     // 52, 5.85%
                setPercent.add(563)     // 53, 5.63%
                setPercent.add(528)     // 54, 5.28%
                setPercent.add(527)     // 55, 5.27%
                setPercent.add(426)     // 56, 4.26%
                setPercent.add(325)     // 57, 3.25%
                setPercent.add(224)     // 58, 2.24%
                setPercent.add(223)     // 59, 2.23%
                setPercent.add(212)     // 60, 2.12%
                setPercent.add(201)     // 61, 2.01%
                setPercent.add(190)     // 62, 1.9%
                setPercent.add(179)     // 63, 1.79%
                setPercent.add(168)     // 64, 1.68%
                setPercent.add(157)     // 65, 1.57%
                setPercent.add(146)     // 66, 1.46%
                setPercent.add(135)     // 67, 1.35%
                setPercent.add(124)     // 68, 1.24%
                setPercent.add(113)     // 69, 1.13%
                setPercent.add(111)     // 70, 1.11%
                setPercent.add(107)     // 71, 1.07%
                setPercent.add(105)     // 72, 1.05%
                setPercent.add(103)     // 73, 1.03%
                setPercent.add(101)     // 74, 1.01%
                setPercent.add(99)      // 75, 0.99%
                setPercent.add(97)      // 76, 0.97%
                setPercent.add(95)      // 77, 0.95%
                setPercent.add(93)      // 78, 0.93%
                setPercent.add(90)      // 79, 0.9%
                setPercent.add(51)      // 80, 0.51%
                setPercent.add(50)      // 81, 0.5%
                setPercent.add(49)      // 82, 0.49%
                setPercent.add(48)      // 83, 0.48%
                setPercent.add(47)      // 84, 0.47%
                setPercent.add(46)      // 85, 0.46%
                setPercent.add(45)      // 86, 0.45%
                setPercent.add(44)      // 87, 0.44%
                setPercent.add(43)      // 88, 0.43%
                setPercent.add(42)      // 89, 0.42%
                setPercent.add(22)      // 90, 0.22%
                setPercent.add(15)      // 91, 0.15%
                setPercent.add(14)      // 92, 0.14%
                setPercent.add(13)      // 93, 0.13%
                setPercent.add(12)      // 94, 0.12%
                setPercent.add(11)      // 95, 0.11%
                setPercent.add(10)      // 96, 0.1%
                setPercent.add(8)       // 97, 0.08%
                setPercent.add(6)       // 98, 0.06%
                setPercent.add(4)       // 99, 0.04%
                setPercent.add(2)       // 100, 0.02%
            }
        }

        val resultValue = getValue(setPercent)
        var percent = ((resultValue.toDouble() / setPercent.size) * 100).toInt()
        binding.textCelebrate.visibility = View.GONE
        when (percent) {
            in 0..19 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_1).optionalFitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(0)
            }
            in 20..39 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_2).optionalFitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(1)
            }
            in 40..69 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_3).optionalFitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(3)
            }
            in 70..89 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_4).optionalFitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(2)
            }
            in 90..100 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_5).optionalFitCenter().into(binding.imgBackground)
                binding.textCelebrate.visibility = View.VISIBLE
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(6)
            }
            else -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_5).optionalFitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(1)
            }
        }

        //binding.imgDiamond.setImageResource(R.drawable.diamond_pack5)
        binding.textResult.text = "$resultValue"
        binding.layoutTextResult.visibility = View.GONE
        binding.layoutResult.visibility = View.VISIBLE
        //val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        //binding.layoutResult.startAnimation(fadeIn)

        val fadeIn = ObjectAnimator.ofFloat(binding.layoutResult, "alpha", 0f, 1f)
        fadeIn.duration = 1000
        fadeIn.start()
        fadeIn.addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                binding.layoutTextResult.visibility = View.VISIBLE
                val fadeIn2 = ObjectAnimator.ofFloat(binding.layoutTextResult, "alpha", 0f, 1f)
                fadeIn2.duration = 1000
                fadeIn2.start()
                fadeIn2.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationStart(p0: Animator) {

                    }

                    override fun onAnimationEnd(p0: Animator) {
                        mIsBusy = false
                    }

                    override fun onAnimationCancel(p0: Animator) {

                    }

                    override fun onAnimationRepeat(p0: Animator) {

                    }
                })
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })

        var user = (activity as MainActivity?)?.getUser()!!
        // 투표권 추가
        firebaseViewModel.addUserTicket(user.uid.toString(), resultValue) { userDTO->
            if (userDTO != null) {
                /*if (resultValue == 100) {
                    firebaseViewModel.sendDisplayBoard("[${user?.nickname}]님이 투표권 \uD83D\uDD25${resultValue}장\uD83D\uDD25 뽑기에 성공하셨습니다\uD83C\uDF8A", -3072, UserDTO(nickname="시스템")) {
                    }
                }*/

                var log2 = LogDTO("[투표권 추가] ${maxDiamond}투표권 뽑기로 ${resultValue} 투표권 추가 (${user.ticketCount} -> ${userDTO.ticketCount})", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log2) { }
            }
        }

        // 다이아 차감
        val oldPaidGemCount = user.paidGem!!
        val oldFreeGemCount = user.freeGem!!
        firebaseViewModel.useUserGem(user.uid.toString(), price) { userDTO ->
            if (userDTO != null) {
                var log = LogDTO("[다이아 차감] ${maxDiamond}투표권 뽑기로 ${price} 다이아 사용 (paidGem : $oldPaidGemCount -> ${userDTO.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                binding.buttonGambleFinish.visibility = View.VISIBLE
            }
        }

        //var map = mutableMapOf<Int, Int>(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0, 6 to 0, 7 to 0, 8 to 0, 9 to 0, 10 to 0)
        /*var map = mutableMapOf<Int, Int>()
        var totalValue = 0
        for (i in 1..setPercent.size) {
            map[i] = 0
            totalValue += setPercent[i-1]
        }
        println("다이아 확률 합 $totalValue (10000이 되어야 함)")

        val gambleCount = 100000
        var getCount = 0
        for (i in 0 until gambleCount) {
            val resultValue = getValue(setPercent)
            map[resultValue] = map[resultValue]!!.plus(1)
            getCount = getCount.plus(resultValue)
        }
        var re = ""
        for (m in map) {
            var percent = ((m.value?.toDouble()!! / 100000!!) * 100).toDouble()
            re += "${m.key} 다이아 - ${m.value}(${percent}%)\n"
        }
        //binding.textTest.text = re
        println(re)
        println("${maxDiamond}뽑기 횟수 : $gambleCount, 최종 소모 다이아 : ${gambleCount.times(price)}, 최종 획득 다이아 : ${getCount}")*/
    }

    private fun getValue(setPercent : ArrayList<Int>) : Int {
        var totalValue = 0
        var percentValue = 0
        var valuePair = arrayListOf<Pair<Int,Int>>()
        for (setValue in setPercent) {
            valuePair.add(Pair(percentValue.plus(1), percentValue.plus(setValue)))
            percentValue = percentValue.plus(setValue)
            totalValue = totalValue.plus(setValue)
        }
        //println("확률 총합 : $totalValue")

        var resultValue = 0
        val randomValue = Random.nextInt(1, 10001)
        for (index in 0 until valuePair.size) {
            if (randomValue in valuePair[index].first..valuePair[index].second) {
                resultValue = index.plus(1)
                break
            }
        }

        return resultValue
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
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
         * @return A new instance of fragment FragmentGamble.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentGamble().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}