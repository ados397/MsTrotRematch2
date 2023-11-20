package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.ados.mstrotrematch2.databinding.NoticeSubDialogBinding
import com.ados.mstrotrematch2.model.NoticeDTO
import com.bumptech.glide.Glide

class NoticeSubDialog(context: Context) : Dialog(context), View.OnClickListener {
    lateinit var binding: NoticeSubDialogBinding

    var noticeDTO: NoticeDTO? = null
    var isStopToday = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NoticeSubDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textContent.movementMethod = ScrollingMovementMethod.getInstance()
        //text_notice_link.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        if (noticeDTO != null) {
            binding.textTitle.text = noticeDTO?.title
            //text_time.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(noticeDTO?.time!!)
            binding.textContent.text = noticeDTO?.content?.replace("\\n","\n")

            if (!noticeDTO?.imageUrl.isNullOrEmpty()) {
                Glide.with(context).load(noticeDTO?.imageUrl).optionalFitCenter().into(binding.imgNotice)
                binding.imgNotice.visibility = View.VISIBLE
            } else {
                binding.imgNotice.visibility = View.GONE
            }

            binding.imgNotice.setOnClickListener {
                callStore()
            }
            binding.buttonNoticeLink.setOnClickListener {
                callStore()
            }

            binding.checkboxStopToday.setOnCheckedChangeListener { compoundButton, b ->
                isStopToday = b
            }
        }
    }

    private fun callStore() {
        val linePackage = noticeDTO?.packageName.toString()
        val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$linePackage")) // 설치 링크를 인텐트에 담아
        context.startActivity(intentPlayStore) // 플레이스토어로 이동
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