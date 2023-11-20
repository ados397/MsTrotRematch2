package com.ados.mstrotrematch2.page

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.MyPagerAdapterFanClub
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterVote
import com.ados.mstrotrematch2.databinding.FragmentPageFanClubBinding
import com.ados.mstrotrematch2.databinding.FragmentPageVoteBinding
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.DisplayBoardDTO
import com.ados.mstrotrematch2.model.FanClubDTO
import com.ados.mstrotrematch2.model.PreferencesDTO
import com.ados.mstrotrematch2.model.UserDTO
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.ados.mstrotrematch2.util.ZoomOutPageTransformer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPageFanClub.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPageFanClub : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: Int? = 0
    private var param2: String? = null

    private var _binding: FragmentPageFanClubBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    // 뷰모델 연결
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var toast : Toast? = null

    private var fanClubDTO: FanClubDTO? = null
    private var userDTO: UserDTO? = null
    private var preferencesDTO: PreferencesDTO? = null

    private var lastChatDate = Date()
    private var displayCount = 0 // 전광판 일정 시간 유지를 위한 변수
    private var displayChat = DisplayBoardDTO() // 표시할 가장 최근 채팅
    private var chatTimer : Timer? = null
    private var safeStatus = false

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        userDTO = (activity as MainActivity?)?.getUser()!!
        preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPageFanClubBinding.inflate(inflater, container, false)
        val rootView = binding.root.rootView

        binding.layoutTab.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            firebaseViewModel.getFanClub(userDTO?.fanClubId.toString()) {
                fanClubDTO = it

                chatTimer()

                if (userDTO?.fanClubJoinDate == null) {
                    userDTO?.fanClubJoinDate = Date()
                }
                firebaseViewModel.getFanClubChatListen(fanClubDTO?.docName.toString(), userDTO?.fanClubJoinDate!!)

                binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
                binding.viewpager.apply {
                    //adapter = MyPagerAdapterFanClub(context as FragmentActivity, fanClubDTO!!, currentMember!!)
                    if (isAdded) {
                        adapter = MyPagerAdapterFanClub(childFragmentManager, viewLifecycleOwner.lifecycle)
                        setPageTransformer(ZoomOutPageTransformer())
                    }
                }
            }

            firebaseViewModel.fanClubChatDTO.observe(viewLifecycleOwner) {
                displayChat = firebaseViewModel.fanClubChatDTO.value!!
                println("채팅 $displayChat")

                // 마지막 메시지가 안 읽은 메시지라면 new 표시
                val fanClubChatReadTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_FAN_CLUB_CHAT_READ_TIME, 0L)
                val date2 = Date(fanClubChatReadTime)
                println("채팅 마지막 읽은 날짜 ${SimpleDateFormat("yyyy-MM-dd HH:mm").format(date2)}")
                if (fanClubChatReadTime == 0L) {
                    binding.imgChatNew.visibility = View.VISIBLE
                } else {
                    val date = Date(fanClubChatReadTime)
                    if (displayChat.createTime!! > date) {
                        binding.imgChatNew.visibility = View.VISIBLE
                    } else {
                        binding.imgChatNew.visibility = View.GONE
                    }
                }
            }
        }

        binding.textChat.visibility = View.GONE
        binding.imgChatNew.visibility = View.GONE

        return rootView
    }

    override fun onDestroyView() {
        if (safeStatus) {
            chatTimer?.cancel()
            _binding = null
        }
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

    override fun onResume() {
        super.onResume()
        safeStatus = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewpager.post {
            if (param1 != 0) {
                changeTab(param1!!)
            }
        }

        binding.buttonChat.setOnClickListener {
            if (chatTimer != null) {
                chatTimer?.cancel()
            }

            val fragment = FragmentChat.newInstance(binding.viewpager.currentItem, "")
            //val fragment = FragmentChat.newInstance(0, "")
            fragment.fanClubDTO = fanClubDTO
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.textTabInfo.setOnClickListener {
            changeTab(0)
        }
        binding.textTabMember.setOnClickListener {
            changeTab(1)
        }
        binding.textTabRank.setOnClickListener {
            changeTab(2)
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

    // 채팅 타이머
    private fun chatTimer() {
        chatTimer = timer(period = 1000)
        {
            // 새로운 채팅 표시
            if (displayChat.createTime != null && displayChat.createTime!! > lastChatDate) {
                activity?.runOnUiThread {
                    lastChatDate = displayChat.createTime!!
                    displayCount = 0
                    if (displayChat.userNickname.isNullOrEmpty()) {
                        binding.textChat.text = "${displayChat.displayText}"
                    } else {
                        binding.textChat.text = "${displayChat.userNickname} : ${displayChat.displayText}"
                    }
                    openChat()
                }
            } else if (displayCount >= preferencesDTO?.fanClubChatDisplayPeriod!!) { // 표시 시간이 지났다면 채팅창 닫음
                activity?.runOnUiThread {
                    closeChat()
                }
            }
            displayCount++
        }
    }

    private fun openChat() {
        if (binding.textChat.visibility == View.GONE) {
            val translateLeft = AnimationUtils.loadAnimation(context, R.anim.translate_left)
            binding.textChat.startAnimation(translateLeft)
            binding.textChat.visibility = View.VISIBLE
            binding.textChat.isSelected = true
            binding.textChat.requestFocus()
        }
    }

    private fun closeChat() {
        if (binding.textChat.visibility == View.VISIBLE) {
            val translateRight = AnimationUtils.loadAnimation(context, R.anim.translate_right)
            binding.textChat.startAnimation(translateRight)
            binding.textChat.visibility = View.GONE
        }
    }

    private fun setTabButton(textView: TextView) {
        textView.background = AppCompatResources.getDrawable(requireContext(), R.drawable.btn_round)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun releaseTabButton(textView: TextView) {
        textView.background = null
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun releaseAllTabButton() {
        releaseTabButton(binding.textTabInfo)
        releaseTabButton(binding.textTabMember)
        releaseTabButton(binding.textTabRank)
    }

    private fun changeTab(tab: Int) {
        //binding.viewpager.adapter?.notifyItemChanged(tab)
        binding.viewpager.currentItem = tab
        //binding.viewpager.currentItem = tab
        releaseAllTabButton()
        when (tab) {
            0 -> setTabButton(binding.textTabInfo)
            1 -> setTabButton(binding.textTabMember)
            2 -> setTabButton(binding.textTabRank)
        }
    }

    fun getFanClub() : FanClubDTO? {
        return fanClubDTO
    }

    fun quitFanClub() {
        val fragment = FragmentFanClubJoin()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            addToBackStack(null)
            commit()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentPageFanClub.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int, param2: String) =
            FragmentPageFanClub().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}