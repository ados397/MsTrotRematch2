package com.ados.mstrotrematch2.page


import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.ItemList
import com.ados.mstrotrematch2.model.MovieDTO
import com.ados.mstrotrematch2.model.RankDTO
import com.ados.mstrotrematch2.model.YoutubeApi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.gson.GsonBuilder
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.android.synthetic.main.fragment_fragment_page_music.*
import okhttp3.*
import java.io.IOException


class FragmentPageMusic : Fragment(), OnMusicItemClickListener {

    var firestore : FirebaseFirestore? = null
    private var movieList : ArrayList<ItemList> = arrayListOf()
    lateinit var recyclerView : RecyclerView
    var mPlayer: YouTubePlayer? = null
    var curVideoId: String? = null
    var youtubeApi = YoutubeApi()
    var jsonbody: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_fragment_page_music, container, false)

        var rootView = inflater.inflate(R.layout.fragment_fragment_page_music, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview_music!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        /*firestore = FirebaseFirestore.getInstance()
        firestore?.collection("people")?.orderBy("count", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            people.clear()
            if (querySnapshot == null) return@addSnapshotListener

            // document 수만큼 획득
            for (snapshot in querySnapshot) {
                var person = snapshot.toObject(MovieDTO::class.java)!!
                people.add(person)
            }
            recyclerView.adapter =
                RecyclerViewAdapterMusic(people, this)
        }*/

        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("json")?.document("popular_list")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                jsonbody = task.result!!["body"].toString()
                loadMovieList(youtubeApi)
            }
        }
        /*firestore?.collection("preferences")?.document("youtubeapi")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){


                if (task.result!!["q"] != null) youtubeApi.q = task.result!!["q"].toString()
                if (task.result!!["part"] != null) youtubeApi.part = task.result!!["part"].toString()
                if (task.result!!["key"] != null) youtubeApi.key = task.result!!["key"].toString()
                if (task.result!!["type"] != null) youtubeApi.type = task.result!!["type"].toString()
                if (task.result!!["maxResults"] != null) youtubeApi.maxResults = task.result!!["maxResults"].toString()
                if (task.result!!["videoDuration"] != null) youtubeApi.videoDuration = task.result!!["videoDuration"].toString()
                if (task.result!!["keyword"] != null) youtubeApi.keyword = task.result!!["keyword"].toString()

                val url = youtubeApi.getUrl()
                println(url)

                loadMovieList(youtubeApi)
            }
        }*/

        return rootView
    }

    fun setAdapter() {
        recyclerView.adapter = RecyclerViewAdapterMusic(movieList, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //tv_fragment_name.text = "노래듣기"

        getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        player_view.getPlayerUiController().showFullscreenButton(true)
        player_view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                mPlayer = youTubePlayer
                val videoId = "uAHaUXV6_vE"
                youTubePlayer.cueVideo(videoId, 0f)

                curVideoId = ""
            }
        })

        player_view.getPlayerUiController().setFullScreenButtonClickListener(View.OnClickListener {
            if (player_view.isFullScreen()) {
                player_view.exitFullScreen()
                getActivity()?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                // Show Tabs
                (activity as MainActivity?)!!.showMainCtrl(true)
                //searchCtrlShow(true)

                //세로 화면으로 고정
                getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            } else {
                player_view.enterFullScreen()
                getActivity()?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                // Hide Tabs
                (activity as MainActivity?)!!.showMainCtrl(false)
                //searchCtrlShow(false)

                // 고정 풀고 센서에 따라 화면 전환 모드
                getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        })

        button_search.setOnClickListener {
            youtubeApi.q = edit_search_movie.text.toString()
            loadMovieList(youtubeApi)
        }

        edit_search_movie.setOnKeyListener { v, keyCode, event ->
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) {
                youtubeApi.q = edit_search_movie.text.toString()
                loadMovieList(youtubeApi)
            }
            false
        }

        //button_insertMove.visibility = View.GONE // 관리자모드
        button_insertMove.setOnClickListener {
            Toast.makeText(getActivity(),"업로드 시작", Toast.LENGTH_SHORT).show()
            insertMovieList()
        }
    }

    override fun onItemClick(item: ItemList, position: Int) {
        if (curVideoId == item.id.videoId) { // 동일한 비디오 한 번 더 클릭하면 플레이어 닫기
            curVideoId = ""
            player_view.visibility = View.GONE
        } else {
            curVideoId = item.id.videoId
            mPlayer?.cueVideo(item.id.videoId.toString(), 0f)
            player_view.visibility = View.VISIBLE
        }
    }

    fun insertMovieList() {
        var api = YoutubeApi()
        var fullData : MovieDTO? = null
        var itemList : ArrayList<ItemList> = arrayListOf()
        var people : ArrayList<RankDTO> = arrayListOf()
        firestore?.collection("people")?.orderBy("count", Query.Direction.DESCENDING)?.limit(10)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                people.add(person)
            }

            people.add(RankDTO("","미스트롯2",0,"",0,0,0,0,null))
            var i = 10
            for (person in people) {
                if (i == 0)
                    i = 30

                api.q = person.name
                //api.q += "노래"
                /*if (person.name.equals("양지원"))
                    api.q += "tv"*/
                when (person.name) {
                    "김수빈" -> api.q += " 트로트"
                    "김다현" -> api.q += " 트로트"
                    "명지" -> api.q += " 트로트"
                    //"이찬원" -> api.q += " 공식 채널"
                    //"김희재" -> api.q += " HEEJAE"
                    //"정동원" -> api.q += "tv"
                    //"안성훈" -> api.q += "tv"
                    //"장민호" -> api.q += " 공식 채널"
                    //"영탁" -> api.q += "의 불쑥tv"
                    //"신성" -> api.q += " 트로트 노래"
                    //"최대성" -> api.q += " 트로트 노래"
                    //"양지원" -> api.q += " tv"
                    //"김중연" -> api.q += " tv"
                    else -> " 노래"
                }

                api.maxResults = i.toString()

                val url = api.getUrl()
                println(url)
                val request = Request.Builder().url(url).build()

                var client = OkHttpClient()
                client.newCall(request).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    override fun onResponse(call: Call, response: Response) {
                        val body = response?.body?.string()
                        val gson = GsonBuilder().create()

                        //println(body)

                        fullData = gson.fromJson(body, MovieDTO::class.java)
                        for (it in fullData!!.items) {
                            itemList.add(it)
                        }

                        println("데이터 입력")
                        fullData?.items = itemList
                        var body2 = gson.toJson(fullData)

                        println(itemList)
                        println(fullData)
                        println(body2)

                        val data = hashMapOf("body" to body2)
                        firestore?.collection("json")?.document("popular_list")?.set(data, SetOptions.merge())

                        getActivity()?.runOnUiThread {

                        }


                    }
                })

                SystemClock.sleep(1000)
                i--;
            }

            Toast.makeText(getActivity(),"업로드 완료", Toast.LENGTH_SHORT).show()
        }
            ?.addOnFailureListener { exception ->

            }


        /*var names = arrayOf("양지원","영탁","이찬원","김희재","임영웅","김중연","안성훈","류지광","김호중","정동원","미스터트롯")
        //var names = arrayOf("양지원","영탁")
        var i = 10

        for (name in names) {
            if (i == 0)
                i = 10

            api.q = name
            api.maxResults = i.toString()

            val url = api.getUrl()
            println(url)
            val request = Request.Builder().url(url).build()

            var client = OkHttpClient()
            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response?.body?.string()
                    val gson = GsonBuilder().create()

                    //println(body)

                    fullData = gson.fromJson(body, MovieDTO::class.java)
                    for (it in fullData!!.items) {
                        itemList.add(it)
                    }

                    println("데이터 입력")
                    fullData?.items = itemList
                    val gson2 = GsonBuilder().create()
                    var body2 = gson2.toJson(fullData)

                    println(itemList)
                    println(fullData)
                    println(body2)

                    val data = hashMapOf("body" to body2)
                    firestore?.collection("json")?.document("popular_list2")?.set(data, SetOptions.merge())

                    getActivity()?.runOnUiThread {

                    }


                }
            })

            SystemClock.sleep(1000)
            i--;
        }*/



    }

    fun loadMovieList(youtubeApi: YoutubeApi) {
        val gson = GsonBuilder().create()
        val fullData = gson.fromJson(jsonbody, MovieDTO::class.java)
        movieList = fullData.items as ArrayList<ItemList>

        getActivity()?.runOnUiThread {
            setAdapter()
        }

        /*val url = youtubeApi.getUrl()
        val request = Request.Builder().url(url).build()

        var client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val body = response?.body?.string()
                val gson = GsonBuilder().create()

                val fullData = gson.fromJson(jsonbody, MovieDTO::class.java)
                movieList = fullData.items as ArrayList<ItemList>

                getActivity()?.runOnUiThread {
                    setAdapter()
                }

            }
        })*/
    }

    fun searchCtrlShow(show: Boolean) {
        if (show) {
            layout_search_movie.visibility = View.VISIBLE
        } else {
            layout_search_movie.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // 재개
    }

    override fun onPause() {
        super.onPause()
        // 중지

        mPlayer?.pause()
    }
}
