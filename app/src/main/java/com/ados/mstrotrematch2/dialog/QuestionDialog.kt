package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.model.QuestionDTO
import com.ados.mstrotrematch2.R
import kotlinx.android.synthetic.main.question_dialog.*

class QuestionDialog(context: Context, var question: QuestionDTO) : Dialog(context), View.OnClickListener {



    private val layout = R.layout.question_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        text_title.text = question.title.toString().replace("\\n","\n")
        text_content.text = question.content.toString().replace("\\n","\n")

        when(question.stat) {
            QuestionDTO.STAT.INFO -> img_stat.setImageResource(R.drawable.information)
            QuestionDTO.STAT.WARNING -> img_stat.setImageResource(R.drawable.warning)
            QuestionDTO.STAT.ERROR -> img_stat.setImageResource(R.drawable.error)
        }
    }

    fun setButtonOk(name: String) {
        button_question_ok.text = name
    }

    fun setButtonCancel(name: String) {
        button_question_cancel.text = name
    }

    fun showButtonOk(visible: Boolean) {
        if (visible == true) {
            button_question_ok.visibility = View.VISIBLE
        } else {
            button_question_ok.visibility = View.GONE
        }
    }

    fun showButtonCancel(visible: Boolean) {
        if (visible == true) {
            button_question_cancel.visibility = View.VISIBLE
        } else {
            button_question_cancel.visibility = View.GONE
        }
    }

    private fun init() {
        //button_question_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_question_ok -> {
                dismiss()
            }
        }*/
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}