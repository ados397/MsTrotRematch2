package com.ados.mstrotrematch2.page

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.FragmentFanClubInfoBinding
import com.ados.mstrotrematch2.dialog.*
import com.ados.mstrotrematch2.model.*
import com.ados.mstrotrematch2.firebase.FirebaseRepository
import com.ados.mstrotrematch2.util.AdsRewardManager
import com.ados.mstrotrematch2.firebase.FirebaseStorageViewModel
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.util.AdsInterstitialManager
import com.ados.mstrotrematch2.util.Utility
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubInfo.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubInfo : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private var _binding: FragmentFanClubInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    // 뷰모델 연결
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    // AD
    private var adsRewardManagerExp: AdsRewardManager? = null
    private var adsRewardManagerGem: AdsRewardManager? = null
    private var adsInterstitialManager : AdsInterstitialManager? = null

    //lateinit var recyclerView : RecyclerView
    //lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubMember

    private var userDTO: UserDTO? = null
    private var fanClubDTO: FanClubDTO? = null
    //private var fanClubExDTO: FanClubExDTO? = null
    //private var currentMember: MemberDTO? = null

    private var toast : Toast? = null

    private var questionDialog: QuestionDialog? = null
    private var gemQuestionDialog: GemQuestionDialog? = null
    //private var levelUpActionFanClubDialog: LevelUpActionFanClubDialog? = null
    //private var expUpFanClubDialog: ExpUpFanClubDialog? = null
    //private var levelUpFanClubDialog: LevelUpFanClubDialog? = null
    private var editTextModifyDialog: EditTextModifyDialog? = null
    //private var sendNoticeDialog: SendNoticeDialog? = null
    //private var fanClubRewardDialog: FanClubRewardDialog? = null
    //private var selectFanClubSymbolDialog: SelectFanClubSymbolDialog? = null
    private var imageViewDialog: ImageViewDialog? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    /*private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            modifySymbolApply("add_image", uri)
        } else {
            Toast.makeText(context, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }*/

    private var isSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        userDTO = (activity as MainActivity?)?.getUser()!!
        fanClubDTO = (parentFragment as FragmentPageFanClub?)?.getFanClub()
        //fanClubExDTO = (activity as MainActivity?)?.getFanClubEx()
        //currentMember = (activity as MainActivity?)?.getMember()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFanClubInfoBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        val adPolicyDTO = (activity as MainActivity?)?.getAdPolicy()!!
        //adsRewardManagerExp = AdsRewardManager(requireActivity(), adPolicyDTO, AdsRewardManager.RewardType.REWARD_FAN_CLUB_EXP)
        //adsRewardManagerGem = AdsRewardManager(requireActivity(), adPolicyDTO, AdsRewardManager.RewardType.REWARD_FAN_CLUB_GEM)

        firebaseViewModel.getFanClubCheckoutCount(fanClubDTO?.docName.toString()) { checkoutCount->
            binding.textCheckoutCount.text = "${decimalFormat.format(checkoutCount)}"
        }

        return rootView
    }

    override fun onDestroyView() {
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
        super.onViewCreated(view, savedInstanceState)

        binding.imgSettings.visibility = View.GONE
        binding.buttonModifyName.visibility = View.GONE
        binding.buttonModifyNotice.visibility = View.GONE
        binding.layoutLevel.visibility = View.GONE
        binding.buttonSendNotice.visibility = View.GONE
        binding.layoutMember.visibility = View.GONE
        binding.buttonDonation.visibility = View.GONE
        binding.buttonReward.visibility = View.GONE

        setInfo()

        binding.textNoticeContent.movementMethod = ScrollingMovementMethod.getInstance()
        binding.textNoticeContent.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as MainActivity?)?.loading()
            firebaseViewModel.getFanClub(userDTO?.fanClubId.toString()) {
                fanClubDTO = it

                firebaseViewModel.getFanClubCheckoutCount(fanClubDTO?.docName.toString()) { checkoutCount->
                    binding.textCheckoutCount.text = "${decimalFormat.format(checkoutCount)}"

                    setInfo()
                    (activity as MainActivity?)?.loadingEnd()
                    callToast("새로 고침")
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        binding.buttonCheckout.setOnClickListener {
            if (userDTO?.isFanClubCheckout()!!) {
                callToast("이미 출석체크 하였습니다.")
            } else {
                val question = QuestionDTO(
                    QuestionDTO.Stat.INFO,
                    "팬클럽 출석체크",
                    "팬클럽 출석체크를 지금 하시겠습니까?",
                )
                if (questionDialog == null) {
                    questionDialog = QuestionDialog(requireContext(), question)
                    questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    questionDialog?.setCanceledOnTouchOutside(false)
                } else {
                    questionDialog?.question = question
                }
                questionDialog?.show()
                questionDialog?.setInfo()
                questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                    questionDialog?.dismiss()
                    questionDialog = null
                }
                questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                    questionDialog?.dismiss()
                    questionDialog = null
                    (activity as MainActivity?)?.loading()
                    val preferencesDTO = (activity as MainActivity?)?.getPreferences()
                    val adPolicyDTO = (activity as MainActivity?)?.getAdPolicy()!!

                    adsInterstitialManager = AdsInterstitialManager(requireActivity(), adPolicyDTO)
                    adsInterstitialManager?.callInterstitial { }

                    // 오늘날짜 출석체크 등록
                    val date = Date()
                    userDTO?.fanClubCheckoutTime = date
                    firebaseViewModel.updateFanClubCheckout(userDTO?.uid.toString()) {
                        /*var exp = (activity as MainActivity?)?.getPreferences()?.rewardUserExp!!
                        if (currentUserEx?.userDTO?.isPremium()!!) { // 프리미엄 패키지 사용중이라면 경험치 두배
                            exp = exp.times(2)
                        }

                        // 경험치 추가 적용
                        applyExp(exp, 0, null)*/

                        firebaseViewModel.updateFanClubCheckoutCount(fanClubDTO?.docName.toString()) { newCount->
                            binding.textCheckoutCount.text = "${decimalFormat.format(newCount)}"
                        }

                        (activity as MainActivity?)?.loadingEnd()
                        setInfo()

                        // 다이아 추가
                        if (userDTO?.isPremium()!!) {
                            addGem(preferencesDTO?.rewardUserCheckoutGem!!.times(2))
                        } else {
                            addGem(preferencesDTO?.rewardUserCheckoutGem!!)
                        }
                    }
                }
            }
        }

        binding.buttonFanClubQuit.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "팬클럽 탈퇴",
                "팬클럽 탈퇴 시 24시간 동안 팬클럽 가입이 제한됩니다. 정말 탈퇴 하시겠습니까?",
            )
            if (questionDialog == null) {
                questionDialog = QuestionDialog(requireContext(), question)
                questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog?.setCanceledOnTouchOutside(false)
            } else {
                questionDialog?.question = question
            }
            questionDialog?.show()
            questionDialog?.setInfo()
            questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                questionDialog?.dismiss()
                questionDialog = null
            }
            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                questionDialog?.dismiss()
                questionDialog = null

                (activity as MainActivity?)?.loading()

                userDTO?.fanClubId = null
                firebaseViewModel.updateUserFanClubId(userDTO!!) {
                    firebaseViewModel.updateFanClubMemberCount(fanClubDTO?.docName.toString(), -1) {
                        var log = LogDTO("[클럽원 탈퇴] ${userDTO?.nickname}(${userDTO?.uid.toString()})", Date())
                        firebaseViewModel.writeFanClubLog(fanClubDTO?.docName.toString(), log) { }

                        Toast.makeText(requireActivity(), "팬클럽을 탈퇴하였습니다.", Toast.LENGTH_SHORT).show()

                        val displayText = "* ${userDTO?.nickname}님이 팬클럽을 탈퇴하셨습니다."
                        val chat = DisplayBoardDTO(Utility.randomDocumentName(), displayText, null, null, null, 0, Date())
                        firebaseViewModel.sendFanClubChat(fanClubDTO?.docName.toString(), chat) {
                            (parentFragment as FragmentPageFanClub?)?.quitFanClub()
                        }
                        (activity as MainActivity?)?.loadingEnd()
                    }
                }
            }
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    private fun setInfo() {
        if (fanClubDTO != null) {
            binding.textName.text = fanClubDTO?.name
            binding.textNoticeContent.text = fanClubDTO?.notice
            binding.textMemberCount.text = "${decimalFormat.format(fanClubDTO?.memberCount)}"
            val imageID = resources.getIdentifier(fanClubDTO?.imgSymbol, "drawable", requireContext().packageName)
            binding.imgSymbol.setImageResource(imageID)
        }

        if (userDTO?.isPremium()!!) {
            binding.imgPremium.visibility = View.VISIBLE
        } else {
            binding.imgPremium.visibility = View.GONE
        }

        binding.imgCheckout.setImageResource(userDTO?.getFanClubCheckoutImage()!!)
    }

    private fun addGem(gemCount: Int) {
        userDTO = (activity as MainActivity?)?.getUser()!!
        val oldFreeGemCount = userDTO?.freeGem!!
        firebaseViewModel.addUserGem(userDTO?.uid.toString(), 0, gemCount) { user ->
            if (user != null) {
                userDTO = user
                var log = LogDTO("[팬클럽 출석체크 다이아 획득] 다이아 $gemCount 획득 (freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", Date())
                firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }

                val getDialog = GetItemDialog(requireContext())
                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                getDialog.setCanceledOnTouchOutside(false)
                getDialog.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
                getDialog.show()

                getDialog.binding.buttonGetItemOk.setOnClickListener {
                    getDialog.dismiss()

                    /* 레벨업 기능은 비활성
                    if (dialog != null) {
                        dialog.oldUserDTO = currentUserEx?.userDTO?.copy()
                        dialog.newUserDTO = currentUserEx?.userDTO?.copy()
                        dialog.setInfo()
                    }*/
                }
            }

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubInfo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubInfo().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}