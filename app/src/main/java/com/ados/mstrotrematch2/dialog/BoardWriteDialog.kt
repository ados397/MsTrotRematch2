package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.BoardDTO
import com.ados.mstrotrematch2.model.SeasonDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.board_dialog.button_cancel
import kotlinx.android.synthetic.main.board_write_dialog.*
import kotlinx.android.synthetic.main.board_write_dialog.button_write
import java.text.SimpleDateFormat
import java.util.*

class BoardWriteDialog(context: Context) : Dialog(context), View.OnClickListener {

    private val layout = R.layout.board_write_dialog
    var firestore : FirebaseFirestore? = null
    lateinit var recyclerView : RecyclerView
    var imagename : String? = null
    var imageurl : String? = null
    var isWrite = false
    var cheeringboardCollectionName = "cheeringboard_s6" // 시즌 변경 작업

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

        Glide.with(img_select_profile.context)
            .asBitmap()
            .load(R.drawable.selectimg) ///feed in path of the image
            .fitCenter()
            .into(img_select_profile)

        img_select_profile.setOnClickListener {
            val dialog = ImageSelectDialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
            }

            dialog.setOnDismissListener {
                if (dialog.img_url.isNotEmpty()) {
                    imageurl = dialog.img_url
                    Glide.with(img_select_profile.context).load(dialog.img_url).apply(
                        RequestOptions().centerCrop()).into(img_select_profile)
                } else if (dialog.img_name.isNotEmpty()) {
                    imagename = dialog.img_name
                    var imageID = context.resources.getIdentifier(imagename, "drawable", context.packageName)
                    //img_select_profile?.setImageResource(imageID)
                    Glide.with(img_select_profile.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .fitCenter()
                        .into(img_select_profile)
                }
            }
        }

        button_write.setOnClickListener {
            writePost()
        }
    }

    fun isValidPassword(id: String): Boolean {
        val reg = Regex("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9!@#\$%^&*]).{6,30}\$")
        if(!id.matches(reg)) return false

        return true
    }

    fun  writePost() {
        var passWord = edit_password.text.toString().replace(" ","")

        if (edit_title.text.toString().replace(" ","").length == 0) {
            Toast.makeText(context,"제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        } else if (edit_name.text.toString().replace(" ","").length == 0) {
            Toast.makeText(context,"이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        } else if (passWord.length == 0) {
            Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        } else if (isValidPassword(passWord) == false) {
            Toast.makeText(context,"비밀번호는 6자 이상 숫자, 문자, 특수문자 중 2가지가 포함되어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        } else if (edit_content.text.toString().replace(" ","").length == 0) {
            Toast.makeText(context,"내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        } else if (imagename.isNullOrEmpty() and imageurl.isNullOrEmpty()) {
            Toast.makeText(context,"이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val alphabat = arrayOf("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z")
        val rand_word = alphabat.get(Random().nextInt(26))

        var post = BoardDTO()
        post.docname = "$rand_word${System.currentTimeMillis()}"
        post.image = imagename
        post.imageUrl = imageurl
        post.title = edit_title.text.toString()
        post.content = edit_content.text.toString()
        post.name = edit_name.text.toString()
        post.time = Date()
        post.password = edit_password.text.toString()

        var tsDoc = firestore!!.collection(cheeringboardCollectionName)?.document(post.docname.toString())
        firestore?.runTransaction { transaction ->
            transaction.set(tsDoc, post)
        }

        // 글쓴날짜 기록
        var pref = PreferenceManager.getDefaultSharedPreferences(context)
        var editor = pref.edit()
        editor.putString("WriteCheering", SimpleDateFormat("yyyy-MM-dd").format(Date())).apply()


        isWrite = true
        dismiss()
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