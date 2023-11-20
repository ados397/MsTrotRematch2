package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.UrlDialogBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class UrlDialog(context: Context) : Dialog(context), View.OnClickListener {

    lateinit var binding: UrlDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UrlDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        binding.buttonOk.setOnClickListener {

        }

        binding.buttonPreview.setOnClickListener {
            Glide.with(binding.imgPreview.context).load(binding.editUrl.text.toString()).apply(
                RequestOptions().optionalFitCenter()).into(binding.imgPreview)
        }
    }

    private fun init() {
        binding.buttonOk.setOnClickListener(this)

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