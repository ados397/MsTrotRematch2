package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.BoardDTO
import kotlinx.android.synthetic.main.delete_dialog.*
import kotlinx.android.synthetic.main.event_dialog.button_ok

class DeleteDialog(context: Context, var item : BoardDTO) : Dialog(context), View.OnClickListener {

    private val layout = R.layout.delete_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        init()

        //button_admin_delete.visibility = View.GONE // 관리자모드
        button_ok.setOnClickListener {

        }
    }

    private fun init() {
        button_ok.setOnClickListener(this)

        //text_msg.text = event.title?.replace("\\n","\n")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }
    }
}