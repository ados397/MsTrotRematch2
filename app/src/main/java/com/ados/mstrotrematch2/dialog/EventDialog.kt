package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.EventDialogBinding
import com.ados.mstrotrematch2.model.EventDTO
import com.bumptech.glide.Glide

class EventDialog(context: Context, var event : EventDTO) : Dialog(context), View.OnClickListener {

    lateinit var binding: EventDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EventDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        init()

        var limitTime = event.limit?.time
        var interval = (limitTime!!.toLong()) - System.currentTimeMillis()

        object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                dismiss()
            }

            override fun onTick(millisUntilFinished: Long) {
                var totalsec = millisUntilFinished / 1000
                var hour = totalsec / 3600
                var min = (totalsec % 3600) / 60
                var sec = totalsec % 60

                binding.textTimer.text = "[${String.format("%02d",hour)}시${String.format("%02d",min)}분${String.format("%02d",sec)}초] 후 티켓이 사라집니다."
            }

        }.start()
    }

    private fun init() {
        binding.buttonOk.setOnClickListener(this)

        binding.textMsg.text = event.title?.replace("\\n","\n")

        Glide.with(binding.imgTicket.context)
            .asBitmap()
            .load(R.drawable.ticekt) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgTicket)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }
    }
}