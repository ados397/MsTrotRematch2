package com.ados.mstrotrematch2.page


import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.*
import com.ados.mstrotrematch2.dialog.BoardDialog
import com.ados.mstrotrematch2.dialog.BoardWriteDialog
import com.ados.mstrotrematch2.dialog.LoadingDialog
import com.ados.mstrotrematch2.dialog.QuestionDialog
import com.ados.mstrotrematch2.model.*
import com.ados.mstrotrematch2.R
import com.bumptech.glide.Glide
import com.fsn.cauly.CaulyAdInfo
import com.fsn.cauly.CaulyAdInfoBuilder
import com.fsn.cauly.CaulyInterstitialAd
import com.fsn.cauly.CaulyInterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.board_dialog.button_cancel
import kotlinx.android.synthetic.main.cheering_top_item.view.*
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.*
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.button_refresh
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.img_season_logo
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.swipe_refresh_layout
import kotlinx.android.synthetic.main.question_dialog.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class FragmentPageCheering : Fragment(), OnCheeringItemClickListener {
    enum class ViewType {
        POPULAR, NEW, STATISTICS
    }

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var dbHandler : DatabaseHelper? = null
    var firestore : FirebaseFirestore? = null
    lateinit var recyclerView : RecyclerView
    private var posts_popular : ArrayList<BoardDTO> = arrayListOf()
    private var posts_new : ArrayList<BoardDTO> = arrayListOf()
    private var people : ArrayList<RankDTO> = arrayListOf()
    private var statistics : ArrayList<BoardDTO> = arrayListOf()
    var pageIndex : Int? = 0
    var lastVisible : DocumentSnapshot? = null
    var isScrolling = false
    var isViewType = ViewType.POPULAR
    private var isrefresh = true
    var cheeringboardCollectionName = ""
    var peopleCollectionName = ""
    var preferencesDTO : PreferencesDTO? = null

    var loadingDialog : LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_fragment_page_cheering, container, false)

        var rootView = inflater.inflate(R.layout.fragment_fragment_page_cheering, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview_cheering!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        dbHandler = DatabaseHelper(getActivity()!!)
        firestore = FirebaseFirestore.getInstance()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore?.collection("preferences")?.document("season")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var seasonDTO = documentSnapshot?.toObject(SeasonDTO::class.java)
            peopleCollectionName = "people_cheering"

            // 시즌 변경 작업
            //seasonDTO?.seasonNum = 5
            var rank_background_img = R.drawable.spotlight_s6_cheering
            var season_logo_img = R.drawable.season6_logo
            if (seasonDTO?.seasonNum == 5) {
                cheeringboardCollectionName = "cheeringboard_s5"
                rank_background_img = R.drawable.spotlight_s5_cheering
                season_logo_img = R.drawable.season5_logo
            } else {
                cheeringboardCollectionName = "cheeringboard_s6"
            }
            Glide.with(img_rank_background.context)
                .asBitmap()
                .load(rank_background_img) ///feed in path of the image
                .fitCenter()
                .into(img_rank_background)
            Glide.with(img_season_logo.context)
                .asBitmap()
                .load(season_logo_img) ///feed in path of the image
                .fitCenter()
                .into(img_season_logo)
        }

        firestore?.collection("preferences")?.document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            preferencesDTO = documentSnapshot?.toObject(PreferencesDTO::class.java)
        }

        layout_top1.visibility = View.GONE
        layout_top2.visibility = View.GONE
        layout_top3.visibility = View.GONE
        //layout_top1.img_crown.setImageResource(R.drawable.crown_gold)
        //layout_top2.img_crown.setImageResource(R.drawable.crown_silver)
        //layout_top3.img_crown.setImageResource(R.drawable.crown_bronze)
        Glide.with(layout_top1.img_crown.context)
            .asBitmap()
            .load(R.drawable.crown_gold) ///feed in path of the image
            .fitCenter()
            .into(layout_top1.img_crown)
        Glide.with(layout_top2.img_crown.context)
            .asBitmap()
            .load(R.drawable.crown_silver) ///feed in path of the image
            .fitCenter()
            .into(layout_top2.img_crown)
        Glide.with(layout_top3.img_crown.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .fitCenter()
            .into(layout_top3.img_crown)

        Glide.with(img_question.context)
            .asBitmap()
            .load(R.drawable.question) ///feed in path of the image
            .fitCenter()
            .into(img_question)

        Glide.with(layout_top3.img_crown.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .fitCenter()
            .into(layout_top3.img_crown)

        number_picker.minValue = 0
        number_picker.maxValue = 47
        number_picker.wrapSelectorWheel = false

        loading()
        timer(period = 100)
        {
            if (cheeringboardCollectionName.isNotEmpty() && peopleCollectionName.isNotEmpty()) {
                cancel()
                getActivity()!!.runOnUiThread {
                    isViewType = ViewType.NEW
                    refreshData(posts_new)

                    isViewType = ViewType.POPULAR
                    refreshData(posts_popular)

                    refreshPeople()
                }
            }
        }
        timer(period = 100)
        {
            if (posts_popular.size > 0) {
                cancel()
                getActivity()!!.runOnUiThread {
                    showPopular()
                    loadingEnd()
                }
            }
        }
        //refreshStatistics()

        button_write.setOnClickListener {
            //Toast.makeText(context,"불량 사용자로 차단되어 응원글 작성을 할 수 없습니다.", Toast.LENGTH_SHORT).show()
            var nowDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
            var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
            var writeDate = pref.getString("WriteCheering", "")

            if (nowDate == writeDate) {
                Toast.makeText(getActivity(),"글쓰기는 하루에 한번만 할 수 있습니다.", Toast.LENGTH_SHORT).show()
            } else {
                var cheerBoard = pref.getString("CheerBoard", "")
                if (cheerBoard.isNullOrEmpty()) {
                    var question = QuestionDTO(QuestionDTO.STAT.INFO, "응원글 이용약관", "")
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

                    questionDialog.button_question_cancel.setOnClickListener { // No
                        questionDialog.dismiss()
                        Toast.makeText(getActivity(),"약관에 동의하여야 글쓰기가 가능합니다.", Toast.LENGTH_SHORT).show()
                    }

                    questionDialog.button_question_ok.setOnClickListener { // Ok
                        questionDialog.dismiss()

                        // 약관 동의 기록
                        var editor = pref.edit()
                        editor.putString("CheerBoard", nowDate).apply()

                        Toast.makeText(getActivity(),"$nowDate 동의 완료 되었습니다.", Toast.LENGTH_SHORT).show()

                        showBoardWriteDialog()
                    }
                } else {
                    showBoardWriteDialog()
                }
            }
        }

        button_refresh.setOnClickListener {
            if (isrefresh == false) {
                Toast.makeText(getActivity(),"새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
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

                lastVisible = null
                if (isViewType == ViewType.STATISTICS) {
                    refreshPeople()
                } else {
                    if (isViewType == ViewType.POPULAR) {
                        refreshData(posts_popular)
                    } else if (isViewType == ViewType.NEW) {
                        refreshData(posts_new)
                    }
                }
            }
        }

        swipe_refresh_layout.setOnRefreshListener {
            if (isrefresh == false) {
                Toast.makeText(getActivity(),"새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
                swipe_refresh_layout.setRefreshing(false)
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

                if (isViewType != ViewType.STATISTICS) {
                    lastVisible = null
                    if (isViewType == ViewType.POPULAR) {
                        refreshData(posts_popular)
                    } else if (isViewType == ViewType.NEW) {
                        refreshData(posts_new)
                    }
                }
                swipe_refresh_layout.setRefreshing(false)
            }
        }

        text_popular.setOnClickListener {
            if (isViewType != ViewType.POPULAR) {
                showPopular()
            }
        }

        text_new.setOnClickListener {
            if (isViewType != ViewType.NEW) {
                showNew()
            }
        }

        text_statistics.setOnClickListener {
            if (isViewType != ViewType.STATISTICS) {
                showStatistics()
            }
        }

        img_question.setOnClickListener {
            var question = QuestionDTO(QuestionDTO.STAT.INFO, "응원점수 집계 및 계산", "")
            question.content = """
■ 응원점수 집계는 하루 한 번
    새벽 0시~2시 사이에 됩니다.
■ 점수 계산
    - 응원글 작성 : 50점, 좋아요 : 1점
    - 두 점수를 합한 점수가 합계 점수
                """
            val dialog = QuestionDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.setButtonCancel("닫기")
            dialog.showButtonOk(false)

            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
        }

        rayout_number_picker.visibility = View.GONE
        //button_data.visibility = View.GONE // 관리자모드
        button_data.setOnClickListener {
            refreshStatistics()
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

                if (isViewType == ViewType.POPULAR) {
                    if (!recyclerView.canScrollVertically(1)) {
                        if (posts_popular.size >= 30) {
                            isScrolling = false

                            refreshData(posts_popular)
                        }
                    }
                } else if (isViewType == ViewType.NEW) {
                    if (!recyclerView.canScrollVertically(1)) {
                        if (posts_new.size >= 30) {
                            isScrolling = false

                            refreshData(posts_new)
                        }
                    }
                }
            }
        })
    }

    fun showBoardWriteDialog() {
        val dialog = BoardWriteDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.button_cancel.setOnClickListener { // No
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            if (dialog.isWrite == true) {
                // 투표권 1장 추가
                var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
                var ticketcount = pref.getInt("TicketCount", preferencesDTO?.ticketChargeCount!!)

                var editor = pref.edit()
                editor.putInt("TicketCount", ticketcount + 1).apply()

                Toast.makeText(getActivity(),"응원하기로 티켓이 1장 추가되었습니다.", Toast.LENGTH_SHORT).show()

                showAd()
            }
        }
    }

    fun refreshData(posts : ArrayList<BoardDTO>) {
        loading()
        var field = ""
        if (isViewType == ViewType.POPULAR) {
            field = "likeCount"
        } else {
            field = "time"
        }

        /* 최근 글만 가져오려고 했지만 복합 쿼리가 안먹힘
        ?.whereGreaterThanOrEqualTo("time", cal.time)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val limit = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(limit)
        cal.add (Calendar.DATE, -90)*/

        if (lastVisible == null) {
            firestore?.collection(cheeringboardCollectionName)?.orderBy(field, Query.Direction.DESCENDING)
                ?.limit(30)?.get()?.addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    var person = document.toObject(BoardDTO::class.java)!!
                    if (dbHandler?.getblock(person.name.toString()) == true || dbHandler?.getblock(person.docname.toString()) == true) {
                        person.isBlock = true
                    }
                    posts.add(person)
                    lastVisible = result.documents.get(result.size() - 1)
                }
                if (result.size() > 0) {
                    recyclerView.adapter = RecyclerViewAdapterCheering(posts, this)
                }
                loadingEnd()
            }?.addOnFailureListener { exception ->

            }
        } else {
            firestore?.collection(cheeringboardCollectionName)?.orderBy(field, Query.Direction.DESCENDING)?.startAfter(lastVisible!!)?.limit(30)?.get()?.addOnSuccessListener { result ->
                //posts.clear()
                for (document in result) {
                    var board = document.toObject(BoardDTO::class.java)!!
                    if (dbHandler?.getblock(board.name.toString()) == true || dbHandler?.getblock(board.docname.toString()) == true) {
                        board.isBlock = true
                    }
                    posts.add(board)
                    lastVisible = result.documents.get(result.size() - 1)
                }
                //recyclerView.adapter = RecyclerViewAdapterCheering(posts, this)
                if (result.size() > 0) {
                    recyclerView.adapter?.notifyItemInserted(posts.size)
                }
                loadingEnd()
            }?.addOnFailureListener { exception ->

            }
        }
    }

    fun showPopular() {
        isViewType = ViewType.POPULAR
        lastVisible = null

        text_popular.setTextColor(Color.parseColor("#DDFFF319"))
        text_popular.paintFlags = text_popular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG
        text_new.setTextColor(Color.parseColor("#CCCCCC"))
        text_new.paintFlags = Paint.ANTI_ALIAS_FLAG
        text_statistics.setTextColor(Color.parseColor("#CCCCCC"))
        text_statistics.paintFlags = Paint.ANTI_ALIAS_FLAG

        recyclerView.adapter = RecyclerViewAdapterCheering(posts_popular, this)
        /*if (isViewType == ViewType.POPULAR) {
            refreshData(posts_popular)
        } else if (isViewType == ViewType.NEW) {
            refreshData(posts_new)
        }*/
    }

    fun showNew() {
        isViewType = ViewType.NEW
        lastVisible = null

        text_popular.setTextColor(Color.parseColor("#CCCCCC"))
        text_popular.paintFlags = Paint.ANTI_ALIAS_FLAG
        text_new.setTextColor(Color.parseColor("#DDFFF319"))
        text_new.paintFlags = text_popular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG
        text_statistics.setTextColor(Color.parseColor("#CCCCCC"))
        text_statistics.paintFlags = Paint.ANTI_ALIAS_FLAG

        recyclerView.adapter = RecyclerViewAdapterCheering(posts_new, this)
        /*if (isViewType == ViewType.POPULAR) {
            refreshData(posts_popular)
        } else if (isViewType == ViewType.NEW) {
            refreshData(posts_new)
        }*/
    }

    fun showStatistics() {
        isViewType = ViewType.STATISTICS
        lastVisible = null

        text_popular.setTextColor(Color.parseColor("#CCCCCC"))
        text_popular.paintFlags = Paint.ANTI_ALIAS_FLAG
        text_new.setTextColor(Color.parseColor("#CCCCCC"))
        text_new.paintFlags = Paint.ANTI_ALIAS_FLAG
        text_statistics.setTextColor(Color.parseColor("#DDFFF319"))
        text_statistics.paintFlags = text_popular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG

        recyclerView.adapter = RecyclerViewAdapterCheering(statistics, this)
    }

    override fun onItemClick(item: BoardDTO, position: Int) {
        if (isViewType != ViewType.STATISTICS) {
            val dialog = BoardDialog(requireContext(), item, getActivity()!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel.setOnClickListener {
                // No
                dialog.dismiss()
                if (dialog.isReport == true) {
                    lastVisible = null
                    if (isViewType == ViewType.STATISTICS) {
                        refreshPeople()
                    } else {
                        if (isViewType == ViewType.POPULAR) {
                            refreshData(posts_popular)
                        } else if (isViewType == ViewType.NEW) {
                            refreshData(posts_new)
                        }
                    }
                }
            }
            /*dialog.button_block.setOnClickListener {
                var question = QuestionDTO(
                    QuestionDTO.STAT.ERROR,
                    "응원글 차단",
                    "해당 응원글을 차단 하시겠습니까?"
                )
                if (item.isBlock) {
                    question.title = "응원글 차단 해제"
                    question.content = "해당 응원글을 차단 해제 하시겠습니까?"
                }

                val questionDialog = QuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_question_ok.setOnClickListener { // Ok
                    questionDialog.dismiss()
                    dialog.dismiss()
                    if (dbHandler?.getblock(item.docname.toString()) == false) {
                        dbHandler?.updateBlock(item.docname.toString(), 1)
                        Toast.makeText(context,"응원글 차단", Toast.LENGTH_SHORT).show()
                    } else {
                        dbHandler?.updateBlock(item.docname.toString(), 0)
                        Toast.makeText(context,"응원글 차단 해제", Toast.LENGTH_SHORT).show()
                    }

                    lastVisible = null
                    if (isViewType == ViewType.STATISTICS) {
                        refreshPeople()
                    } else {
                        if (isViewType == ViewType.POPULAR) {
                            refreshData(posts_popular)
                        } else if (isViewType == ViewType.NEW) {
                            refreshData(posts_new)
                        }
                    }
                }
            }*/
        }
    }

    override fun onItemClick_like(item: BoardDTO, like: TextView) {
        if (dbHandler?.getlike(item.docname.toString()) == false) {
            dbHandler?.updateLike(item.docname.toString(), 1)

            item.likeCount = item.likeCount!! + 1
            like.text = "${item.likeCount}"
            like.paintFlags = like.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            Toast.makeText(getActivity(),"좋아요", Toast.LENGTH_SHORT).show()
        } else {
            dbHandler?.updateLike(item.docname.toString(), 0)

            item.likeCount = item.likeCount!! - 1
            like.text = "${item.likeCount}"
            like.paintFlags = Paint.ANTI_ALIAS_FLAG

            Toast.makeText(getActivity(),"좋아요 취소", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onItemClick_dislike(item: BoardDTO, dislike: TextView) {
        if (dbHandler?.getdislike(item.docname.toString()) == false) {
            dbHandler?.updateDislike(item.docname.toString(), 1)

            item.dislikeCount = item.dislikeCount!! + 1
            dislike.text = "${item.dislikeCount}"
            dislike.paintFlags = dislike.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            Toast.makeText(getActivity(),"싫어요", Toast.LENGTH_SHORT).show()
        } else {
            dbHandler?.updateDislike(item.docname.toString(), 0)

            item.dislikeCount = item.dislikeCount!! - 1
            dislike.text = "${item.dislikeCount}"
            dislike.paintFlags = Paint.ANTI_ALIAS_FLAG

            Toast.makeText(getActivity(),"싫어요 취소", Toast.LENGTH_SHORT).show()
        }
    }

    fun showAd() {
        // 광고 종류 획득
        var firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var ad_interstitial = task.result!!["ad_interstitial"]
                callInterstitial(ad_interstitial as String)
            }
        }
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

    fun interstitialAdmob(isFirst : Boolean) {
        // 애드몹 - 전면
        var InterstitialAd = InterstitialAd(context)
        InterstitialAd.adUnitId = getString(R.string.admob_Interstitial_ad_unit_id)
        InterstitialAd.loadAd(AdRequest.Builder().build())

        InterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (InterstitialAd.isLoaded) {
                    InterstitialAd.show()
                } else {
                    // 광고 호출 실패
                    if (isFirst) {
                        interstitialCauly(false)
                    }
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                if (isFirst) {
                    interstitialCauly(false)
                }
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {

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
                }
            }

            override fun onClosedInterstitialAd(p0: CaulyInterstitialAd?) {

            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {

            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(getActivity())
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

    fun refreshPeople() {
        firestore?.collection(peopleCollectionName)?.orderBy("cheeringCountTotal", Query.Direction.DESCENDING)
            ?.get()?.addOnSuccessListener { result ->
                people.clear()
                statistics.clear()
                var index = 0
                for (document in result) {
                    var person = document.toObject(RankDTO::class.java)!!
                    people.add(person)
                    if (index == 0 && layout_top1 != null) {
                        layout_top1.visibility = View.VISIBLE
                        layout_top1.text_name.text = person.name
                        layout_top1.text_count.text = "응원글:${decimalFormat.format(person.cheeringCount)}"
                        layout_top1.text_count2.text = "좋아요:${decimalFormat.format(person.likeCount)}"
                    } else if (index == 1 && layout_top2 != null) {
                        layout_top2.visibility = View.VISIBLE
                        layout_top2.text_name.text = person.name
                        layout_top2.text_count.text = "응원글:${decimalFormat.format(person.cheeringCount)}"
                        layout_top2.text_count2.text = "좋아요:${decimalFormat.format(person.likeCount)}"
                    } else if (index == 2 && layout_top3 != null) {
                        layout_top3.visibility = View.VISIBLE
                        layout_top3.text_name.text = person.name
                        layout_top3.text_count.text = "응원글:${decimalFormat.format(person.cheeringCount)}"
                        layout_top3.text_count2.text = "좋아요:${decimalFormat.format(person.likeCount)}"
                    }

                    var board = BoardDTO()
                    board.image = person.image
                    board.title = "${index+1}등 ${person.name}"
                    board.content = "합계 점수 : ${decimalFormat.format(person.cheeringCountTotal)}"
                    board.name = "응원글 : ${decimalFormat.format(person.cheeringCount)}"
                    board.likeCount = person.likeCount
                    board.dislikeCount = person.dislikeCount
                    statistics.add(board)
                    index++
                }
            }?.addOnFailureListener { exception ->

            }
    }

    fun refreshStatistics() {
        Toast.makeText(getActivity(),"데이터 취합 시작", Toast.LENGTH_SHORT).show()
        // 01시에서 03시 사이에 최신폰에서 업데이트 하도록 처리
        var nowTime = SimpleDateFormat("HHmm").format(Date()).toInt()
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P && (nowTime >= 0 && nowTime <= 2400)) {
            println("카운트 획득 시작")
            var nowDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
            firestore?.collection(peopleCollectionName)?.get()?.addOnSuccessListener { result ->
                for (document in result) {
                    var person = document.toObject(RankDTO::class.java)!!
                    println("$person 처리 시작")
                    // 업데이트가 안된 항목만 처리
                    var updateDate = ""
                    if (person.updateDate != null) {
                        updateDate = SimpleDateFormat("yyyy-MM-dd").format(person.updateDate)
                    }
                    println("현재시간 : $nowDate, 업데이트시간 : $updateDate")

                    SystemClock.sleep(300)
                    if (number_picker.value == 0 || person.image.equals("profile${String.format("%03d",number_picker.value)}")) {
                    //if (nowDate != updateDate) {
                        loading()
                        println("업데이트 시작")
                        firestore?.collection(cheeringboardCollectionName)?.whereEqualTo("image", person.image)
                            ?.get()?.addOnSuccessListener { result ->
                                person.cheeringCount = result.size()
                                person.likeCount = 0
                                person.dislikeCount = 0
                                person.updateDate = Date()
                                for (document in result) {
                                    var post = document.toObject(BoardDTO::class.java)!!
                                    if (post.likeCount!! < 55555) {
                                        person.likeCount = person.likeCount!! + post.likeCount!!
                                        person.dislikeCount =
                                            person.dislikeCount!! + post.dislikeCount!!
                                    }
                                }

                                // 전체 점수는 응원글 50점, 좋아요 1점
                                person.cheeringCountTotal = (person.cheeringCount!! * 50) + person.likeCount!!

                                firestore?.collection(peopleCollectionName)
                                    ?.document(person.docname.toString())
                                    ?.set(person)

                                loadingEnd()
                                println("$person 카운트 변경")
                                Toast.makeText(getActivity(),"[${person.name} 완료.]", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { exception ->

                            }
                    }
                }
            }
                ?.addOnFailureListener { exception ->

                }

            println("카운트 획득 끝")
        //}
    }
}
