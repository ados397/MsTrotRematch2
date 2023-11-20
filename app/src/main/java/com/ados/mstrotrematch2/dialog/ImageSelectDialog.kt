package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.ados.mstrotrematch2.adapter.OnImageSelectItemClickListener
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterImageSelect
import com.ados.mstrotrematch2.databinding.ImageSelectDialogBinding
import com.ados.mstrotrematch2.model.RankDTO
import com.ados.mstrotrematch2.model.RankExDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ImageSelectDialog(context: Context) : Dialog(context), View.OnClickListener,
    OnImageSelectItemClickListener {

    lateinit var binding: ImageSelectDialogBinding
    var imgName = ""
    var imgUrl = ""
    var peopleName = ""
    var peopleDocName = ""
    var peopleExDTOs : ArrayList<RankExDTO> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImageSelectDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.recyclerviewImg.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerviewImg.adapter = RecyclerViewAdapterImageSelect(peopleExDTOs, this)

        binding.buttonUrl.setOnClickListener {
            val dialog = UrlDialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.binding.buttonCancelUrl.setOnClickListener { // No
                dialog.dismiss()
            }

            dialog.binding.buttonOk.setOnClickListener { // Yes
                var url = dialog.binding.editUrl.text.toString().replace(" ","")
                if (url.isNullOrEmpty()) {
                    Toast.makeText(context, "URL을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (!url.toUpperCase().contains("HTTP")) {
                    Toast.makeText(context, "URL이 잘못 입력되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    imgName = ""
                    imgUrl = dialog.binding.editUrl.text.toString()
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

    override fun onItemClick(item: RankExDTO) {
        imgName = item.rankDTO?.image.toString()
        peopleName = item.rankDTO?.name.toString()
        peopleDocName = item.rankDTO?.docname.toString()
        dismiss()
    }

}