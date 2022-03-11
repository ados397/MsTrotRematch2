package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.BoardDTO
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.donation_status_dialog.*
import kotlinx.android.synthetic.main.donation_status_dialog.img_profile
import kotlinx.android.synthetic.main.donation_status_dialog.text_name
import kotlinx.android.synthetic.main.image_dialog.*
import java.text.DecimalFormat

class DonationStatusDialog(context: Context, val item: RankDTO, val rank: Int) : Dialog(context), View.OnClickListener {

    private val layout = R.layout.donation_status_dialog
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        init()

        var image = BoardDTO(image = item.image)
        img_profile.setOnClickListener {
            val dialog = ImageDialog(context, image)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
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

        text_name.text = "[ ${item.name} ] 가수님"

        var donation = item.count?.div(1000000)?.times(10)
        var next_donation = donation?.plus(10)
        var next_count = next_donation?.times(100000)?.minus(item.count!!)

        text_rank2.text = "${rank}위"
        text_count2.text = "${decimalFormat.format(item.count)}"
        text_donation2.text = "${decimalFormat.format(donation)}만원"
        text_next_count1.text = "${decimalFormat.format(next_donation)}만원"
        text_next_count3.text = "${decimalFormat.format(next_count)}"
        text_donation_desc.text = "( ${donation?.times(10)}만표 달성 )"

        if (donation!! <= 0) {
            text_donation_desc.visibility = View.GONE
        }

        if (rank == 1) {
            //img_crown.setImageResource(R.drawable.crown_gold)
            Glide.with(img_crown.context)
                .asBitmap()
                .load(R.drawable.crown_gold) ///feed in path of the image
                .fitCenter()
                .into(img_crown)
            text_last_donation.text = "이대로 [금 왕관] 확정 시 30만원 추가 적립"
        } else if (rank == 2) {
            //img_crown.setImageResource(R.drawable.crown_silver)
            Glide.with(img_crown.context)
                .asBitmap()
                .load(R.drawable.crown_silver) ///feed in path of the image
                .fitCenter()
                .into(img_crown)
            text_last_donation.text = "이대로 [은 왕관] 확정 시 20만원 추가 적립"
        } else if (rank == 3) {
            //img_crown.setImageResource(R.drawable.crown_bronze)
            Glide.with(img_crown.context)
                .asBitmap()
                .load(R.drawable.crown_bronze) ///feed in path of the image
                .fitCenter()
                .into(img_crown)
            text_last_donation.text = "이대로 [동 왕관] 확정 시 10만원 추가 적립"
        } else {
            img_crown.visibility = View.GONE
            text_last_donation.visibility = View.GONE
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