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
import com.ados.mstrotrematch2.databinding.CongratulateDialogBinding
import com.ados.mstrotrematch2.model.NoticeDTO
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat

class CongratulateDialog(context: Context) : Dialog(context), View.OnClickListener {
    lateinit var binding: CongratulateDialogBinding

    var noticeDTO: NoticeDTO? = null
    var rankDTO: RankDTO? = null
    var isStopToday = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CongratulateDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textContent.movementMethod = ScrollingMovementMethod.getInstance()
        //text_notice_link.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        if (rankDTO != null) {
            binding.textTitle.text = "🎉 생일 축하합니다!! 🎂"
            //text_time.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(noticeDTO?.time!!)
            binding.textContent.text = "${SimpleDateFormat("yyyyMMdd").format(rankDTO?.birthday)} ${rankDTO?.name} 가수님\n생일을 진심으로 축하 드립니다❤️"

            var imageID = context.resources.getIdentifier(rankDTO?.image, "drawable", context.packageName)
            if (imageID > 0) {
                //image?.setImageResource(imageID)

                Glide.with(binding.imgNotice.context)
                    .asBitmap()
                    .load(imageID) ///feed in path of the image
                    .optionalFitCenter()
                    .into(binding.imgNotice)
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