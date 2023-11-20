package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.ados.mstrotrematch2.databinding.NonMemberSaveUidDialogBinding
import com.ados.mstrotrematch2.model.UserDTO

class NonMemberSaveUidDialog(context: Context, var userDTO: UserDTO) : Dialog(context) {

    lateinit var binding: NonMemberSaveUidDialogBinding

    var isDisabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NonMemberSaveUidDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInfo()

        binding.buttonCopyUid.setOnClickListener {
            val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", binding.textUid.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        binding.checkboxDisabled.setOnCheckedChangeListener { compoundButton, b ->
            isDisabled = b
        }
    }

    fun setInfo() {
        binding.textUid.text = userDTO.uid
    }

    fun setButtonOk(name: String) {
        binding.textQuestionOk.text = name
    }
}