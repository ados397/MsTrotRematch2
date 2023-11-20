package com.ados.mstrotrematch2.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.adapter.OnFanClubItemClickListener
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterFanClub
import com.ados.mstrotrematch2.databinding.FragmentFanClubJoinBinding
import com.ados.mstrotrematch2.dialog.ImageViewDialog
import com.ados.mstrotrematch2.dialog.QuestionDialog
import com.ados.mstrotrematch2.firebase.FirebaseRepository
import com.ados.mstrotrematch2.firebase.FirebaseStorageViewModel
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.*
import com.ados.mstrotrematch2.util.Utility
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubJoin.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubJoin : Fragment(), OnFanClubItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubJoinBinding? = null
    private val binding get() = _binding!!

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    private lateinit var callback: OnBackPressedCallback

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClub

    private var questionDialog: QuestionDialog? = null
    private var imageViewDialog: ImageViewDialog? = null

    private var selectedFanClub: FanClubDTO? = null
    private var selectedPosition: Int? = 0

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
        _binding = FragmentFanClubJoinBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_fan_club)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        (activity as MainActivity?)?.loading()
        firebaseViewModel.getFanClub(FirebaseRepository.FanClubOrder.NAME_ASC)
        firebaseViewModel.fanClubDTOs.observe(viewLifecycleOwner) {
            setAdapter()
        }

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCancel.setOnClickListener {
            selectRecyclerView()
        }

        binding.buttonJoin.setOnClickListener {
            var userDTO = (activity as MainActivity?)?.getUser()!!
            val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!

            firebaseViewModel.getVoteCountTotal(userDTO.uid.toString(), selectedFanClub?.docName.toString()) { voteCount ->
                if (voteCount < preferencesDTO.fanClubMembershipConditions!!) {
                    val question = QuestionDTO(
                        QuestionDTO.Stat.WARNING,
                        "팬클럽 가입",
                        "[${selectedFanClub?.name}]에 가입하기 위해서는 ${selectedFanClub?.singerName}님에게 ${decimalFormat.format(preferencesDTO.fanClubMembershipConditions)}표 이상 투표 해야 합니다.\n* 현재 나의 투표수: ${decimalFormat.format(voteCount)}\n\n* 팬이 아닌 사용자의 가입을 막기 위한 조치 입니다.",
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
                    questionDialog?.showButtonOk(false)
                    questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                        questionDialog?.dismiss()
                        questionDialog = null
                    }
                } else if (isBlockFanClubJoin(userDTO)) {
                    val question = QuestionDTO(
                        QuestionDTO.Stat.WARNING,
                        "팬클럽 가입",
                        "팬클럽 탈퇴 혹은 추방 시, 24시간이 지나야 팬클럽에 가입 요청할 수 있습니다.\n\n탈퇴일 [${SimpleDateFormat("yyyy.MM.dd HH:mm").format(userDTO.fanClubQuitDate!!)}]",
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
                    questionDialog?.showButtonOk(false)
                    questionDialog?.setButtonCancel("확인")
                    questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                        questionDialog?.dismiss()
                        questionDialog = null
                    }
                } else {
                    val question = QuestionDTO(
                        QuestionDTO.Stat.INFO,
                        "팬클럽 가입",
                        "[${selectedFanClub?.name}]에 가입할 수 있습니다.\n* 가입조건:${selectedFanClub?.singerName}님에게 ${decimalFormat.format(preferencesDTO.fanClubMembershipConditions)}표 투표\n* 현재 나의 투표수: ${decimalFormat.format(voteCount)}\n\n가입 하시겠습니까?",
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
                    questionDialog?.setButtonCancel("확인")
                    questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                        questionDialog?.dismiss()
                        questionDialog = null
                    }
                    questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // OK
                        questionDialog?.dismiss()

                        userDTO.fanClubId = selectedFanClub?.docName
                        firebaseViewModel.updateUserFanClubId(userDTO) {
                            Toast.makeText(requireActivity(), "팬클럽 가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                            firebaseViewModel.updateFanClubMemberCount(selectedFanClub?.docName.toString(), 1) {
                                val displayText = "* ${userDTO?.nickname}님이 팬클럽에 가입되셨습니다! 환영인사를 건네보세요."
                                val chat = DisplayBoardDTO(Utility.randomDocumentName(), displayText, null, null, null, 0, Date())
                                firebaseViewModel.sendFanClubChat(selectedFanClub?.docName.toString(), chat) {
                                    // 팬클럽 로그 기록
                                    var log = LogDTO("[팬클럽 가입] 사용자 정보 (uid : ${userDTO.uid}, nickname : ${userDTO.nickname})", Date())
                                    firebaseViewModel.writeFanClubLog(selectedFanClub?.docName.toString(), log) {
                                        val fragment = FragmentPageFanClub()
                                        parentFragmentManager.beginTransaction().apply{
                                            replace(R.id.layout_fragment, fragment)
                                            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                            addToBackStack(null)
                                            commit()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }



        }
    }

    private fun setAdapter() {
        var uidSet = hashSetOf<String>()
        val itemsEx: ArrayList<FanClubExDTO> = arrayListOf()
        val userDTO = (activity as MainActivity?)?.getUser()!!
        for (fanClub in firebaseViewModel.fanClubDTOs.value!!) {
            if (!fanClub.docName.isNullOrEmpty()) {
                var isFavorite = false
                if (userDTO.favorites.contains(fanClub.docName)) {
                    isFavorite = true
                }
                itemsEx.add(FanClubExDTO(fanClub, isFavorite))

                uidSet.add(fanClub.docName.toString())
            }
        }
        itemsEx.sortByDescending { it.favorite }

        recyclerViewAdapter = RecyclerViewAdapterFanClub(itemsEx, this@FragmentFanClubJoin)
        recyclerView.adapter = recyclerViewAdapter
        (activity as MainActivity?)?.loadingEnd()

        /*var uriCheckIndex = 0
        for (uid in uidSet) {
            firebaseStorageViewModel.getFanClubSymbolImage(uid) { uri ->
                if (uri != null) {
                    for (item in itemsEx) {
                        if (item.fanClubDTO?.docName == uid) {
                            item.imgSymbolCustomUri = uri
                        }
                    }
                }
                uriCheckIndex++
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            while(true) {
                if (uriCheckIndex == uidSet.size) {
                    recyclerViewAdapter = RecyclerViewAdapterFanClub(itemsEx, this@FragmentFanClubJoin)
                    recyclerView.adapter = recyclerViewAdapter
                    (activity as MainActivity?)?.loadingEnd()
                    break
                }
                delay(100)
            }
        }*/
    }

    private fun openLayout() {
        if (binding.layoutMenu.visibility == View.GONE) {
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.startAnimation(translateUp)
        }
        binding.layoutMenu.visibility = View.VISIBLE
        //recyclerView.smoothSnapToPosition(position)
    }

    private fun closeLayout() {
        if (binding.layoutMenu.visibility == View.VISIBLE) {
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.startAnimation(translateDown)
        }
        binding.layoutMenu.visibility = View.GONE
        //recyclerView.smoothSnapToPosition(position)
    }

    private fun selectRecyclerView() {
        if (recyclerViewAdapter.selectItem(selectedPosition!!)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
            openLayout()
        } else { // 해제 일 경우 메뉴 숨김 및 레이아웃 밝게
            closeLayout()
        }
    }

    private fun isBlockFanClubJoin(user: UserDTO) : Boolean {
        // 팬클럽 탈퇴 후 24시간이 지나야 재 가입이 가능
        var isBlock = false
        if (user.fanClubQuitDate != null) {
            val calendar= Calendar.getInstance()
            calendar.time = user.fanClubQuitDate!!
            calendar.add(Calendar.DATE, 1)

            if (Date() < calendar.time) {
                isBlock = true
            }
        }
        return isBlock
    }

    override fun onItemClick(item: FanClubExDTO, position: Int) {
        selectedFanClub = item.fanClubDTO
        selectedPosition = position
        binding.buttonJoin.text = "${item.fanClubDTO?.name} 가입"
        selectRecyclerView()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubJoin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubJoin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}