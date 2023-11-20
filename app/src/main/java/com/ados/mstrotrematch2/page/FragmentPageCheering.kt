package com.ados.mstrotrematch2.page


import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.*
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.adapter.OnCheeringItemClickListener
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterCheering
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterCheeringTotal
import com.ados.mstrotrematch2.database.DBHelperReport
import com.ados.mstrotrematch2.database.DBHelperCheeringBoard
import com.ados.mstrotrematch2.databinding.FragmentPageCheeringBinding
import com.ados.mstrotrematch2.dialog.*
import com.ados.mstrotrematch2.firebase.FirebaseRepository
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.*
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.bumptech.glide.Glide
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class FragmentPageCheering : Fragment(), OnCheeringItemClickListener {
    private var _binding: FragmentPageCheeringBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var toast : Toast? = null

    private val decimalFormat: DecimalFormat = DecimalFormat("###,###")
    private var dbHandler : DBHelperCheeringBoard? = null
    private lateinit var dbHandlerReport : DBHelperReport
    lateinit var recyclerView : RecyclerView
    private var isScrolling = false
    private var isViewType = FirebaseRepository.CheeringBoardType.POPULAR
    private var isRefresh = true
    private var isInitPopularData = true // 해당 변수가 false 면 데이터 획득만 갱신 x
    private var isInitNewData = false // 최초 실행 시에는 인기순 통계가 출력 되도록 함
    private var isInitTotalData = false // 최초 실행 시에는 인기순 통계가 출력 되도록 함
    private var insertRefresh = false
    private var isSwitchButtonMsg = false

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPageCheeringBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.recyclerview_cheering!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        dbHandler = DBHelperCheeringBoard(requireContext())
        dbHandlerReport = DBHelperReport(requireContext())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userDTO = (activity as MainActivity?)?.getUser()!!

        binding.switchFavorite.isChecked = sharedPreferences.getBoolean(MySharedPreferences.PREF_KEY_CHEERING_SHOW_FAVORITE, false)

        val seasonDTO = (activity as MainActivity?)?.getSeason()!!
        if (seasonDTO != null) {
            //seasonDTO.seasonNum = 2 // @ 시즌 변경
            var imgBackgroundName = "spotlight_new_s${seasonDTO.seasonNum}_cheering"
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
        }
        binding.textTop3.text = "응원하기 ${seasonDTO.getWeek()}주차"

        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
        val writeCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_WRITE_CHEERING, 0)
        var writeCountMax = if (userDTO.isPremium()) {
            preferencesDTO.writeCount!!.times(2)
        } else {
            preferencesDTO.writeCount!!
        }

        if (writeCount >= writeCountMax) { // 오늘 작성 가능한 글을 모두 다 작성했다면
            binding.imgWriteNew.visibility = View.GONE
        } else { // 아직 작성 횟수가 남았다면
            binding.imgWriteNew.visibility = View.VISIBLE
        }

        observeCheeringBoard()
        isViewType = FirebaseRepository.CheeringBoardType.NEW
        refreshData()

        isViewType = FirebaseRepository.CheeringBoardType.POPULAR
        refreshData()

        refreshPeople()

        binding.layoutTop1.root.visibility = View.GONE
        binding.layoutTop2.root.visibility = View.GONE
        binding.layoutTop3.root.visibility = View.GONE
        //binding.layoutTop1.imgCrown.setImageResource(R.drawable.crown_gold)
        //binding.layoutTop2.imgCrown.setImageResource(R.drawable.crown_silver)
        //binding.layoutTop3.imgCrown.setImageResource(R.drawable.crown_bronze)
        Glide.with(binding.layoutTop1.imgCrown.context)
            .asBitmap()
            .load(R.drawable.crown_gold) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.layoutTop1.imgCrown)
        Glide.with(binding.layoutTop2.imgCrown.context)
            .asBitmap()
            .load(R.drawable.crown_silver) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.layoutTop2.imgCrown)
        Glide.with(binding.layoutTop3.imgCrown.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.layoutTop3.imgCrown)

        Glide.with(binding.imgQuestion.context)
            .asBitmap()
            .load(R.drawable.question) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgQuestion)

        Glide.with(binding.layoutTop3.imgCrown.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.layoutTop3.imgCrown)

        (activity as MainActivity?)?.loading()
        /*timer(period = 100)
        {
            if (cheeringBoardCollectionName.isNotEmpty() && peopleCollectionName.isNotEmpty()) {
                cancel()
                requireActivity().runOnUiThread {
                    var pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
                    val keyString = "WriteCheering${SimpleDateFormat("yyyyMMdd").format(Date())}"
                    val writeCount = pref.getInt(keyString, 0)
                    binding.textCount.text = "남은횟수 : ${preferencesDTO?.writeCount!!.minus(writeCount)}"

                    isViewType = FirebaseRepository.CheeringBoardType.NEW
                    refreshData(postsNew)

                    isViewType = FirebaseRepository.CheeringBoardType.POPULAR
                    refreshData(postsPopular)

                    refreshPeople()
                }
            }
        }*/
        /*timer(period = 100)
        {
            if (postsPopular.size > 0) {
                cancel()
                requireActivity().runOnUiThread {
                    showPopular()
                    (activity as MainActivity?)?.loadingEnd()
                }
            }
        }*/
        //refreshStatistics()

        binding.buttonWrite.setOnClickListener {
            //Toast.makeText(context,"불량 사용자로 차단되어 응원글 작성을 할 수 없습니다.")
            /*var nowDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
            var pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
            var writeDate = pref.getString("WriteCheering", "")

            if (nowDate == writeDate) {
                callToast("글쓰기는 하루에 한번만 할 수 있습니다.")
            }*/
            val userDTO = (activity as MainActivity?)?.getUser()!!
            val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
            val writeCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_WRITE_CHEERING, 0)
            val writeCountMax = if (userDTO.isPremium()) {
                preferencesDTO.writeCount!!.times(2)
            } else {
                preferencesDTO.writeCount!!
            }

            if (writeCount >= writeCountMax) {
                callToast("오늘은 작성가능한 응원글을 모두 작성하였습니다. (${writeCountMax}회)")
            } else {
                val cheerBoard = sharedPreferences.getString(MySharedPreferences.PREF_KEY_CHEER_BOARD, "")
                if (cheerBoard.isNullOrEmpty()) {
                    var question = QuestionDTO(QuestionDTO.Stat.INFO, "응원글 이용약관", "")
                    question.content = """
응원글 중 아래에 해당되는 경우 고지없이 응원글이 삭제되거나 글쓰기에 제한을 받을 수 있습니다.

■ 동일인이라고 인정되는 자가 동일 또는 유사내용을 반복하여 게재하는 도배성 글
■ 특정인을 비방하거나 명예훼손의 우려가 있는 경우
■ 해당 게시판의 취지와 부합하지 않을 경우
                """
                    val questionDialog = QuestionDialog(requireContext(), question)
                    questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    questionDialog.setCanceledOnTouchOutside(false)
                    questionDialog.show()
                    questionDialog.setButtonOk("동의")
                    questionDialog.setButtonCancel("거절")

                    questionDialog.binding.buttonQuestionCancel.setOnClickListener { // No
                        questionDialog.dismiss()
                        callToast("약관에 동의하여야 글쓰기가 가능합니다.")
                    }

                    questionDialog.binding.buttonQuestionOk.setOnClickListener { // Ok
                        questionDialog.dismiss()

                        // 약관 동의 기록
                        var nowDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
                        sharedPreferences.putString(MySharedPreferences.PREF_KEY_CHEER_BOARD, nowDate)

                        callToast("$nowDate 동의 완료 되었습니다.")

                        showBoardWriteDialog()
                    }
                } else {
                    showBoardWriteDialog()
                }
            }
        }

        binding.buttonRefresh.setOnClickListener {
            if (!isRefresh) {
                callToast("새로고침은 5초에 한 번 가능합니다.")
            } else {
                isRefresh = false

                var second = 1;
                timer(period = 1000)
                {
                    if (second > 5) {
                        cancel()
                        isRefresh = true
                    }
                    second++
                }

                firebaseViewModel.lastVisibleRemove()
                if (isViewType == FirebaseRepository.CheeringBoardType.STATISTICS) {
                    refreshPeople()
                } else {
                    refreshData()
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (!isRefresh) {
                callToast("새로고침은 5초에 한 번 가능합니다.")
                binding.swipeRefreshLayout.isRefreshing = false
            } else {
                isRefresh = false

                var second = 1;
                timer(period = 1000)
                {
                    if (second > 5) {
                        cancel()
                        isRefresh = true
                    }
                    second++
                }

                val seasonDTO = (activity as MainActivity?)?.getSeason()!!
                binding.textTop3.text = "응원하기 ${seasonDTO.getWeek()}주차"

                when (isViewType) {
                    FirebaseRepository.CheeringBoardType.POPULAR -> {
                        firebaseViewModel.lastVisiblePopularRemove()
                        refreshData()
                    }
                    FirebaseRepository.CheeringBoardType.NEW -> {
                        firebaseViewModel.lastVisibleNewRemove()
                        refreshData()
                    }
                    FirebaseRepository.CheeringBoardType.STATISTICS -> {

                    }
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        binding.textPopular.setOnClickListener {
            if (isViewType != FirebaseRepository.CheeringBoardType.POPULAR) {
                showPopular()
            }
        }

        binding.textNew.setOnClickListener {
            if (isViewType != FirebaseRepository.CheeringBoardType.NEW) {
                showNew()
            }
        }

        binding.textStatistics.setOnClickListener {
            if (isViewType != FirebaseRepository.CheeringBoardType.STATISTICS) {
                showStatistics()
            }
        }

        binding.imgQuestion.setOnClickListener {
            var question = QuestionDTO(QuestionDTO.Stat.INFO, "응원점수 집계 및 계산", "")
            question.content = """
■ 응원점수 집계는 하루 한 번
    새벽 0시~2시 사이에 됩니다.
■ 점수 계산
    - 응원글 작성 : 50점, 좋아요 : 1점
    - 두 점수를 합한 점수가 합계 점수
                """
            val dialog = CheeringInfoDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.setButtonCancel("닫기")
            dialog.showButtonOk(false)

            dialog.binding.buttonQuestionCancel.setOnClickListener { // No
                dialog.dismiss()
            }
        }

        binding.buttonData.visibility = View.GONE // 관리자모드
        binding.buttonData.setOnClickListener {
            refreshStatistics()
        }

        binding.switchFavorite.setOnCheckedChangeListener { buttonView, isChecked ->
            val userDTO = (activity as MainActivity?)?.getUser()!!
            if (userDTO.favorites.size <= 0 && isChecked) {
                sharedPreferences.putBoolean(MySharedPreferences.PREF_KEY_CHEERING_SHOW_FAVORITE, false)
                callToast("지정한 최애가 없습니다. 최애는 투표하기에서 설정 가능합니다.")
                binding.switchFavorite.isChecked = false
            } else {
                isSwitchButtonMsg = false

                sharedPreferences.putBoolean(MySharedPreferences.PREF_KEY_CHEERING_SHOW_FAVORITE, isChecked)

                firebaseViewModel.lastVisiblePopularRemove()
                firebaseViewModel.lastVisibleNewRemove()

                insertRefresh = false

                when (isViewType) {
                    FirebaseRepository.CheeringBoardType.POPULAR -> { // 인기순
                        // 최신순 데이터는 획득만
                        isInitNewData = false
                        isViewType = FirebaseRepository.CheeringBoardType.NEW
                        refreshData()

                        isViewType = FirebaseRepository.CheeringBoardType.POPULAR
                        refreshData()
                    }
                    FirebaseRepository.CheeringBoardType.NEW -> { // 최신순
                        // 인기순 데이터는 획득만
                        isInitPopularData = false
                        isViewType = FirebaseRepository.CheeringBoardType.POPULAR
                        refreshData()

                        isViewType = FirebaseRepository.CheeringBoardType.NEW
                        refreshData()
                    }
                    else -> { // 통계
                        // 인기순, 최신순 데이터 획득만
                        isInitNewData = false
                        isViewType = FirebaseRepository.CheeringBoardType.NEW
                        refreshData()

                        isInitPopularData = false
                        isViewType = FirebaseRepository.CheeringBoardType.POPULAR
                        refreshData()
                    }
                }
            }


            //refreshData()

            /*if (!isSwitchButtonMsg) { // 변수를 사용하지 않으면 취소를 눌렀을 때 무한 반복 됨
                var question = if (isChecked) {
                    QuestionDTO(QuestionDTO.Stat.INFO, "최애가수만 보기", "최애가수만 표시하시겠습니까?")
                } else {
                    QuestionDTO(QuestionDTO.Stat.INFO, "전체가수 보기", "전체가수 모두 표시하시겠습니까?")
                }
                val dialog = QuestionDialog(requireContext(), question)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                dialog.binding.buttonQuestionCancel.setOnClickListener { // No
                    dialog.dismiss()
                    binding.switchFavorite.isChecked = !binding.switchFavorite.isChecked
                }
                dialog.binding.buttonQuestionOk.setOnClickListener { // Ok
                    dialog.dismiss()
                    isSwitchButtonMsg = false

                    sharedPreferences.putBoolean(MySharedPreferences.PREF_KEY_CHEERING_SHOW_FAVORITE, isChecked)

                    firebaseViewModel.lastVisiblePopularRemove()
                    firebaseViewModel.lastVisibleNewRemove()

                    insertRefresh = false
                    refreshData()
                }
            }*/
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //val totalItemCount = recyclerView.layoutManager!!.itemCount
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }
            override fun onScrolled(recyclerView1: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView1, dx, dy)

                // 마지막까지 스크롤 했으면 다음 데이터 조회
                if (!recyclerView.canScrollVertically(1)) {
                    isScrolling = false
                    insertRefresh = true
                    if ((isViewType == FirebaseRepository.CheeringBoardType.POPULAR && firebaseViewModel.boardDTOsPopular.value!!.size >= 30)
                        || (isViewType == FirebaseRepository.CheeringBoardType.NEW && firebaseViewModel.boardDTOsNew.value!!.size >= 30)) {
                        refreshData()
                    }
                }
            }
        })

        // 프래그먼트 간 통신
        // 신고 처리한 항목을 갱신
        setFragmentResultListener("notifyItemChanged") { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val position = bundle.getInt("position")
            // Do something with the result
            recyclerView.adapter?.notifyItemChanged(position)
        }

        setFragmentResultListener("notifyItemRemoved") { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val position = bundle.getInt("position")
            // Do something with the result
            firebaseViewModel.boardDTOsPopular.value!!.removeAt(position)
            recyclerView.adapter?.notifyItemRemoved(position)
        }

    }

    private fun showBoardWriteDialog() {
        val fragment = FragmentBoardWrite()
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            addToBackStack(null)
            commit()
        }
        return

        /*val dialog = BoardWriteDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.preferencesDTO = preferencesDTO
        dialog.mainActivity = (activity as MainActivity?)
        dialog.show()
        dialog.binding.buttonCancel.setOnClickListener { // No
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            if (dialog.isWrite) {
                // 투표권 1장 추가
                var pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
                var ticketcount = pref.getInt("TicketCount", preferencesDTO?.ticketChargeCount!!)

                var editor = pref.edit()
                editor.putInt("TicketCount", ticketcount + preferencesDTO?.cheeringTicketCount!!).apply()

                callToast("응원하기로 티켓이 ${preferencesDTO?.cheeringTicketCount}장 추가되었습니다.")

                val keyString = "WriteCheering${SimpleDateFormat("yyyyMMdd").format(Date())}"
                val writeCount = pref.getInt(keyString, 0)
                binding.textCount.text = "남은횟수 : ${preferencesDTO?.writeCount!!.minus(writeCount)}"

                showAd()
            }
        }*/
    }

    private fun observeCheeringBoard() {
        // 인기순 데이터
        firebaseViewModel.boardDTOsPopular.observe(viewLifecycleOwner) {
            if (firebaseViewModel.boardDTOsPopular.value != null) {
                if (isInitPopularData) { // 해당 변수가 false 면 데이터 획득만 갱신 x
                    setAdapter(firebaseViewModel.boardDTOsPopular.value!!, true)
                } else {
                    isInitPopularData = true
                }
            }
            (activity as MainActivity?)?.loadingEnd()
        }

        // 최신순 데이터
        firebaseViewModel.boardDTOsNew.observe(viewLifecycleOwner) {
            if (firebaseViewModel.boardDTOsNew.value != null) {
                if (isInitNewData) { // 최초 실행 시에는 인기순 통계가 출력 되도록 함
                    setAdapter(firebaseViewModel.boardDTOsNew.value!!, true)
                } else {
                    isInitNewData = true
                }
            }
            (activity as MainActivity?)?.loadingEnd()
        }

        // 통계 데이터
        firebaseViewModel.rankDTOsStatistics.observe(viewLifecycleOwner) {
            if (firebaseViewModel.rankDTOsStatistics.value != null) {
                for ((index, person) in firebaseViewModel.rankDTOsStatistics.value!!.withIndex()) {
                    when (index) {
                        0 -> {
                            binding.layoutTop1.root.visibility = View.VISIBLE
                            binding.layoutTop1.textName.text = person.name
                            binding.layoutTop1.textCount.text = "총점:${decimalFormat.format(person.cheeringCountTotal)}"
                        }
                        1 -> {
                            binding.layoutTop2.root.visibility = View.VISIBLE
                            binding.layoutTop2.textName.text = person.name
                            binding.layoutTop2.textCount.text = "총점:${decimalFormat.format(person.cheeringCountTotal)}"
                        }
                        2 -> {
                            binding.layoutTop3.root.visibility = View.VISIBLE
                            binding.layoutTop3.textName.text = person.name
                            binding.layoutTop3.textCount.text = "총점:${decimalFormat.format(person.cheeringCountTotal)}"
                        }
                    }
                }
                if (isInitTotalData) { // 최초 실행 시에는 인기순 통계가 출력 되도록 함
                    recyclerView.adapter = RecyclerViewAdapterCheeringTotal(firebaseViewModel.rankDTOsStatistics.value!!)
                } else {
                    isInitTotalData = true
                }
            }
            (activity as MainActivity?)?.loadingEnd()
        }
    }

    private fun setAdapter(boards: ArrayList<BoardDTO>, isInsert: Boolean) {
        if (insertRefresh) {
            recyclerView.adapter?.notifyItemInserted(boards.size)
        } else {
            recyclerView.adapter = RecyclerViewAdapterCheering(boards, this)
        }
        insertRefresh = false

        /*if (boards.size > 30) {
            recyclerView.adapter?.notifyItemInserted(boards.size)
        } else {
            recyclerView.adapter = RecyclerViewAdapterCheering(boards, this)
        }*/
    }

    private fun refreshData() {
        (activity as MainActivity?)?.loading()
        val seasonDTO = (activity as MainActivity?)?.getSeason()!!

        val list = if (binding.switchFavorite.isChecked) (activity as MainActivity?)?.getUser()!!.favorites
        else null

        when (isViewType) {
            FirebaseRepository.CheeringBoardType.POPULAR -> {
                firebaseViewModel.getCheeringBoardPopular(dbHandlerReport, seasonDTO.seasonNum!!, seasonDTO.getWeek(), list)
            }
            FirebaseRepository.CheeringBoardType.NEW -> {
                firebaseViewModel.getCheeringBoardNew(dbHandlerReport, seasonDTO.seasonNum!!, seasonDTO.getWeek(), list)
            }
            else -> {
                (activity as MainActivity?)?.loadingEnd()
            }
        }
    }

    private fun showPopular() {
        isViewType = FirebaseRepository.CheeringBoardType.POPULAR
        //firebaseViewModel.lastVisibleRemove()

        setTextColor()

        setAdapter(firebaseViewModel.boardDTOsPopular.value!!, false)
    }

    private fun showNew() {
        isViewType = FirebaseRepository.CheeringBoardType.NEW
        //firebaseViewModel.lastVisibleRemove()

        setTextColor()

        setAdapter(firebaseViewModel.boardDTOsNew.value!!, false)
    }

    private fun showStatistics() {
        isViewType = FirebaseRepository.CheeringBoardType.STATISTICS
        //firebaseViewModel.lastVisibleRemove()

        setTextColor()

        recyclerView.adapter = RecyclerViewAdapterCheeringTotal(firebaseViewModel.rankDTOsStatistics.value!!)
    }

    private fun setTextColor() {
        var popularColor = ContextCompat.getColor(requireContext(), R.color.unselect_text)
        var popularFlag = Paint.ANTI_ALIAS_FLAG
        var newColor = ContextCompat.getColor(requireContext(), R.color.unselect_text)
        var newFlag = Paint.ANTI_ALIAS_FLAG
        var statisticsColor = ContextCompat.getColor(requireContext(), R.color.unselect_text)
        var statisticsFlag = Paint.ANTI_ALIAS_FLAG
        when (isViewType) {
            FirebaseRepository.CheeringBoardType.POPULAR -> {
                popularColor = ContextCompat.getColor(requireContext(), R.color.select_text)
                popularFlag = binding.textPopular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG
            }
            FirebaseRepository.CheeringBoardType.NEW -> {
                newColor = ContextCompat.getColor(requireContext(), R.color.select_text)
                newFlag = binding.textPopular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG
            }
            FirebaseRepository.CheeringBoardType.STATISTICS -> {
                statisticsColor = ContextCompat.getColor(requireContext(), R.color.select_text)
                statisticsFlag = binding.textPopular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG
            }
        }

        binding.textPopular.setTextColor(popularColor)
        binding.textPopular.paintFlags = popularFlag

        binding.textNew.setTextColor(newColor)
        binding.textNew.paintFlags = newFlag

        binding.textStatistics.setTextColor(statisticsColor)
        binding.textStatistics.paintFlags = statisticsFlag
    }

    override fun onItemClick(item: BoardDTO, position: Int) {
        if (isViewType != FirebaseRepository.CheeringBoardType.STATISTICS) {
            val fragment = FragmentBoard()
            fragment.item = item
            fragment.itemPosition = position
            parentFragmentManager.beginTransaction().apply {
                add(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }
    }

    override fun onItemClickLike(item: BoardDTO, like: TextView) {
        if (dbHandler?.getLike(item.docname.toString()) == false) {
            dbHandler?.updateLike(item.docname.toString(), 1)

            item.likeCount = item.likeCount!! + 1
            like.text = "${item.likeCount}"
            like.paintFlags = like.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            callToast("좋아요")
        } else {
            dbHandler?.updateLike(item.docname.toString(), 0)

            item.likeCount = item.likeCount!! - 1
            like.text = "${item.likeCount}"
            like.paintFlags = Paint.ANTI_ALIAS_FLAG

            callToast("좋아요 취소")
        }
    }

    private fun refreshPeople() {
        (activity as MainActivity?)?.loading()
        firebaseViewModel.getCheeringStatistics()
    }

    private fun refreshStatistics() {
        callToast("데이터 취합 시작")
        (activity as MainActivity?)?.loading()
        val seasonDTO = (activity as MainActivity?)?.getSeason()!!
        firebaseViewModel.updateCheeringStatistics(seasonDTO.seasonNum!!, seasonDTO.getWeek()) {
            callToast("데이터 취합 완료")
            (activity as MainActivity?)?.loadingEnd()
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
}
