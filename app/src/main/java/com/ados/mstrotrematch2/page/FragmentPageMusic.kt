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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.adapter.OnMusicItemClickListener
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterMusic
import com.ados.mstrotrematch2.databinding.FragmentPageMusicBinding
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
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
import okhttp3.*
import java.io.IOException


class FragmentPageMusic : Fragment(), OnMusicItemClickListener {
    private var _binding: FragmentPageMusicBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var movieList : ArrayList<ItemList> = arrayListOf()
    lateinit var recyclerView : RecyclerView
    var mPlayer: YouTubePlayer? = null
    var curVideoId: String? = null
    var youtubeApi = YoutubeApi()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPageMusicBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.recyclerview_music!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        firebaseViewModel.getMovieList()
        firebaseViewModel.jsonMovie.observe(viewLifecycleOwner) {
            loadMovieList(youtubeApi)
        }

        return rootView
    }

    private fun setAdapter() {
        recyclerView.adapter = RecyclerViewAdapterMusic(movieList, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //tv_fragment_name.text = "노래듣기"

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.playerView.getPlayerUiController().showFullscreenButton(true)
        binding.playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                mPlayer = youTubePlayer
                val videoId = "uAHaUXV6_vE"
                youTubePlayer.cueVideo(videoId, 0f)

                curVideoId = ""
            }
        })

        binding.playerView.getPlayerUiController().setFullScreenButtonClickListener(View.OnClickListener {
            if (binding.playerView.isFullScreen()) {
                binding.playerView.exitFullScreen()
                requireActivity().window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                // Show Tabs
                (activity as MainActivity?)!!.showMainCtrl(true)
                //searchCtrlShow(true)

                //세로 화면으로 고정
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            } else {
                binding.playerView.enterFullScreen()
                requireActivity().window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                // Hide Tabs
                (activity as MainActivity?)!!.showMainCtrl(false)
                //searchCtrlShow(false)

                // 고정 풀고 센서에 따라 화면 전환 모드
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        })

        binding.buttonSearch.setOnClickListener {
            youtubeApi.q = binding.editSearchMovie.text.toString()
            loadMovieList(youtubeApi)
        }

        binding.editSearchMovie.setOnKeyListener { v, keyCode, event ->
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) {
                youtubeApi.q = binding.editSearchMovie.text.toString()
                loadMovieList(youtubeApi)
            }
            false
        }

        binding.buttonInsertMove.visibility = View.GONE // 관리자모드
        binding.buttonInsertMove.setOnClickListener {
            Toast.makeText(requireActivity(),"업로드 시작", Toast.LENGTH_SHORT).show()
            insertMovieList()
        }
    }

    override fun onItemClick(item: ItemList, position: Int) {
        if (curVideoId == item.id.videoId) { // 동일한 비디오 한 번 더 클릭하면 플레이어 닫기
            curVideoId = ""
            binding.playerView.visibility = View.GONE
        } else {
            curVideoId = item.id.videoId
            mPlayer?.cueVideo(item.id.videoId.toString(), 0f)
            binding.playerView.visibility = View.VISIBLE
        }
    }

    private fun insertMovieList() {
        var api = YoutubeApi()
        //api.order = "viewCount"
        var fullData : MovieDTO? = null
        var itemList : ArrayList<ItemList> = arrayListOf()
        var people : ArrayList<RankDTO> = arrayListOf()
        var firestore = FirebaseFirestore.getInstance()
        firestore.collection("people").orderBy("count", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                people.add(person)
            }

            people.add(RankDTO("","미스터트롯",0,"",0,0,0,0,null))
            var i = 10
            for (person in people) {
                if (i == 0)
                    i = 30

                api.q = person.name
                api.channelId = null
                //api.q += "노래"
                /*if (person.name.equals("양지원"))
                    api.q += "tv"*/
                when (person.name) {
                    "임영웅" -> {
                        //api.q += " 공식 채널"
                        api.channelId = "UC3WZlO2Zl8NE1yIUgtwUtQw"
                    }
                    "이찬원" -> {
                        //api.q += " 클린"
                        api.channelId = "UC4UnP3v-iaFaLdtKwp84Pmw"
                    }
                    "김희재" -> api.q += " HEEJAE"
                    "정동원" -> {
                        api.q += "tv"
                        api.channelId = "UCrLQ0ovys23H9xBV6U-Sd4A"
                    }
                    "안성훈" -> api.q += "tv"
                    "장민호" -> api.q += " 고화질"
                    "영탁" -> {
                        //api.q += "의 불쑥tv"
                        api.channelId = "UCH7JoVNZFpo1pOzZH-t5uew"
                    }
                    "신성" -> api.q += " 트로트 노래"
                    "최대성" -> api.q += " 트로트 노래"
                    "양지원" -> api.q += " tv"
                    "김중연" -> api.q += " tv"
                    "김수찬" -> {
                        //api.q += " tv"
                        api.channelId = "UCgLn4rH3Ey9OWSd88-HMUyQtoRegex"
                    }
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
                        firestore.collection("json")?.document("popular_list")?.set(data, SetOptions.merge())

                        requireActivity().runOnUiThread {

                        }


                    }
                })

                SystemClock.sleep(1000)
                i--;
            }

            Toast.makeText(requireActivity(),"업로드 완료", Toast.LENGTH_SHORT).show()
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

                    requireActivity().runOnUiThread {

                    }


                }
            })

            SystemClock.sleep(1000)
            i--;
        }*/



    }

    private fun loadMovieList(youtubeApi: YoutubeApi) {
        val gson = GsonBuilder().create()
        val fullData = gson.fromJson(firebaseViewModel.jsonMovie.value, MovieDTO::class.java)
        movieList = fullData.items as ArrayList<ItemList>

        requireActivity().runOnUiThread {
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

                requireActivity().runOnUiThread {
                    setAdapter()
                }

            }
        })*/
    }

    fun searchCtrlShow(show: Boolean) {
        if (show) {
            binding.layoutSearchMovie.visibility = View.VISIBLE
        } else {
            binding.layoutSearchMovie.visibility = View.GONE
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
