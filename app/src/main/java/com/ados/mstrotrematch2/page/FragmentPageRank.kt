package com.ados.mstrotrematch2.page

import android.content.Context
import android.os.Bundle
import android.util.TypedValue.COMPLEX_UNIT_DIP
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.adapter.OnRankItemClickListener
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterRank
import com.ados.mstrotrematch2.databinding.FragmentPageRankBinding
import com.ados.mstrotrematch2.dialog.AdminDialog
import com.ados.mstrotrematch2.dialog.DonationStatusDialog
import com.ados.mstrotrematch2.firebase.FirebaseRepository
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPageRank.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPageRank : Fragment(), OnRankItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentPageRankBinding? = null
    private val binding get() = _binding!!

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private lateinit var callback: OnBackPressedCallback

    private var peopleTop3 : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther : ArrayList<RankDTO> = arrayListOf()
    lateinit var recyclerView : RecyclerView

    private var isrefresh = true
    private lateinit var currentDate : String // 12시 지나서 날짜 변경을 체크하기 위한 변수

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
        _binding = FragmentPageRankBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.recyclerview_rank!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        currentDate = SimpleDateFormat("yyyyMMdd").format(Date())

        observePeople()
        refreshPeople()

        // 시즌 변경 작업
        // 김태수(10), 박경래(19) 삭제
        /*
        var index : Int = 1
        var names : ArrayList<String> = arrayListOf(
            "강태관","강화","고재근","구자명","김경민","김수찬","김인석","김재혁","김중연","김태수",
            "김호중","김희재","나무","나태주","남승민","노지훈","류지광","미스터붐박스","박경래","삼식이",
            "신성","신인선","안성훈","양지원","영기","영탁","오샘","옥진욱","유호","이대원",
            "이도진","이재식","이찬원","임도형","임영웅","장민호","정동원","정호","천명훈","최대성",
            "최윤하","최정훈","추혁진","한강","허민영","홍잠언","황윤성","박군","신유","진해성"
        )
        var index2 : Int = 1
        for (name in names){
            var docname = String.format("no%05d",index2)
            var person = RankDTO(String.format("profile%02d",index2), name, 0, docname)
            firestore?.collection("people")?.document(docname)?.set(person)
            index2++
        }*/

        /*
        index2 = 1
        for (name in names){
            var docname = String.format("no%05d",index2)
            var person = RankDTO(String.format("profile%02d",index2), name, 0, docname)
            firestore?.collection("people_cheering")?.document(docname)?.set(person)
            index2++
        }*/

        // 시즌 변경 작업
        /*firestore?.collection("people_back")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                person.count = 0
                firestore?.collection("people")?.document(person.docname.toString())?.set(person)
            }
        }
                ?.addOnFailureListener { exception ->

                }*/


        //recyclerView.adapter = MyRankRecyclerViewAdapter(peopleOther)

        //val firestore = FirebaseFirestore.getInstance()
        // 불타는 트롯맨 가수 추가 (15명)
        /*var names : ArrayList<String> = arrayListOf(
            "강설민","공훈","김정민","민수현","박민수","손태진","에녹","이수호"
            ,"임성현","장동열","전종혁","정다한","최상","춘길","황영웅"
        )
        var index : Int = 101 // 불타는 트롯맨은 101부터 시작
        for (name in names){
            var docname = String.format("no%05d",index)
            var person = RankDTO(String.format("profile%02d",index), name, 0, docname)
            firestore?.collection("people2")?.document(docname)?.set(person)
            index++
        }*/

        // 미스터트롯2 가수 추가 (18명)
        /*var names : ArrayList<String> = arrayListOf(
            "고정우","김용필","나상도","마커스강","박서진","박성온","박세욱","박지현","성리"
            ,"손빈아","송민준","재하","진욱","진해성","천재원","최수호","최우진","황민호"
        )
        var index : Int = 201 // 미스터트롯2는 201부터 시작
        for (name in names){
            var docname = String.format("no%05d",index)
            var person = RankDTO(String.format("profile%03d",index), name, 0, docname)
            firestore?.collection("people2")?.document(docname)?.set(person)
            index++
        }*/



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

    private fun observePeople() {
        firebaseViewModel.peopleDTOs.observe(viewLifecycleOwner) {
            if (firebaseViewModel.peopleDTOs.value != null) {
                peopleTop3 = firebaseViewModel.peopleDTOs.value!!
                peopleOther = firebaseViewModel.peopleDTOs.value!!.clone() as ArrayList<RankDTO>
                peopleOther.subList(0, 3).clear()

                ShowTop3()
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther, this)
                (activity as MainActivity?)?.loadingEnd()
            }
        }
    }

    private fun refreshPeople() {
        (activity as MainActivity?)?.loading()
        firebaseViewModel.getPeople(FirebaseRepository.PeopleOrder.COUNT_DESC)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val seasonDTO = (activity as MainActivity?)?.getSeason()!!
        if (seasonDTO != null) {
            //seasonDTO.seasonNum = 2 // @ 시즌 변경
            var imgBackgroundName = "spotlight_new_s${seasonDTO.seasonNum}_main"
            var imageID = resources.getIdentifier(imgBackgroundName, "drawable", requireContext().packageName)
            if (imageID > 0) {
                Glide.with(binding.imgRankBackground.context)
                    .asBitmap()
                    .load(imageID) ///feed in path of the image
                    .optionalFitCenter()
                    .into(binding.imgRankBackground)
            }

            var imgSeasonLogoName = "new_season${seasonDTO.seasonNum}_logo"
            imageID = resources.getIdentifier(imgSeasonLogoName, "drawable", requireContext().packageName)
            if (imageID > 0) {
                Glide.with(binding.imgSeasonLogo.context)
                    .asBitmap()
                    .load(imageID) ///feed in path of the image
                    .optionalFitCenter()
                    .into(binding.imgSeasonLogo)
            }

            binding.textSeasonEndDate.text = seasonDTO.endDate


            checkDDay()
        }

        dateCheckTimer()

        //adapter 추가
        //recyclerview_rank.adapter = MyRankRecyclerViewAdapter()
        //레이아웃 매니저 추가
        //recyclerview_rank.layoutManager = LinearLayoutManager(view)
        //recyclerview_rank.layoutManager = RelativeLayoutManager(this)

        //binding.profileRankNo1.imgRank.setImageResource(R.drawable.crown_gold)
        //binding.profileRankNo2.imgRank.setImageResource(R.drawable.crown_silver)
        //binding.profileRankNo3.imgRank.setImageResource(R.drawable.crown_bronze)
        Glide.with(binding.profileRankNo1.imgRank.context)
            .asBitmap()
            .load(R.drawable.crown_gold) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo1.imgRank)
        Glide.with(binding.profileRankNo2.imgRank.context)
            .asBitmap()
            .load(R.drawable.crown_silver) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo2.imgRank)
        Glide.with(binding.profileRankNo3.imgRank.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo3.imgRank)

        Glide.with(binding.imgHallOfFame.context)
            .asBitmap()
            .load(R.drawable.hall_of_fame_button) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgHallOfFame)

        /*var imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)
        if (imageID != null) {
            binding.profileRankNo1.imgProfile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)
        if (imageID != null) {
            binding.profileRankNo2.imgProfile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)
        if (imageID != null) {
            binding.profileRankNo3.imgProfile.setImageResource(imageID)
        }*/

        binding.profileRankNo1.root.visibility = View.GONE
        binding.profileRankNo2.root.visibility = View.GONE
        binding.profileRankNo3.root.visibility = View.GONE

        binding.profileRankNo1.textRank.text = "현재  1위"
        binding.profileRankNo1.textRank.setTextSize(COMPLEX_UNIT_DIP, 15.5f)
        binding.profileRankNo1.textName.setTextSize(COMPLEX_UNIT_DIP, 19.0f)
        binding.profileRankNo1.textCount.setTextSize(COMPLEX_UNIT_DIP, 16.0f)
        Glide.with(binding.profileRankNo1.imgLineTop.context)
            .asBitmap()
            .load(R.drawable.gold_line_top) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo1.imgLineTop)

        binding.profileRankNo2.textRank.text = "현재  2위"
        binding.profileRankNo2.textRank.setTextSize(COMPLEX_UNIT_DIP, 14.0f)
        binding.profileRankNo2.textName.setTextSize(COMPLEX_UNIT_DIP, 17.0f)
        binding.profileRankNo2.textCount.setTextSize(COMPLEX_UNIT_DIP, 14.0f)
        Glide.with(binding.profileRankNo2.imgLineTop.context)
            .asBitmap()
            .load(R.drawable.red_line_top) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo2.imgLineTop)

        binding.profileRankNo3.textRank.text = "현재  3위"
        binding.profileRankNo3.textRank.setTextSize(COMPLEX_UNIT_DIP, 13.0f)
        binding.profileRankNo3.textName.setTextSize(COMPLEX_UNIT_DIP, 16.0f)
        binding.profileRankNo3.textCount.setTextSize(COMPLEX_UNIT_DIP, 13.0f)
        Glide.with(binding.profileRankNo3.imgLineTop.context)
            .asBitmap()
            .load(R.drawable.blue_line_top) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo3.imgLineTop)

        binding.profileRankNo1.root.setOnClickListener {
            callDonationStatusDialog(peopleTop3[0], 1)
        }
        binding.profileRankNo2.root.setOnClickListener {
            callDonationStatusDialog(peopleTop3[1], 2)
        }
        binding.profileRankNo3.root.setOnClickListener {
            callDonationStatusDialog(peopleTop3[2], 3)
        }

        binding.buttonAdmin.visibility = View.GONE // 관리자모드
        binding.buttonAdmin.setOnClickListener {
            val dialog = AdminDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.binding.buttonOk.setOnClickListener { // OK
                dialog.dismiss()
            }
        }

        binding.imgHallOfFame.setOnClickListener {
            (activity as MainActivity?)!!.callHallOfFameActivity()
        }

        binding.buttonRefresh.setOnClickListener {
            if (!isrefresh) {
                Toast.makeText(requireActivity(),"새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
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

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (!isrefresh) {
                Toast.makeText(requireActivity(),"새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
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
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun ShowTop3() {
        var imageID : Int = 0
        /*var imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)
        if (imageID != null) {
            binding.profileRankNo1.imgProfile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)
        if (imageID != null) {
            binding.profileRankNo2.imgProfile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)
        if (imageID != null) {
            binding.profileRankNo3.imgProfile.setImageResource(imageID)
        }*/

        if (peopleTop3.size > 0 && binding.profileRankNo1 != null) {
            binding.profileRankNo1.root.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)!!
            //binding.profileRankNo1.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo1.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo1.imgProfile)
            binding.profileRankNo1.textName.text = peopleTop3[0].name
            //binding.profileRankNo1.textCount.text = "${formatNumber(peopleTop3[0].count!!)}표"
            binding.profileRankNo1.textCount.text = "${decimalFormat.format(peopleTop3[0].count!!)}"

            // 시즌 변경 작업
            //binding.profileRankNo1.imgProfile.visibility = View.GONE
            //binding.profileRankNo1.textName.visibility = View.GONE
            //binding.profileRankNo1.textCount.visibility = View.GONE
        }
        if (peopleTop3.size > 1 && binding.profileRankNo2 != null) {
            binding.profileRankNo2.root.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)!!
            //binding.profileRankNo2.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo2.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo2.imgProfile)
            binding.profileRankNo2.textName.text = peopleTop3[1].name
            //binding.profileRankNo2.textCount.text = "${formatNumber(peopleTop3[1].count!!)}표"
            binding.profileRankNo2.textCount.text = "${decimalFormat.format(peopleTop3[1].count!!)}"

            // 시즌 변경 작업
            //binding.profileRankNo2.imgProfile.visibility = View.GONE
            //binding.profileRankNo2.textName.visibility = View.GONE
            //binding.profileRankNo2.textCount.visibility = View.GONE
        }
        if (peopleTop3.size > 2 && binding.profileRankNo3 != null) {
            binding.profileRankNo3.root.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)!!
            //binding.profileRankNo3.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo3.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo3.imgProfile)
            binding.profileRankNo3.textName.text = peopleTop3[2].name
            //binding.profileRankNo3.textCount.text = "${formatNumber(peopleTop3[2].count!!)}표"
            binding.profileRankNo3.textCount.text = "${decimalFormat.format(peopleTop3[2].count!!)}"

            // 시즌 변경 작업
            //binding.profileRankNo3.imgProfile.visibility = View.GONE
            //binding.profileRankNo3.textName.visibility = View.GONE
            //binding.profileRankNo3.textCount.visibility = View.GONE
        }
    }

    override fun onItemClick(item: RankDTO, position: Int) {
        callDonationStatusDialog(item, position.plus(4))
    }

    private fun callDonationStatusDialog(item: RankDTO, rank: Int) {
        val userDTO = (activity as MainActivity?)?.getUser()!!
        val seasonDTO = (activity as MainActivity?)?.getSeason()!!
        firebaseViewModel.getVoteCount(userDTO.uid.toString(), seasonDTO.seasonNum!!, item.docname.toString()) { voteCount ->
            val dialog = DonationStatusDialog(requireContext(), item, rank, voteCount)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.binding.buttonOk.setOnClickListener { // OK
                dialog.dismiss()
            }
        }
    }

    private fun formatNumber(value: Int): String {
        return when {
            value >= 1E3 -> "${decimalFormat.format((value.toFloat() / 1E3).toInt())}K"
            else -> NumberFormat.getInstance().format(value)
        }
    }

    // 날짜 변경 체크 타이머
    private fun dateCheckTimer() {
        timer(period = 1000)
        {
            val checkDate = SimpleDateFormat("yyyyMMdd").format(Date())
            if (currentDate != checkDate) {
                requireActivity().runOnUiThread {
                    println("날짜가 변경되었습니다 $currentDate -> $checkDate")
                    currentDate = checkDate

                    checkDDay()
                }
            }
        }
    }

    private fun checkDDay() {
        val seasonDTO = (activity as MainActivity?)?.getSeason()!!
        val dDay = seasonDTO.getDDay()
        if (dDay <= 10) {
            binding.textDDay.visibility = View.VISIBLE
            binding.textDDay.text = if (dDay > 0) "D-$dDay" else "D-DAY"
        } else {
            binding.textDDay.visibility = View.GONE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentPageRank.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentPageRank().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}