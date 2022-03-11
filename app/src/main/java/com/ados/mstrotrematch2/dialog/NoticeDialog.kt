package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.RecyclerViewAdapterNotice
import com.ados.mstrotrematch2.model.NewsDTO
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.notice_dialog.recyclerview_notice

class NoticeDialog(context: Context) : Dialog(context), View.OnClickListener {

    var firestore : FirebaseFirestore? = null
    private val layout = R.layout.notice_dialog
    //lateinit var recyclerView : RecyclerView
    var lastVisible : DocumentSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        recyclerview_notice.layoutManager = LinearLayoutManager(context)

        var news : ArrayList<NewsDTO> = arrayListOf()
        firestore = FirebaseFirestore.getInstance()
        refreshData(news)
        /*firestore?.collection("news")?.orderBy("time", Query.Direction.DESCENDING)?.limit(20)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            news.clear()
            if(querySnapshot == null)return@addSnapshotListener

            // document 수만큼 획득
            for(snapshot in querySnapshot){
                var new = snapshot.toObject(NewsDTO::class.java)!!
                news.add(new)
                //println("${new.title}, ${new.time}, ${new.content}")
            }

            recyclerview_notice.adapter = RecyclerViewAdapterNotice(news)
        }*/

        recyclerview_notice.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
            override fun onScrolled(recyclerView1: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView1, dx, dy)

                if (!recyclerview_notice.canScrollVertically(1)) {
                    if (news.size >= 10) {

                        refreshData(news)
                    }
                }
            }
        })

        init()

    }

    fun refreshData(news : ArrayList<NewsDTO>) {
        if (lastVisible == null) {
            firestore?.collection("news")?.orderBy("time", Query.Direction.DESCENDING)?.limit(10)?.get()?.addOnSuccessListener { result ->
            //firestore?.collection(cheeringboardCollectionName)?.orderBy(field, Query.Direction.DESCENDING)?.limit(30)?.get()?.addOnSuccessListener { result ->
                news.clear()
                for (document in result) {
                    var person = document.toObject(NewsDTO::class.java)!!
                    news.add(person)
                    lastVisible = result.documents.get(result.size() - 1)
                }
                if (result.size() > 0) {
                    recyclerview_notice.adapter = RecyclerViewAdapterNotice(news)
                }
            }?.addOnFailureListener { exception ->

            }
        } else {
            firestore?.collection("news")?.orderBy("time", Query.Direction.DESCENDING)?.startAfter(lastVisible!!)?.limit(10)?.get()?.addOnSuccessListener { result ->
            //firestore?.collection(cheeringboardCollectionName)?.orderBy(field, Query.Direction.DESCENDING)?.startAfter(lastVisible!!)?.limit(30)?.get()?.addOnSuccessListener { result ->
                //posts.clear()
                for (document in result) {
                    var board = document.toObject(NewsDTO::class.java)!!
                    news.add(board)
                    lastVisible = result.documents.get(result.size() - 1)
                }
                //recyclerView.adapter = RecyclerViewAdapterCheering(posts, this)
                if (result.size() > 0) {
                    recyclerview_notice.adapter?.notifyItemInserted(news.size)
                }
            }?.addOnFailureListener { exception ->

            }
        }
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }
}