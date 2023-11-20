package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.ImageViewDialogBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class ImageViewDialog(context: Context) : Dialog(context) {

    lateinit var binding: ImageViewDialogBinding
    private val layout = R.layout.image_view_dialog

    var imageUri: Uri? = null
    var imageID: Int? = null
    var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImageViewDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setInfo()

        binding.layoutMain.setOnClickListener {
            dismiss()
        }

        binding.imageView.setOnClickListener {
            dismiss()
        }
    }

    fun setInfo() {
        if (imageUri != null) {
            Glide.with(context).load(imageUri).optionalFitCenter().into(binding.imageView)
        } else if (imageID != null) {
            binding.imageView.setImageResource(imageID!!)
        } else if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context).load(imageUrl).optionalFitCenter().into(binding.imageView)
        }
        //binding.imageView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        //binding.imageView.requestLayout()
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }
}