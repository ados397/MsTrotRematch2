package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.question_dialog_vote.*
import java.text.DecimalFormat

class QuestionDialogVote(context: Context, val item: RankDTO, val ticketcount: Int, val questCount: Int, val questMaxCount: Int) : Dialog(context), View.OnClickListener {

    private val layout = R.layout.question_dialog_vote
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        init()

        button_minus.setOnClickListener {
            var count = text_input_count.text.toString().toInt()
            if (count > 1) {
                text_input_count.text = "${count.minus(1)}"
            }
        }

        button_plus.setOnClickListener {
            var count = text_input_count.text.toString().toInt()
            if (count < ticketcount) {
                text_input_count.text = "${count.plus(1)}"
            }
        }

        button_max.setOnClickListener {
            text_input_count.text = "$ticketcount"
        }
    }

    private fun init() {
        button_ok.setOnClickListener(this)

        var imageID = context.resources.getIdentifier(item.image, "drawable", context.packageName)
        //img_profile.setImageResource(imageID)
        Glide.with(img_profile.context)
            .asBitmap()
            .load(imageID) ///feed in path of the image
            .fitCenter()
            .into(img_profile)
        Glide.with(img_lock.context)
            .asBitmap()
            .load(R.drawable.lock) ///feed in path of the image
            .fitCenter()
            .into(img_lock)

        text_msg1.text = "${item.name} 님에게"
        text_count.text = "현재 득표수 : ${decimalFormat.format(item.count)}"

        var count = questMaxCount - questCount
        if (count <= 0) {
            layout_input_desc.visibility = View.GONE
            layout_multi_count.visibility = View.VISIBLE
        } else {
            text_input_desc.text = "오늘 광고를 $count 회 더 보면 다중 투표가 활성화 됩니다."
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }
    }
}