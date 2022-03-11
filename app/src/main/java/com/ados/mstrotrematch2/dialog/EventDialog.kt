package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.EventDTO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.event_dialog.*

class EventDialog(context: Context, var event : EventDTO) : Dialog(context), View.OnClickListener {

    private val layout = R.layout.event_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
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

                text_timer.text = "[${String.format("%02d",hour)}시${String.format("%02d",min)}분${String.format("%02d",sec)}초] 후 티켓이 사라집니다."
            }

        }.start()
    }

    private fun init() {
        button_ok.setOnClickListener(this)

        text_msg.text = event.title?.replace("\\n","\n")

        Glide.with(img_ticket.context)
            .asBitmap()
            .load(R.drawable.ticekt) ///feed in path of the image
            .fitCenter()
            .into(img_ticket)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }
    }
}