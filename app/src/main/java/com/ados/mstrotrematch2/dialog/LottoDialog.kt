package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.LottoDialogBinding
import com.bumptech.glide.Glide
import kr.fromwhy.menuselector.actor.Wheel
import org.jetbrains.anko.runOnUiThread
import kotlin.random.Random

class LottoDialog(context: Context) : Dialog(context), View.OnClickListener {
    lateinit var binding: LottoDialogBinding

    private var imageList: ArrayList<Int> = arrayListOf()
    private lateinit var wheel: Wheel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LottoDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(binding.imgBackground.context)
            .asBitmap()
            .load(R.drawable.lotto_back) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgBackground)
        Glide.with(binding.imgGift.context)
            .asBitmap()
            .load(R.drawable.gift_box2) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgGift)

        imageList.add(R.drawable.tickets1)
        imageList.add(R.drawable.tickets2)
        imageList.add(R.drawable.tickets3)
        imageList.add(R.drawable.tickets4)
        imageList.add(R.drawable.tickets5)
        imageList.add(R.drawable.tickets4)
        imageList.add(R.drawable.tickets3)
        imageList.add(R.drawable.tickets2)

        startWheel()

        binding.layoutButtonGift.setOnClickListener {
            if(wheel.isStarted()) {
                var result = wheel.stopWheel()
            }

            binding.imgLotto.visibility = View.GONE
            binding.layoutButtonGift.visibility = View.GONE

            val resultValue = getValue()
            binding.textResult.text = "$resultValue"
            binding.layoutResult.visibility = View.VISIBLE
            binding.buttonCancel.setEnabled(true)

            when (resultValue) {
                in 1..4 -> Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.result_back1).optionalFitCenter().into(binding.imgBackground)
                in 5..8 -> Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.result_back2).optionalFitCenter().into(binding.imgBackground)
                in 9..12 -> Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.result_back3).optionalFitCenter().into(binding.imgBackground)
                in 13..16 -> Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.result_back4).optionalFitCenter().into(binding.imgBackground)
                in 17..20 -> Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.result_back5).optionalFitCenter().into(binding.imgBackground)
            }

        }
    }

    fun getValue() : Int {
        val randomValue = Random.nextInt(1, 1001)
        var resultValue = 0
        when (randomValue) {
            /*1 -> result_value = 1
            in 2..16 -> result_value = 2
            in 17..31 -> result_value = 3
            in 32..46 -> result_value = 4
            in 47..56 -> result_value = 5
            in 57..66 -> result_value = 6
            in 67..70 -> result_value = 7
            in 71..74 -> result_value = 8
            in 75..78 -> result_value = 9
            in 79..81 -> result_value = 10
            in 82..83 -> result_value = 11
            in 84..85 -> result_value = 12
            in 86..87 -> result_value = 13
            in 88..89 -> result_value = 14
            in 90..91 -> result_value = 15
            in 92..93 -> result_value = 16
            in 94..95 -> result_value = 17
            in 96..97 -> result_value = 18
            in 98..99 -> result_value = 19
            100 -> result_value = 20*/
            in 1..5 -> resultValue = 1
            in 6..205 -> resultValue = 2
            in 206..385 -> resultValue = 3
            in 386..555 -> resultValue = 4
            in 556..655 -> resultValue = 5
            in 656..755 -> resultValue = 6
            in 756..795 -> resultValue = 7
            in 796..835 -> resultValue = 8
            in 836..875 -> resultValue = 9
            in 876..905 -> resultValue = 10
            in 906..915 -> resultValue = 11
            in 916..925 -> resultValue = 12
            in 926..935 -> resultValue = 13
            in 936..945 -> resultValue = 14
            in 946..955 -> resultValue = 15
            in 956..965 -> resultValue = 16
            in 966..975 -> resultValue = 17
            in 976..985 -> resultValue = 18
            in 986..995 -> resultValue = 19
            in 996..1000 -> resultValue = 20
        }

        return resultValue
    }

    private fun startWheel() {
        wheel = Wheel(imageList, object : Wheel.WheelListener {
            override fun newImage(img: Int) {
                context.runOnUiThread {
                    //img_lotto.setImageResource(img)
                    Glide.with(binding.imgLotto.context)
                        .asBitmap()
                        .load(img) ///feed in path of the image
                        .optionalFitCenter()
                        .into(binding.imgLotto)
                }
            }
        }, 200, randomLong(0, 200))

        wheel.start()
    }

    private fun randomLong(lower: Long, upper: Long): Long {
        return lower + ((Random.nextDouble() * (upper - lower))).toLong()
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}