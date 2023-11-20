package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.RecoveryDialogBinding
import com.ados.mstrotrematch2.model.PreferencesDTO
import com.ados.mstrotrematch2.model.RecoveryCodeDTO
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RecoveryDialog(context: Context) : Dialog(context), View.OnClickListener {

    lateinit var binding: RecoveryDialogBinding
    var isRun = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecoveryDialogBinding.inflate(layoutInflater)
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