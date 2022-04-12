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
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.NoticeDTO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.notice_sub_dialog.*

class NoticeSubDialog(context: Context) : Dialog(context), View.OnClickListener {
    private val layout = R.layout.notice_sub_dialog
    var noticeDTO: NoticeDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        text_content.movementMethod = ScrollingMovementMethod.getInstance()
        //text_notice_link.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        if (noticeDTO != null) {
            text_title.text = noticeDTO?.title
            //text_time.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(noticeDTO?.time!!)
            text_content.text = noticeDTO?.content?.replace("\\n","\n")

            if (!noticeDTO?.imageUrl.isNullOrEmpty()) {
                Glide.with(context).load(noticeDTO?.imageUrl).fitCenter().into(img_notice)
                img_notice.visibility = View.VISIBLE
            } else {
                img_notice.visibility = View.GONE
            }

            img_notice.setOnClickListener {
                callStore()
            }
            button_notice_link.setOnClickListener {
                callStore()
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