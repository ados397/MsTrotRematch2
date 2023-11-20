package com.ados.mstrotrematch2

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.databinding.ActivityHallOfFameBinding
import com.ados.mstrotrematch2.dialog.DonationCertificateDialog
import com.ados.mstrotrematch2.dialog.LoadingDialog
import com.ados.mstrotrematch2.model.*
import com.ados.mstrotrematch2.adapter.OnCheeringItemClickListener
import com.ados.mstrotrematch2.adapter.OnRankItemClickListener
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterCheeringTotal
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterRank
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.util.AdsBannerManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class HallOfFameActivity : AppCompatActivity(), OnRankItemClickListener, OnCheeringItemClickListener {
    private lateinit var binding: ActivityHallOfFameBinding
    enum class HallOfFameType {
        MR_NEW, MR_OLD, MR2, FIRE
    }

    enum class DisplayType {
        VOTE, CHEERING, ONE_MILLION, TWO_MILLION, THREE_MILLION, FOUR_MILLION, FIVE_MILLION, SIX_MILLION, DONATION
    }

    private val menuItemList = listOf(
        R.id.Item1, R.id.Item2, R.id.Item3, R.id.Item4, R.id.Item5, R.id.Item6, R.id.Item7, R.id.Item8, R.id.Item9
    )

    private val firebaseViewModel : FirebaseViewModel by viewModels() // 뷰모델 연결
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var hallOfFameType = HallOfFameType.MR_OLD

    lateinit var recyclerView : RecyclerView
    var loadingDialog : LoadingDialog? = null
    private var selectedSeasonNum = 1
    private var selectedMenuId = 0

    private var displayTypeIndex = 2
    private val displayTypes : List<DisplayType> = listOf(DisplayType.VOTE, DisplayType.CHEERING, DisplayType.DONATION)

    private var peopleTop3Vote : ArrayList<RankDTO> = arrayListOf()
    private var peopleOtherVote : ArrayList<RankDTO> = arrayListOf()
    private var peopleOtherDonation : ArrayList<RankDTO> = arrayListOf()

    private var peopleTop3Cheering : ArrayList<RankDTO> = arrayListOf()
    private var peopleOtherCheering : ArrayList<RankDTO> = arrayListOf()

    lateinit var adPolicyDTO: AdPolicyDTO
    lateinit var seasonDTO: SeasonDTO
    private var adsBannerManager : AdsBannerManager? = null // AD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHallOfFameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adPolicyDTO = intent.getParcelableExtra("adPolicy")!!
        seasonDTO = intent.getParcelableExtra("season")!!
        //seasonDTO.seasonNum = 2 // @ 시즌 변경

        hallOfFameType = intent.getSerializableExtra("hallOfFameType") as HallOfFameType

        adsBannerManager = AdsBannerManager(this, lifecycle, adPolicyDTO, binding.adViewAdmob, binding.xmladview, binding.adViewKakao)
        adsBannerManager?.callBanner {

        }

        recyclerView = binding.recyclerviewRank
        recyclerView.layoutManager = LinearLayoutManager(this)

        when (hallOfFameType) {
            HallOfFameType.MR_NEW -> {
                selectedSeasonNum = seasonDTO.seasonNum?.minus(1)!!
            }
            HallOfFameType.MR_OLD -> {
                selectedSeasonNum = 9
            }
            else -> {
                binding.textOtherSeason.visibility = View.GONE
            }
        }

        observeVote()
        observeCheering()
        loadData()

        // Top 3 화면 설정
        binding.profileRankNo1.root.visibility = View.GONE
        binding.profileRankNo2.root.visibility = View.GONE
        binding.profileRankNo3.root.visibility = View.GONE

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

        Glide.with(binding.imgTitle.context)
            .asBitmap()
            .load(R.drawable.hall_of_fame_title) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgTitle)
        Glide.with(binding.imgLeft.context)
            .asBitmap()
            .load(R.drawable.left_arrow) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgLeft)
        Glide.with(binding.imgRight.context)
            .asBitmap()
            .load(R.drawable.right_arrow) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgRight)


        binding.profileRankNo1.textRank.text = "최종  1위"
        binding.profileRankNo1.textRank.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.5f)
        binding.profileRankNo1.textName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19.0f)
        binding.profileRankNo1.textCount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
        Glide.with(binding.profileRankNo1.imgLineTop.context)
            .asBitmap()
            .load(R.drawable.gold_line_top) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo1.imgLineTop)

        binding.profileRankNo2.textRank.text = "최종  2위"
        binding.profileRankNo2.textRank.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f)
        binding.profileRankNo2.textName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17.0f)
        binding.profileRankNo2.textCount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f)
        Glide.with(binding.profileRankNo2.imgLineTop.context)
            .asBitmap()
            .load(R.drawable.red_line_top) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo2.imgLineTop)

        binding.profileRankNo3.textRank.text = "최종  3위"
        binding.profileRankNo3.textRank.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.0f)
        binding.profileRankNo3.textName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
        binding.profileRankNo3.textCount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.0f)
        Glide.with(binding.profileRankNo3.imgLineTop.context)
            .asBitmap()
            .load(R.drawable.blue_line_top) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.profileRankNo3.imgLineTop)

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.imgLeft.setOnClickListener {
            if (displayTypeIndex == 0)
                displayTypeIndex = displayTypes.size - 1
            else
                displayTypeIndex--

            var animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.horizon_left)
            binding.mainLayout.startAnimation(animation)

            display()
        }

        binding.imgRight.setOnClickListener {
            if (displayTypeIndex == (displayTypes.size - 1))
                displayTypeIndex = 0
            else
                displayTypeIndex++

            var animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.horizon_right)
            binding.mainLayout.startAnimation(animation)

            display()
        }

        binding.profileRankNo1.root.setOnClickListener {
            when(displayTypes[displayTypeIndex]) {
                DisplayType.DONATION, DisplayType.VOTE -> {
                    callDonationCertificateDialog(peopleTop3Vote[0], 1)
                }
            }
        }
        binding.profileRankNo2.root.setOnClickListener {
            when(displayTypes[displayTypeIndex]) {
                DisplayType.DONATION, DisplayType.VOTE -> {
                    callDonationCertificateDialog(peopleTop3Vote[1], 2)
                }
            }
        }
        binding.profileRankNo3.root.setOnClickListener {
            when(displayTypes[displayTypeIndex]) {
                DisplayType.DONATION, DisplayType.VOTE -> {
                    callDonationCertificateDialog(peopleTop3Vote[2], 3)
                }
            }
        }

        binding.textOtherSeason.paintFlags = binding.textOtherSeason.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.textOtherSeason.setOnClickListener {
            var pop = PopupMenu(this, binding.textOtherSeason)
            for ((index, id) in menuItemList.withIndex()) {
                pop.menu.add(Menu.NONE, id, Menu.NONE, "시즌${index+1} 명예의 전당")
            }

            // 1. 리스너로 처리
            var listener = PopupListener()
            pop.setOnMenuItemClickListener(listener)

            // 2. 람다식으로 처리
            pop.setOnMenuItemClickListener { item ->
                if (selectedMenuId != item.itemId) {
                    selectedMenuId = item.itemId
                    when (item.itemId) {
                        R.id.Item1 -> selectedSeasonNum = 1
                        R.id.Item2 -> selectedSeasonNum = 2
                        R.id.Item3 -> selectedSeasonNum = 3
                        R.id.Item4 -> selectedSeasonNum = 4
                        R.id.Item5 -> selectedSeasonNum = 5
                        R.id.Item6 -> selectedSeasonNum = 6
                        R.id.Item7 -> selectedSeasonNum = 7
                        R.id.Item8 -> selectedSeasonNum = 8
                        R.id.Item9 -> selectedSeasonNum = 9
                    }
                    loadData()
                }
                false
            }
            pop.show()
        }


        // 뉴스 기록
        // 시즌 변경 작업 // 시즌교체 작업
        //var firestore = FirebaseFirestore.getInstance()
        // 투표 수 기록
        /*firestore?.collection("people")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_9")?.collection("vote")?.document(person.docname!!)?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }

        // 응원수 기록
        firestore?.collection("people_cheering")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_9")?.collection("cheering")?.document(person.docname!!)?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/
        /*firestore?.collection("news")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(NewsDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_13")?.collection("news")?.document()?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/
        // 투표 수 기록
        /*firestore?.collection("people")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_13")?.collection("vote")?.document(person.docname!!)?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/

        // 응원수 기록
        /*firestore?.collection("people_cheering")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_13")?.collection("cheering")?.document(person.docname!!)?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/

        /*firestore?.collection("season_result")?.document("season_5")?.collection("vote")?.document("no00033")?.collection("donationNews")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var news = document.toObject(DonationNewsDTO::class.java)!!

                firestore?.collection("season_result")?.document("season_6")?.collection("vote")?.document("no00033")?.collection("donationNews")?.document()?.set(news) // 이찬원
            }

        }*/

       /*firestore?.collection("season_result")?.document("season_12")?.collection("vote")?.document("no00035")?.collection("donationNews")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var news = document.toObject(DonationNewsDTO::class.java)!!

                //firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00035")?.collection("donationNews")?.document()?.set(news) // 임영웅
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00033")?.collection("donationNews")?.document()?.set(news) // 이찬원
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00026")?.collection("donationNews")?.document()?.set(news) // 영탁
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00037")?.collection("donationNews")?.document()?.set(news) // 정동원
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00036")?.collection("donationNews")?.document()?.set(news) // 장민호
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00012")?.collection("donationNews")?.document()?.set(news) // 김희재
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00023")?.collection("donationNews")?.document()?.set(news) // 안성훈
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00021")?.collection("donationNews")?.document()?.set(news) // 신성
                //firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00009")?.collection("donationNews")?.document()?.set(news) // 김중연
            }

        }*/

        // 전체 순위 조회 (그알세)
        /*var index = 1
        firestore?.collection("season_result")?.document("season_12")?.collection("vote")?.orderBy("count", Query.Direction.DESCENDING)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                println ("${index++}. ${person.name} : ${decimalFormat.format(person.count)}표")
            }
        }
            ?.addOnFailureListener { exception ->

            }*/

        // 사진
        //binding.imgLeft.visibility = View.GONE
        //binding.imgRight.visibility = View.GONE
        if (hallOfFameType == HallOfFameType.MR_NEW)
            binding.textOtherSeason.visibility = View.GONE
    }

    inner class PopupListener : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(p0: MenuItem?): Boolean {
            when (p0?.itemId) {
                R.id.Item1 ->
                    binding.textOtherSeason.text = "menu1"
                R.id.Item2 ->
                    binding.textOtherSeason.text = "menu1"
                R.id.Item3 ->
                    binding.textOtherSeason.text = "menu1"
                R.id.Item4 ->
                    binding.textOtherSeason.text = "menu1"
                R.id.Item5 ->
                    binding.textOtherSeason.text = "menu1"
                R.id.Item6 ->
                    binding.textOtherSeason.text = "menu1"
                R.id.Item7 ->
                    binding.textOtherSeason.text = "menu1"
                R.id.Item8 ->
                    binding.textOtherSeason.text = "menu1"
                R.id.Item9 ->
                    binding.textOtherSeason.text = "menu1"
            }
            return false
        }

    }

    private fun loadData() {
        var imgBackgroundName = if (hallOfFameType == HallOfFameType.MR_NEW) "spotlight_new_s${selectedSeasonNum}_main"
        else "spotlight_s${selectedSeasonNum}_main"
        var imageID = resources.getIdentifier(imgBackgroundName, "drawable", packageName)
        if (imageID > 0) {
            Glide.with(binding.imgRankBackground.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.imgRankBackground)
        }

        var imgSeasonLogoName = if (hallOfFameType == HallOfFameType.MR_NEW) "new_season${selectedSeasonNum}_logo"
        else "season${selectedSeasonNum}_logo"
        imageID = resources.getIdentifier(imgSeasonLogoName, "drawable", packageName)
        if (imageID > 0) {
            Glide.with(binding.imgSeasonLogo.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.imgSeasonLogo)
        }

        peopleTop3Vote.clear()
        peopleOtherVote.clear()
        peopleOtherDonation.clear()
        peopleTop3Cheering.clear()
        peopleOtherCheering.clear()

        loadVote()
        loadCheering()

        loading()
        timer(period = 100)
        {
            if (peopleTop3Vote.size > 0) {
                cancel()
                //display()
                runOnUiThread {
                    display()
                    loadingEnd()
                }
            }
        }
    }

    private fun getDocName() : String = when (hallOfFameType) {
        HallOfFameType.MR_NEW -> "newSeason_${String.format("%04d",selectedSeasonNum)}"
        HallOfFameType.MR_OLD -> "season_$selectedSeasonNum"
        HallOfFameType.MR2 -> "season_${selectedSeasonNum}_mr2"
        HallOfFameType.FIRE -> "season_${selectedSeasonNum}_fire"
    }

    private fun observeVote() {
        firebaseViewModel.hallOfFameVote.observe(this) {
            if (firebaseViewModel.hallOfFameVote.value != null) {
                peopleTop3Vote = firebaseViewModel.hallOfFameVote.value!!
                peopleOtherVote = firebaseViewModel.hallOfFameVote.value!!.clone() as ArrayList<RankDTO>
                peopleOtherVote.subList(7, peopleOtherVote.size).clear()
                peopleOtherVote.subList(0, 3).clear()

                for (person in firebaseViewModel.hallOfFameVote.value!!) {
                    if (person.count!! >= 1000000) {
                        var addPerson = person.copy()
                        addPerson.subTitle = "${decimalFormat.format(addPerson.count?.div(1000000)?.times(10))}만원 기부"
                        peopleOtherDonation.add(addPerson)
                    }
                }
                peopleOtherDonation.subList(0, 3).clear()

            }
        }
    }

    private fun observeCheering() {
        firebaseViewModel.hallOfFameCheering.observe(this) {
            if (firebaseViewModel.hallOfFameCheering.value != null) {
                peopleTop3Cheering = firebaseViewModel.hallOfFameCheering.value!!
                peopleOtherCheering = firebaseViewModel.hallOfFameCheering.value!!.clone() as ArrayList<RankDTO>
                peopleOtherCheering.subList(7, peopleOtherCheering.size).clear()
            }
        }
    }

    // 투표 순위 조회
    private fun loadVote() {
        firebaseViewModel.getHallOfFameVote(getDocName())
    }

    // 투표 순위 조회
    private fun loadCheering() {
        firebaseViewModel.getHallOfFameCheering(getDocName())
    }

    private fun showVoteTop3() {
        var imageID : Int = 0

        if (peopleTop3Vote.size > 0 && binding.profileRankNo1 != null) {
            binding.profileRankNo1.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Vote[0].image, "drawable", packageName)!!
            //binding.profileRankNo1.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo1.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo1.imgProfile)
            binding.profileRankNo1.textName.text = peopleTop3Vote[0].name
            binding.profileRankNo1.textCount.text = "${decimalFormat.format(peopleTop3Vote[0].count!!)}"
        }
        if (peopleTop3Vote.size > 1 && binding.profileRankNo2 != null) {
            binding.profileRankNo2.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Vote[1].image, "drawable", packageName)!!
            //binding.profileRankNo2.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo2.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo2.imgProfile)
            binding.profileRankNo2.textName.text = peopleTop3Vote[1].name
            binding.profileRankNo2.textCount.text = "${decimalFormat.format(peopleTop3Vote[1].count!!)}"
        }
        if (peopleTop3Vote.size > 2 && binding.profileRankNo3 != null) {
            binding.profileRankNo3.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Vote[2].image, "drawable", packageName)!!
            //binding.profileRankNo3.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo3.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo3.imgProfile)
            binding.profileRankNo3.textName.text = peopleTop3Vote[2].name
            binding.profileRankNo3.textCount.text = "${decimalFormat.format(peopleTop3Vote[2].count!!)}"
        }
    }

    private fun ShowCheeringTop3() {
        var imageID : Int = 0

        if (peopleTop3Cheering.size > 0 && binding.profileRankNo1 != null) {
            binding.profileRankNo1.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Cheering[0].image, "drawable", packageName)!!
            //binding.profileRankNo1.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo1.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo1.imgProfile)
            binding.profileRankNo1.textName.text = peopleTop3Cheering[0].name
            binding.profileRankNo1.textCount.text = "${decimalFormat.format(peopleTop3Cheering[0].cheeringCountTotal)}점"
        }
        if (peopleTop3Cheering.size > 1 && binding.profileRankNo2 != null) {
            binding.profileRankNo2.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Cheering[1].image, "drawable", packageName)!!
            //binding.profileRankNo2.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo2.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo2.imgProfile)
            binding.profileRankNo2.textName.text = peopleTop3Cheering[1].name
            binding.profileRankNo2.textCount.text = "${decimalFormat.format(peopleTop3Cheering[1].cheeringCountTotal)}점"
        }
        if (peopleTop3Cheering.size > 2 && binding.profileRankNo3 != null) {
            binding.profileRankNo3.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Cheering[2].image, "drawable", packageName)!!
            //binding.profileRankNo3.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo3.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo3.imgProfile)
            binding.profileRankNo3.textName.text = peopleTop3Cheering[2].name
            binding.profileRankNo3.textCount.text = "${decimalFormat.format(peopleTop3Cheering[2].cheeringCountTotal)}점"
        }
    }

    private fun ShowDonation() {
        var imageID : Int = 0

        if (peopleTop3Vote.size > 0 && binding.profileRankNo1 != null) {
            binding.profileRankNo1.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Vote[0].image, "drawable", packageName)!!
            //binding.profileRankNo1.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo1.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo1.imgProfile)
            binding.profileRankNo1.textName.text = peopleTop3Vote[0].name
            binding.profileRankNo1.textCount.text = "${decimalFormat.format(peopleTop3Vote[0].count?.div(1000000)?.times(10)?.plus(30))}만원 기부"
        }
        if (peopleTop3Vote.size > 1 && binding.profileRankNo2 != null) {
            binding.profileRankNo2.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Vote[1].image, "drawable", packageName)!!
            //binding.profileRankNo2.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo2.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo2.imgProfile)
            binding.profileRankNo2.textName.text = peopleTop3Vote[1].name
            binding.profileRankNo2.textCount.text = "${decimalFormat.format(peopleTop3Vote[1].count?.div(1000000)?.times(10)?.plus(20))}만원 기부"
        }
        if (peopleTop3Vote.size > 2 && binding.profileRankNo3 != null) {
            binding.profileRankNo3.root.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3Vote[2].image, "drawable", packageName)!!
            //binding.profileRankNo3.imgProfile.setImageResource(imageID)
            Glide.with(binding.profileRankNo3.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.profileRankNo3.imgProfile)
            binding.profileRankNo3.textName.text = peopleTop3Vote[2].name
            binding.profileRankNo3.textCount.text = "${decimalFormat.format(peopleTop3Vote[2].count?.div(1000000)?.times(10)?.plus(10))}만원 기부"
        }
    }

    private fun display() {
        binding.profileRankNo1.root.visibility = View.GONE
        binding.profileRankNo2.root.visibility = View.GONE
        binding.profileRankNo3.root.visibility = View.GONE

        when(displayTypes[displayTypeIndex]) {
            DisplayType.VOTE -> {
                binding.buttonTitle.text = "투표 순위 Top 7"
                showVoteTop3()
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOtherVote, this)
            }
            DisplayType.CHEERING -> {
                binding.buttonTitle.text = "응원 순위 Top 7"
                ShowCheeringTop3()
                recyclerView.adapter = RecyclerViewAdapterCheeringTotal(peopleOtherCheering)
            }
            DisplayType.DONATION -> {
                binding. buttonTitle.text = "♡ 기부 내역 ♡"
                ShowDonation()
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOtherDonation, this)
            }
        }
    }

    private fun loading() {
        android.os.Handler().postDelayed({
            if (loadingDialog == null) {
                loadingDialog = LoadingDialog(this)
                loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                loadingDialog?.setCanceledOnTouchOutside(false)
            }
            loadingDialog?.show()
        }, 0)
    }

    private fun loadingEnd() {
        android.os.Handler().postDelayed({
            loadingDialog?.dismiss()
        }, 400)
    }

    override fun onItemClick(item: BoardDTO, position: Int) {

    }

    override fun onItemClickLike(item: BoardDTO, like: TextView) {

    }

    override fun onItemClick(item: RankDTO, position: Int) {
        println("기부내역 클릭 ${displayTypes[displayTypeIndex]}, $displayTypeIndex")
        when(displayTypes[displayTypeIndex]) {
            DisplayType.DONATION, DisplayType.VOTE -> {
                callDonationCertificateDialog(item, position.plus(4))
            }
        }
    }

    private fun callDonationCertificateDialog(item: RankDTO, rank: Int) {
        firebaseViewModel.getDonationNews(getDocName(), item.docname.toString()) { donationNews ->
            var intent = Intent(this, DonationCertificateActivity::class.java)
            intent.putParcelableArrayListExtra("donationNews", donationNews)
            intent.putExtra("item", item)
            intent.putExtra("rank", rank)
            intent.putExtra("hallOfFameType", hallOfFameType)
            startActivity(intent)
        }
    }

    private fun formatNumber(value: Int): String {
        return when {
            value >= 1E3 -> "${decimalFormat.format((value.toFloat() / 1E3).toInt())}K"
            else -> NumberFormat.getInstance().format(value)
        }
    }
}