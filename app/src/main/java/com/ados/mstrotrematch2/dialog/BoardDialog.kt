package com.ados.mstrotrematch2.dialog


import android.graphics.Color
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.DatabaseHelper
import com.ados.mstrotrematch2.model.BoardDTO
import com.ados.mstrotrematch2.model.SeasonDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.board_dialog.*
import kotlinx.android.synthetic.main.board_dialog.button_cancel
import kotlinx.android.synthetic.main.delete_dialog.*
import java.text.SimpleDateFormat

class BoardDialog(context: Context, val item: BoardDTO, val parentActivity: Activity) : Dialog(context), View.OnClickListener {

    var dbHandler : DatabaseHelper? = null
    var firestore : FirebaseFirestore? = null
    private val layout = R.layout.board_dialog
    lateinit var recyclerView : RecyclerView
    var cheeringboardCollectionName = "cheeringboard_s6" // 시즌 변경 작업
    var isReport = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        firestore = FirebaseFirestore.getInstance()

        firestore?.collection("preferences")?.document("season")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var seasonDTO = documentSnapshot?.toObject(SeasonDTO::class.java)

            // 시즌 변경 작업
            //seasonDTO?.seasonNum = 6
            if (seasonDTO?.seasonNum == 5) {
                cheeringboardCollectionName = "cheeringboard_s5"
            }
        }

        if (item.isBlock) {
            text_title.text = "차단되었습니다."
            text_content.text = "내가 신고한 응원글 또는 사용자 입니다."
            text_title.setTextColor(Color.parseColor("#CCCCCC"))
            text_content.setTextColor(Color.parseColor("#CCCCCC"))
            button_report.visibility = View.GONE
        } else {
            text_title.text = item.title
            text_content.text = item.content
        }

        text_name.text = item.name
        text_content.movementMethod = LinkMovementMethod()
        text_time.text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.time)
        text_like_count.text = item.likeCount.toString()
        text_dislike_count.text = item.dislikeCount.toString()

        Glide.with(img_like.context)
            .asBitmap()
            .load(R.drawable.like) ///feed in path of the image
            .fitCenter()
            .into(img_like)
        Glide.with(img_dislike.context)
            .asBitmap()
            .load(R.drawable.dislike) ///feed in path of the image
            .fitCenter()
            .into(img_dislike)

        if (item.imageUrl.isNullOrEmpty()) {
            var imageID = context.resources.getIdentifier(item.image, "drawable", context.packageName)
            //img_profile.setImageResource(imageID)
            Glide.with(img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(img_profile)
        } else {
            Glide.with(img_profile.context).load(item.imageUrl).apply(
                RequestOptions().centerCrop()).into(img_profile)
        }

        dbHandler = DatabaseHelper(context)
        // 좋아요, 싫어요 했으면 밑줄 표시
        if (dbHandler?.getlike(item.docname.toString()) == true) {
            text_like_count.paintFlags = text_like_count.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
        if (dbHandler?.getdislike(item.docname.toString()) == true) {
            text_dislike_count.paintFlags = text_dislike_count.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        img_profile.setOnClickListener {
            val dialog = ImageDialog(context, item)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
        }

        // 좋아요 클릭
        img_like.setOnClickListener {
            clickLike()
        }
        text_like_count.setOnClickListener {
            clickLike()
        }

        // 싫어요 클릭
        text_dislike_count.visibility = View.GONE
        img_dislike.visibility = View.GONE
        img_dislike.setOnClickListener {
            clickDislike()
        }
        text_dislike_count.setOnClickListener {
            clickDislike()
        }

        button_delete.setOnClickListener {
            val dialog = DeleteDialog(context, item)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
            }

            dialog.button_ok.setOnClickListener { // Yes
                if (item.password == dialog.edit_password.text.toString()) {
                    var firestore = FirebaseFirestore.getInstance()
                    firestore.collection(cheeringboardCollectionName).document(item.docname.toString()).delete()

                    Toast.makeText(context,"삭제되었습니다. 새로고침 하세요.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    dismiss()
                } else {
                    Toast.makeText(context,"비밀번호가 맞지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            // 관리자 삭제 기능
            dialog.button_admin_delete.setOnClickListener { // Yes
                var firestore = FirebaseFirestore.getInstance()
                firestore.collection(cheeringboardCollectionName).document(item.docname.toString()).delete()

                Toast.makeText(context,"삭제되었습니다. 새로고침 하세요.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                dismiss()
            }
        }

        button_report.setOnClickListener {
            val dialog = ReportDialog(context, item, parentActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.setOnDismissListener {
                if (dialog.isReport == true) {
                    isReport = true
                    text_title.text = "차단되었습니다."
                    text_content.text = "내가 신고한 응원글 또는 사용자 입니다."
                    text_title.setTextColor(Color.parseColor("#CCCCCC"))
                    text_content.setTextColor(Color.parseColor("#CCCCCC"))
                    button_report.visibility = View.GONE
                }
            }
        }
    }

    fun clickLike() {
        var tsDoc = firestore?.collection(cheeringboardCollectionName)?.document(item.docname.toString())
        println("문서명 ${item.docname.toString()}")

        if (dbHandler?.getlike(item.docname.toString()) == false) {
            dbHandler?.updateLike(item.docname.toString(), 1)

            item.likeCount = item.likeCount!! + 1
            text_like_count.text = "${item.likeCount}"
            text_like_count.paintFlags = text_like_count.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            firestore?.runTransaction { transaction ->
                val boardDTO = transaction.get(tsDoc!!).toObject(BoardDTO::class.java)
                println("좋아요 수  ${boardDTO?.likeCount}")
                boardDTO?.likeCount = boardDTO?.likeCount?.plus(1)
                println("좋아요 수  ${boardDTO?.likeCount}")
                transaction.set(tsDoc, boardDTO!!)
            }

            Toast.makeText(context,"좋아요", Toast.LENGTH_SHORT).show()
        } else {
            dbHandler?.updateLike(item.docname.toString(), 0)

            item.likeCount = item.likeCount!! - 1
            text_like_count.text = "${item.likeCount}"
            text_like_count.paintFlags = Paint.ANTI_ALIAS_FLAG

            firestore?.runTransaction { transaction ->
                val boardDTO = transaction.get(tsDoc!!).toObject(BoardDTO::class.java)
                boardDTO?.likeCount = boardDTO?.likeCount?.minus(1)
                transaction.set(tsDoc, boardDTO!!)
            }

            Toast.makeText(context,"좋아요 취소", Toast.LENGTH_SHORT).show()
        }
        Thread.sleep(300L)
    }

    fun clickDislike() {
        var tsDoc = firestore?.collection(cheeringboardCollectionName)?.document(item.docname.toString())

        if (dbHandler?.getdislike(item.docname.toString()) == false) {
            dbHandler?.updateDislike(item.docname.toString(), 1)

            item.dislikeCount = item.dislikeCount!! + 1
            text_dislike_count.text = "${item.dislikeCount}"
            text_dislike_count.paintFlags = text_dislike_count.paintFlags or Paint.UNDERLINE_TEXT_FLAG


            firestore?.runTransaction { transaction ->
                val boardDTO = transaction.get(tsDoc!!).toObject(BoardDTO::class.java)
                boardDTO?.dislikeCount = boardDTO?.dislikeCount?.plus(1)
                transaction.set(tsDoc, boardDTO!!)
            }

            Toast.makeText(context,"싫어요", Toast.LENGTH_SHORT).show()
        } else {
            dbHandler?.updateDislike(item.docname.toString(), 0)

            item.dislikeCount = item.dislikeCount!! - 1
            text_dislike_count.text = "${item.dislikeCount}"
            text_dislike_count.paintFlags = Paint.ANTI_ALIAS_FLAG

            firestore?.runTransaction { transaction ->
                val boardDTO = transaction.get(tsDoc!!).toObject(BoardDTO::class.java)
                boardDTO?.dislikeCount = boardDTO?.dislikeCount?.minus(1)
                transaction.set(tsDoc, boardDTO!!)
            }

            Toast.makeText(context,"싫어요 취소", Toast.LENGTH_SHORT).show()
        }
        Thread.sleep(300L)
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