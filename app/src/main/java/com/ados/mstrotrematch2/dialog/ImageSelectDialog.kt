package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.ados.mstrotrematch2.OnImageSelectItemClickListener
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.RecyclerViewAdapterImageSelect
import com.ados.mstrotrematch2.model.RankDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.image_select_dialog.*
import kotlinx.android.synthetic.main.url_dialog.*


class ImageSelectDialog(context: Context) : Dialog(context), View.OnClickListener,
    OnImageSelectItemClickListener {

    private val layout = R.layout.image_select_dialog
    var img_name = ""
    var img_url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        recyclerview_img.layoutManager = GridLayoutManager(context, 3)

        var people : ArrayList<RankDTO> = arrayListOf()
        var firestore = FirebaseFirestore.getInstance()
        firestore?.collection("people")?.orderBy("name", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                people.add(person)
            }
            recyclerview_img.adapter = RecyclerViewAdapterImageSelect(people, this)
        }
            ?.addOnFailureListener { exception ->

            }

        //recyclerview_img.adapter = RecyclerViewAdapterImageSelect(this)

        button_url.setOnClickListener {
            val dialog = UrlDialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel_url.setOnClickListener { // No
                dialog.dismiss()
            }

            dialog.button_ok.setOnClickListener { // Yes
                var url = dialog.edit_url.text.toString().replace(" ","")
                if (url.length == 0) {
                    Toast.makeText(context, "URL을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (url.toUpperCase().contains("HTTP") == false) {
                    Toast.makeText(context, "URL이 잘못 입력되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    img_name = ""
                    img_url = dialog.edit_url.text.toString()
                    dialog.dismiss()
                    dismiss()
                }
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

    override fun onItemClick(item: RankDTO) {
        img_name = item.image.toString()
        dismiss()
    }

}