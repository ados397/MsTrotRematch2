package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.databinding.LoveGameInputDialogBinding
import com.ados.mstrotrematch2.model.PreferencesDTO
import com.google.firebase.firestore.FirebaseFirestore

class LoveGameInputDialog(context: Context) : Dialog(context), View.OnClickListener {
    lateinit var binding: LoveGameInputDialogBinding

    var firestore : FirebaseFirestore? = null
    var preferencesDTO : PreferencesDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoveGameInputDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            preferencesDTO = documentSnapshot?.toObject(PreferencesDTO::class.java)
        }

        init()
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }
}