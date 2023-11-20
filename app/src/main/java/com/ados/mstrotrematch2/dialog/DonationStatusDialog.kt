package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.DonationStatusDialogBinding
import com.ados.mstrotrematch2.model.RankDTO
import com.ados.mstrotrematch2.model.UserDTO
import com.ados.mstrotrematch2.util.Utility
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class DonationStatusDialog(context: Context, val item: RankDTO, val rank: Int, val voteCount: Long) : Dialog(context), View.OnClickListener {

    lateinit var binding: DonationStatusDialogBinding
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DonationStatusDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        init()

        binding.imgProfile.setOnClickListener {
            val imageID = context.resources.getIdentifier(item.image, "drawable", context.packageName)
            val dialog = ImageViewDialog(context)
            dialog.imageID = imageID
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.show()
        }
    }

    private fun init() {
        binding.buttonOk.setOnClickListener(this)

        var imageID = context.resources.getIdentifier(item.image, "drawable", context.packageName)
        //img_profile.setImageResource(imageID)
        Glide.with(binding.imgProfile.context)
            .asBitmap()
            .load(imageID) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgProfile)

        binding.textName.text = "[ ${item.name} ] 가수님"

        var donation = item.count?.div(1000000)?.times(10)!!
        var nextDonation = donation.plus(10)
        var nextCount = nextDonation.times(100000).minus(item.count!!)

        binding.textRank2.text = "${rank}위"
        binding.textCount2.text = "${decimalFormat.format(item.count)}"
        binding.textDonation2.text = "${Utility.getNumKorString(donation.times(10000))}원"
        binding.textNextCount1.text = "${Utility.getNumKorString(nextDonation.times(10000))}원"
        binding.textNextCount3.text = "${decimalFormat.format(nextCount)}"
        binding.textDonationDesc.text = "⭐${Utility.getNumKorString(donation.times(100000))}표 달성⭐"

        if (voteCount > 0) {
            binding.layoutMyVote.visibility = View.VISIBLE
            binding.textMyCount2.text = "${decimalFormat.format(voteCount)}"
        } else {
            binding.layoutMyVote.visibility = View.GONE
        }

        if (donation!! <= 0) {
            binding.textDonationDesc.visibility = View.GONE
        }

        when (rank) {
            1 -> {
                //img_crown.setImageResource(R.drawable.crown_gold)
                Glide.with(binding.imgCrown.context)
                    .asBitmap()
                    .load(R.drawable.crown_gold) ///feed in path of the image
                    .optionalFitCenter()
                    .into(binding.imgCrown)
                binding.textLastDonation.text = "TOP3 확정 시 100만표 당 만원씩 추가 적립"
                binding.imgLineTop.setImageResource(R.drawable.gold_line_top)
            }
            2 -> {
                //img_crown.setImageResource(R.drawable.crown_silver)
                Glide.with(binding.imgCrown.context)
                    .asBitmap()
                    .load(R.drawable.crown_silver) ///feed in path of the image
                    .optionalFitCenter()
                    .into(binding.imgCrown)
                binding.textLastDonation.text = "TOP3 확정 시 100만표 당 만원씩 추가 적립"
                binding.imgLineTop.setImageResource(R.drawable.red_line_top)
            }
            3 -> {
                //img_crown.setImageResource(R.drawable.crown_bronze)
                Glide.with(binding.imgCrown.context)
                    .asBitmap()
                    .load(R.drawable.crown_bronze) ///feed in path of the image
                    .optionalFitCenter()
                    .into(binding.imgCrown)
                binding.textLastDonation.text = "TOP3 확정 시 100만표 당 만원씩 추가 적립"
                binding.imgLineTop.setImageResource(R.drawable.blue_line_top)
            }
            else -> {
                binding.textLastDonation.visibility = View.GONE
                binding.imgCrown.visibility = View.GONE
                binding.imgLineTop.visibility = View.GONE
            }
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