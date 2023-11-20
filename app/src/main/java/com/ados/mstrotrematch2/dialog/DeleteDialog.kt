package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.DeleteDialogBinding
import com.ados.mstrotrematch2.model.BoardDTO

class DeleteDialog(context: Context, var item : BoardDTO) : Dialog(context), View.OnClickListener {

    lateinit var binding: DeleteDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DeleteDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        binding.buttonOk.setOnClickListener {

        }
    }

    private fun init() {
        binding.buttonOk.setOnClickListener(this)

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