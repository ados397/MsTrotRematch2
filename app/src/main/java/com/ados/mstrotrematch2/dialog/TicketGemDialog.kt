package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.ados.mstrotrematch2.databinding.TicketGemDialogBinding

class TicketGemDialog(context: Context, val ticketCount: Int, val gemCount: Int) : Dialog(context), View.OnClickListener {

    lateinit var binding: TicketGemDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)

        binding = TicketGemDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textTicketCount.text = "티켓 : ${ticketCount}장"
        binding.textGemCount.text = "다이아 : ${gemCount}개"
    }

    private fun init() {
        //binding.buttonQuestionOk.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.binding.buttonQuestionOk -> {
                dismiss()
            }
        }*/
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}