package com.ados.mstrotrematch2.page


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.Dimension
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.dialog.LoadingDialog
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.dialog.AdminDialog
import com.ados.mstrotrematch2.dialog.DonationStatusDialog
import com.ados.mstrotrematch2.model.RankDTO
import com.ados.mstrotrematch2.model.SeasonDTO
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.donation_status_dialog.button_ok
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.*
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.button_refresh
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.profile_rank_no1
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.profile_rank_no2
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.profile_rank_no3
import kotlinx.android.synthetic.main.notice_dialog.*
import kotlinx.android.synthetic.main.profile_item.view.*
import java.text.DecimalFormat
import kotlin.concurrent.timer

class FragmentPageRank : Fragment(), OnRankItemClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    var firestore : FirebaseFirestore? = null
    private var peopleTop3 : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther : ArrayList<RankDTO> = arrayListOf()
    lateinit var recyclerView : RecyclerView

    private var isrefresh = true

    var loadingDialog : LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_fragment_page_rank, container, false)

        var rootView = inflater.inflate(R.layout.fragment_fragment_page_rank, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview_rank!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        var index : Int = 1
        firestore = FirebaseFirestore.getInstance()

        /*firestore?.collection("people")?.orderBy("count", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            peopleTop3.clear()
            peopleOther.clear()
            if(querySnapshot == null)return@addSnapshotListener

            index = 1
            // document 수만큼 획득
            for(snapshot in querySnapshot){
                var person = snapshot.toObject(RankDTO::class.java)!!
                if (index < 4) { // Top 3 저장
                    peopleTop3.add(person)
                    ShowTop3()
                }
                else { // 4위 부터 저장
                    peopleOther.add(person)
                }
                index++
            }
            recyclerView.adapter =
                RecyclerViewAdapterRank(
                    peopleOther
                )
        }*/
        refreshPeople()

        /*var names : ArrayList<String> = arrayListOf(
            "강태관","강화","고재근","구자명","김경민","김수찬","김인석","김재혁","김중연","김태수",
            "김호중","김희재","나무","나태주","남승민","노지훈","류지광","미스터붐박스","박경래","삼식이",
            "신성","신인선","안성훈","양지원","영기","영탁","오샘","옥진욱","유호","이대원",
            "이도진","이재식","이찬원","임도형","임영웅","장민호","정동원","정호","천명훈","최대성",
            "최윤하","최정훈","추혁진","한강","허민영","홍잠언","황윤성"
        )
        var index2 : Int = 1
        for (name in names){
            var docname = String.format("no%05d",index2)
            var person = RankDTO(String.format("profile%02d",index2), name, 100, docname)
            firestore?.collection("people")?.document(docname)?.set(person)
            index2++
        }*/


        //recyclerView.adapter = MyRankRecyclerViewAdapter(peopleOther)
        /*var names : ArrayList<String> = arrayListOf(
                "가야송"
                ,"강동혜"
                ,"강보미"
                ,"강예빈"
                ,"강유진"
                ,"강혜연"
                ,"공소원"
                ,"권미희"
                ,"김가현"
                ,"김나현"
                ,"김나현"
                ,"김다나"
                ,"김다현"
                ,"김명선"
                ,"김사은"
                ,"김설아"
                ,"김성은"
                ,"김소유"
                ,"김수빈"
                ,"김수연"
                ,"김양희"
                ,"김연지"
                ,"김은빈"
                ,"김의영"
                ,"김지수"
                ,"김지율"
                ,"김태연"
                ,"김현경"
                ,"김현정"
                ,"나리"
                ,"나비"
                ,"나혜연"
                ,"누이들"
                ,"류원정"
                ,"뤼니 킴"
                ,"마리아"
                ,"명지"
                ,"문서연"
                ,"미스둥이"
                ,"박슬기"
                ,"박주희"
                ,"박해수"
                ,"방세옥"
                ,"방수정"
                ,"버블디아"
                ,"배혜지"
                ,"백수정"
                ,"백장미"
                ,"변혜진"
                ,"별사랑"
                ,"서예림"
                ,"성민지"
                ,"소유미"
                ,"송유진"
                ,"송하예"
                ,"신가윤"
                ,"손"
                ,"아트윈스"
                ,"안은주"
                ,"애슐리"
                ,"양양"
                ,"양지은"
                ,"연예진"
                ,"영지"
                ,"오서영"
                ,"오승연"
                ,"오승은"
                ,"오희선"
                ,"우현정"
                ,"윤희"
                ,"윤태화"
                ,"은가은"
                ,"이보경"
                ,"이소원"
                ,"이승연"
                ,"이예은"
                ,"이재은"
                ,"이하은"
                ,"이희민"
                ,"임다애"
                ,"임서원"
                ,"장서윤"
                ,"장하온"
                ,"장태희"
                ,"장향희"
                ,"전영랑"
                ,"전유진"
                ,"전향진"
                ,"정은주"
                ,"정해진"
                ,"정혜"
                ,"조혜령"
                ,"주미"
                ,"진달래"
                ,"찬미찬송"
                ,"채은정"
                ,"최설화"
                ,"최은비"
                ,"최형선"
                ,"태하"
                ,"트윈걸스"
                ,"파스텔걸스"
                ,"하나"
                ,"하서정"
                ,"하이량"
                ,"한초임"
                ,"허윤아"
                ,"허찬미"
                ,"혜진이"
                ,"홍지윤"
                ,"황승아"
                ,"황우림"
        )

        var index2 : Int = 1
        for (name in names){
            var docname = String.format("no%05d",index2)
            var person = RankDTO(String.format("profile%03d",index2), name, 0, docname)
            firestore?.collection("people")?.document(docname)?.set(person)
            index2++
        }*/

        /*firestore?.collection("people")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                person.count = 0
                firestore?.collection("people_back")?.document(person.docname.toString())?.set(person)
            }
        }
                ?.addOnFailureListener { exception ->

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

        /*var person = RankDTO("profile113", "송가인", 0, "no00113")
        firestore?.collection("people_back")?.document(person.docname.toString())?.set(person)

        var person2 = RankDTO("profile114", "정미애", 0, "no00114")
        firestore?.collection("people_back")?.document(person2.docname.toString())?.set(person2)

        var person3 = RankDTO("profile115", "홍자", 0, "no00115")
        firestore?.collection("people_back")?.document(person3.docname.toString())?.set(person3)

        var person4 = RankDTO("profile116", "정다경", 0, "no00116")
        firestore?.collection("people_back")?.document(person4.docname.toString())?.set(person4)

        var person5 = RankDTO("profile117", "김나희", 0, "no00117")
        firestore?.collection("people_back")?.document(person5.docname.toString())?.set(person5)*/

        return rootView
    }

    fun refreshPeople() {
        loading()
        var index : Int = 1
        firestore?.collection("people")?.orderBy("count", Query.Direction.DESCENDING)?.get()?.addOnSuccessListener { result ->
            peopleTop3.clear()
            peopleOther.clear()

            index = 1
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                if (index < 4) { // Top 3 저장
                    peopleTop3.add(person)
                    ShowTop3()
                }
                else { // 4위 부터 저장
                    peopleOther.add(person)
                }
                index++
            }
            recyclerView.adapter = RecyclerViewAdapterRank(peopleOther, this)
        }
            ?.addOnFailureListener { exception ->

            }
        loadingEnd()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firestore?.collection("preferences")?.document("season")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var seasonDTO = documentSnapshot?.toObject(SeasonDTO::class.java)

            // 시즌 변경 작업
            //seasonDTO?.seasonNum = 6
            //text_season_end_date.visibility = View.GONE
            //button_refresh.visibility = View.GONE
            var rank_background_img = R.drawable.spotlight_s6_main
            var season_logo_img = R.drawable.season6_logo
            if (seasonDTO?.seasonNum!! == 5) {
                rank_background_img = R.drawable.spotlight_s5_main
                season_logo_img = R.drawable.season5_logo
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

            text_season_end_date.text = seasonDTO?.endDate
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

                refreshPeople()
            }
        }

        img_hall_of_fame.setOnClickListener {
            (activity as MainActivity?)!!.callHallOfFameActivity()
        }

        //adapter 추가
        //recyclerview_rank.adapter = MyRankRecyclerViewAdapter()
        //레이아웃 매니저 추가
        //recyclerview_rank.layoutManager = LinearLayoutManager(view)
        //recyclerview_rank.layoutManager = RelativeLayoutManager(this)

        //profile_rank_no1.img_rank.setImageResource(R.drawable.crown_gold)
        //profile_rank_no2.img_rank.setImageResource(R.drawable.crown_silver)
        //profile_rank_no3.img_rank.setImageResource(R.drawable.crown_bronze)
        Glide.with(profile_rank_no1.img_rank.context)
            .asBitmap()
            .load(R.drawable.crown_gold) ///feed in path of the image
            .fitCenter()
            .into(profile_rank_no1.img_rank)
        Glide.with(profile_rank_no2.img_rank.context)
            .asBitmap()
            .load(R.drawable.crown_silver) ///feed in path of the image
            .fitCenter()
            .into(profile_rank_no2.img_rank)
        Glide.with(profile_rank_no3.img_rank.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .fitCenter()
            .into(profile_rank_no3.img_rank)

        Glide.with(img_hall_of_fame.context)
            .asBitmap()
            .load(R.drawable.hall_of_fame_button) ///feed in path of the image
            .fitCenter()
            .into(img_hall_of_fame)

        /*var imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no1.img_profile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no2.img_profile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no3.img_profile.setImageResource(imageID)
        }*/

        profile_rank_no1.visibility = View.GONE
        profile_rank_no2.visibility = View.GONE
        profile_rank_no3.visibility = View.GONE

        profile_rank_no1.text_rank.text = "현재  1위"
        profile_rank_no1.text_rank.setTextSize(Dimension.SP, 10.toFloat())
        profile_rank_no1.text_name.setTextSize(Dimension.SP, 10.toFloat())
        profile_rank_no1.text_count.setTextSize(Dimension.SP, 10.toFloat())

        profile_rank_no2.text_rank.text = "현재  2위"
        profile_rank_no2.text_rank.setTextSize(Dimension.SP, 9.toFloat())
        profile_rank_no2.text_name.setTextSize(Dimension.SP, 9.toFloat())
        profile_rank_no2.text_count.setTextSize(Dimension.SP, 9.toFloat())

        profile_rank_no3.text_rank.text = "현재  3위"
        profile_rank_no3.text_rank.setTextSize(Dimension.SP, 8.toFloat())
        profile_rank_no3.text_name.setTextSize(Dimension.SP, 8.toFloat())
        profile_rank_no3.text_count.setTextSize(Dimension.SP, 8.toFloat())

        profile_rank_no1.setOnClickListener {
            callDonationStatusDialog(peopleTop3[0], 1)
        }
        profile_rank_no2.setOnClickListener {
            callDonationStatusDialog(peopleTop3[1], 2)
        }
        profile_rank_no3.setOnClickListener {
            callDonationStatusDialog(peopleTop3[2], 3)
        }

        //button_admin.visibility = View.GONE // 관리자모드
        button_admin.setOnClickListener {
            val dialog = AdminDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_ok.setOnClickListener { // OK
                dialog.dismiss()
            }
        }
    }

    fun ShowTop3() {
        var imageID : Int = 0
        /*var imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no1.img_profile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no2.img_profile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no3.img_profile.setImageResource(imageID)
        }*/

        if (peopleTop3.size > 0 && profile_rank_no1 != null) {
            profile_rank_no1.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)!!
            //profile_rank_no1.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no1.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no1.img_profile)
            profile_rank_no1.text_name.text = peopleTop3[0].name
            profile_rank_no1.text_count.text = "${decimalFormat.format(peopleTop3[0].count)}표"

            // 시즌 변경 작업
            //profile_rank_no1.img_profile.visibility = View.GONE
            //profile_rank_no1.text_name.visibility = View.GONE
            //profile_rank_no1.text_count.visibility = View.GONE
        }
        if (peopleTop3.size > 1 && profile_rank_no2 != null) {
            profile_rank_no2.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)!!
            //profile_rank_no2.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no2.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no2.img_profile)
            profile_rank_no2.text_name.text = peopleTop3[1].name
            profile_rank_no2.text_count.text = "${decimalFormat.format(peopleTop3[1].count)}표"

            // 시즌 변경 작업
            //profile_rank_no2.img_profile.visibility = View.GONE
            //profile_rank_no2.text_name.visibility = View.GONE
            //profile_rank_no2.text_count.visibility = View.GONE
        }
        if (peopleTop3.size > 2 && profile_rank_no3 != null) {
            profile_rank_no3.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)!!
            //profile_rank_no3.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no3.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no3.img_profile)
            profile_rank_no3.text_name.text = peopleTop3[2].name
            profile_rank_no3.text_count.text = "${decimalFormat.format(peopleTop3[2].count)}표"

            // 시즌 변경 작업
            //profile_rank_no3.img_profile.visibility = View.GONE
            //profile_rank_no3.text_name.visibility = View.GONE
            //profile_rank_no3.text_count.visibility = View.GONE
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

    override fun onItemClick(item: RankDTO, position: Int) {
        callDonationStatusDialog(item, position.plus(4))
    }

    fun callDonationStatusDialog(item: RankDTO, rank: Int) {
        val dialog = DonationStatusDialog(requireContext(), item, rank)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.button_ok.setOnClickListener { // OK
            dialog.dismiss()
        }
    }
}

