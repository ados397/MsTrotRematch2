package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.QuestionDialogVoteBinding
import com.ados.mstrotrematch2.model.RankDTO
import com.ados.mstrotrematch2.model.RankExDTO
import com.ados.mstrotrematch2.model.UserDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class QuestionDialogVote(context: Context, var item: RankExDTO, val ticketCount: Int, private val questCount: Int, private val questMaxCount: Int) : Dialog(context), View.OnClickListener {

    lateinit var binding: QuestionDialogVoteBinding
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var userDTO: UserDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionDialogVoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        binding.buttonMinus.setOnClickListener {
            var count = binding.textInputCount.text.toString().toInt()
            if (count > 1) {
                binding.textInputCount.text = "${count.minus(1)}"
            }
        }

        binding.buttonPlus.setOnClickListener {
            var count = binding.textInputCount.text.toString().toInt()
            if (count < ticketCount) {
                binding.textInputCount.text = "${count.plus(1)}"
            }
        }

        binding.buttonMax.setOnClickListener {
            binding.textInputCount.text = "$ticketCount"
        }
    }

    private fun init() {
        binding.layoutMultiCount.visibility = View.GONE
        binding.layoutPremium.visibility = View.GONE

        binding.buttonOk.setOnClickListener(this)

        var imageID = context.resources.getIdentifier(item.rankDTO?.image, "drawable", context.packageName)
        //img_profile.setImageResource(imageID)
        Glide.with(binding.imgProfile.context)
            .asBitmap()
            .load(imageID) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgProfile)
        Glide.with(binding.imgLock.context)
            .asBitmap()
            .load(R.drawable.lock) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgLock)

        binding.textMsg1.text = "${item.rankDTO?.name} 님에게"
        binding.textCount.text = "득표수 : ${decimalFormat.format(item.rankDTO?.count)}"

        setFavorite()

        var count = questMaxCount - questCount
        if (userDTO?.isPremium()!! || count <= 0) {
            binding.layoutInputDesc.visibility = View.GONE
            binding.layoutMultiCount.visibility = View.VISIBLE
            if (userDTO?.isPremium()!!) {
                binding.layoutPremium.visibility = View.VISIBLE
            }
        } else {
            binding.textInputDesc.text = "오늘 광고를 $count 회 더 보면 다중 투표가 활성화 됩니다."
        }

        binding.imgProfile.setOnClickListener {
            val dialog = ImageViewDialog(context)
            dialog.imageID = imageID
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.show()
        }
    }

    fun setFavorite() {
        if (item.favorite!!) {
            binding.imgFavorite.setImageResource(R.drawable.hearts_fill)
            binding.imgFavorite.colorFilter = null
        } else {
            binding.imgFavorite.setImageResource(R.drawable.hearts_line)
            binding.imgFavorite.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.text_disable2), PorterDuff.Mode.SRC_IN)
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