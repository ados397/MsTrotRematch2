package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.event_dialog.button_ok
import kotlinx.android.synthetic.main.url_dialog.*

class UrlDialog(context: Context) : Dialog(context), View.OnClickListener {

    private val layout = R.layout.url_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        init()

        button_ok.setOnClickListener {

        }

        button_preview.setOnClickListener {
            Glide.with(img_preview.context).load(edit_url.text.toString()).apply(
                RequestOptions().fitCenter()).into(img_preview)
        }
    }

    private fun init() {
        button_ok.setOnClickListener(this)

        //text_msg.text = event.title?.replace("\\n","\n")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }
    }
}