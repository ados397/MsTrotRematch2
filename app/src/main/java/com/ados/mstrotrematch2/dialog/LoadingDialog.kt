package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.ados.mstrotrematch2.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.loading_dialog.*

class LoadingDialog(context: Context) : Dialog(context) {

    private val layout = R.layout.loading_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        Glide.with(context).load(R.raw.loading).into(img_loading)
    }
}