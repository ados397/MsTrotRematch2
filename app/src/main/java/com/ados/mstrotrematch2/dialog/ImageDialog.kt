package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.BoardDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.board_dialog.*

class ImageDialog(context: Context, val item: BoardDTO) : Dialog(context), View.OnClickListener {

    private val layout = R.layout.image_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

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
                RequestOptions().fitCenter()).into(img_profile)
        }

        img_profile.setOnClickListener {
            
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