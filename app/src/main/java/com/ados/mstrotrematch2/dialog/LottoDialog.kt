package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.ados.mstrotrematch2.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.lotto_dialog.*
import kr.fromwhy.menuselector.actor.Wheel
import org.jetbrains.anko.runOnUiThread
import kotlin.random.Random

class LottoDialog(context: Context) : Dialog(context), View.OnClickListener {
    private val layout = R.layout.lotto_dialog
    private var imageList: ArrayList<Int> = arrayListOf()
    private lateinit var wheel: Wheel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        Glide.with(img_background.context)
            .asBitmap()
            .load(R.drawable.lotto_back) ///feed in path of the image
            .fitCenter()
            .into(img_background)
        Glide.with(img_gift.context)
            .asBitmap()
            .load(R.drawable.gift_box2) ///feed in path of the image
            .fitCenter()
            .into(img_gift)

        imageList.add(R.drawable.tickets1)
        imageList.add(R.drawable.tickets2)
        imageList.add(R.drawable.tickets3)
        imageList.add(R.drawable.tickets4)
        imageList.add(R.drawable.tickets5)
        imageList.add(R.drawable.tickets4)
        imageList.add(R.drawable.tickets3)
        imageList.add(R.drawable.tickets2)

        startWheel()

        layout_button_gift.setOnClickListener {
            if(wheel.isStarted()) {
                var result = wheel.stopWheel()
            }

            img_lotto.visibility = View.GONE
            layout_button_gift.visibility = View.GONE

            val result_value = getValue()
            text_result.text = "$result_value"
            layout_result.visibility = View.VISIBLE
            button_cancel.setEnabled(true)

            when (result_value) {
                in 1..4 -> Glide.with(img_background.context).asBitmap().load(R.drawable.result_back1).fitCenter().into(img_background)
                in 5..8 -> Glide.with(img_background.context).asBitmap().load(R.drawable.result_back2).fitCenter().into(img_background)
                in 9..12 -> Glide.with(img_background.context).asBitmap().load(R.drawable.result_back3).fitCenter().into(img_background)
                in 13..16 -> Glide.with(img_background.context).asBitmap().load(R.drawable.result_back4).fitCenter().into(img_background)
                in 17..20 -> Glide.with(img_background.context).asBitmap().load(R.drawable.result_back5).fitCenter().into(img_background)
            }

        }
    }

    fun getValue() : Int {
        val random_value = Random.nextInt(1, 1001)
        var result_value = 0
        when (random_value) {
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
            in 1..5 -> result_value = 1
            in 6..205 -> result_value = 2
            in 206..385 -> result_value = 3
            in 386..555 -> result_value = 4
            in 556..655 -> result_value = 5
            in 656..755 -> result_value = 6
            in 756..795 -> result_value = 7
            in 796..835 -> result_value = 8
            in 836..875 -> result_value = 9
            in 876..905 -> result_value = 10
            in 906..915 -> result_value = 11
            in 916..925 -> result_value = 12
            in 926..935 -> result_value = 13
            in 936..945 -> result_value = 14
            in 946..955 -> result_value = 15
            in 956..965 -> result_value = 16
            in 966..975 -> result_value = 17
            in 976..985 -> result_value = 18
            in 986..995 -> result_value = 19
            in 996..1000 -> result_value = 20
        }

        return result_value
    }

    private fun startWheel() {
        wheel = Wheel(imageList, object : Wheel.WheelListener {
            override fun newImage(img: Int) {
                context.runOnUiThread {
                    //img_lotto.setImageResource(img)
                    Glide.with(img_lotto.context)
                        .asBitmap()
                        .load(img) ///feed in path of the image
                        .fitCenter()
                        .into(img_lotto)
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