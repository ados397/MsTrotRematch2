package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.Window
import android.widget.Toast
import com.ados.mstrotrematch2.model.QuestionDTO
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.CheeringInfoDialogBinding
import com.ados.mstrotrematch2.model.PreferencesDTO
import com.ados.mstrotrematch2.model.RecoveryCodeDTO
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CheeringInfoDialog(context: Context, var question: QuestionDTO) : Dialog(context), View.OnClickListener {

    lateinit var binding: CheeringInfoDialogBinding

    private var firestore : FirebaseFirestore? = null
    private var preferencesDTO : PreferencesDTO? = null
    private var recoveryDialog : RecoveryDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CheeringInfoDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            preferencesDTO = documentSnapshot?.toObject(PreferencesDTO::class.java)
        }

        binding.textTitle.text = question.title.toString().replace("\\n","\n")
        binding.textContent.text = question.content.toString().replace("\\n","\n")

        when(question.stat) {
            QuestionDTO.Stat.INFO -> binding.imgStat.setImageResource(R.drawable.information)
            QuestionDTO.Stat.WARNING -> binding.imgStat.setImageResource(R.drawable.warning)
            QuestionDTO.Stat.ERROR -> binding.imgStat.setImageResource(R.drawable.error)
        }

        binding.buttonRecovery.setOnClickListener {
            if (recoveryDialog == null) {
                recoveryDialog = RecoveryDialog(context)
                recoveryDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                recoveryDialog?.setCanceledOnTouchOutside(false)
            }
            recoveryDialog?.show()

            recoveryDialog?.binding?.buttonCancel?.setOnClickListener { // No
                recoveryDialog?.dismiss()
                recoveryDialog = null
            }

            recoveryDialog?.binding?.buttonOk?.setOnClickListener { // Ok
                val code = recoveryDialog?.binding?.editCode?.text.toString().trim()
                val name = recoveryDialog?.binding?.editName?.text.toString().trim()
                val email = recoveryDialog?.binding?.editEmail?.text.toString().trim()
                if (code.isNullOrEmpty()) {
                    Toast.makeText(context, "복구코드를 입력하세요.", Toast.LENGTH_SHORT).show()
                } else if (name.isNullOrEmpty()) {
                    Toast.makeText(context, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                } else if (email.isNullOrEmpty()) {
                    Toast.makeText(context, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    println("코드 : $code")
                    // 관리자 복구 코드
                    if (code == "ados397" && email == "ados397@gmail.com") {
                        val adminCount = name.toInt()
                        recoveryDialog?.dismiss()
                        recoveryDialog = null
                        // 투표권 추가
                        var pref = PreferenceManager.getDefaultSharedPreferences(context)
                        var ticketcount = pref.getInt("TicketCount", preferencesDTO?.ticketChargeCount!!)

                        var editor = pref.edit()
                        editor.putInt("TicketCount", ticketcount.plus(adminCount)).apply()

                        Toast.makeText(context,"관리자 code(${adminCount}) 실행", Toast.LENGTH_SHORT).show()
                    } else {
                        if (recoveryDialog?.isRun == false) {
                            recoveryDialog?.isRun = true
                            firestore?.collection("recoveryCode")?.whereEqualTo("code", code)?.get()?.addOnCompleteListener { task ->
                                if (task.isSuccessful && task.result.size() > 0) {
                                    for (document in task.result) { // 사용자 찾음
                                        val recoveryCodeDTO = document.toObject(RecoveryCodeDTO::class.java)
                                        println("코드 정보 : $recoveryCodeDTO")
                                        if (recoveryCodeDTO.useTime != null) {
                                            recoveryDialog?.isRun = false
                                            Toast.makeText(context, "이미 사용된 코드 입니다.", Toast.LENGTH_SHORT).show()
                                        }  else {
                                            if (name != recoveryCodeDTO.name) {
                                                recoveryDialog?.isRun = false
                                                Toast.makeText(context, "잘못된 이름 입니다. 이름을 제대로 입력했는지 확인해 주세요.", Toast.LENGTH_SHORT).show()
                                            } else if (email != recoveryCodeDTO.email) {
                                                recoveryDialog?.isRun = false
                                                Toast.makeText(context, "잘못된 이메일 입니다. 이메일을 제대로 입력했는지 확인해 주세요.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                recoveryDialog?.dismiss()
                                                recoveryDialog = null
                                                firestore?.collection("recoveryCode")?.document(recoveryCodeDTO.code.toString())?.update("useTime", Date())?.addOnCompleteListener {
                                                    // 투표권 추가
                                                    var pref = PreferenceManager.getDefaultSharedPreferences(context)
                                                    var ticketcount = pref.getInt("TicketCount", preferencesDTO?.ticketChargeCount!!)

                                                    var editor = pref.edit()
                                                    editor.putInt("TicketCount", ticketcount.plus(recoveryCodeDTO.count!!)).apply()

                                                    Toast.makeText(context,"투표권이 ${recoveryCodeDTO.count}장 복구 되었습니다.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                        /*recoveryDialog?.dismiss()
                                        recoveryDialog = null
                                        firestore?.collection("recoveryCode")?.document(recoveryCodeDTO.code.toString())?.update("useTime", Date())?.addOnCompleteListener {
                                            // 투표권 추가
                                            var pref = PreferenceManager.getDefaultSharedPreferences(context)
                                            var ticketcount = pref.getInt("TicketCount", preferencesDTO?.ticketChargeCount!!)

                                            var editor = pref.edit()
                                            editor.putInt("TicketCount", ticketcount.plus(recoveryCodeDTO.count!!)).apply()

                                            Toast.makeText(context,"투표권이 ${recoveryCodeDTO.count}장 복구 되었습니다.", Toast.LENGTH_SHORT).show()
                                        }*/
                                    }
                                } else { // 코드 못 찾음
                                    Toast.makeText(context, "잘못된 코드 입니다. 코드 번호를 다시 확인해 주세요.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun setButtonOk(name: String) {
        binding.buttonQuestionOk.text = name
    }

    fun setButtonCancel(name: String) {
        binding.buttonQuestionCancel.text = name
    }

    fun showButtonOk(visible: Boolean) {
        if (visible) {
            binding.buttonQuestionOk.visibility = View.VISIBLE
        } else {
            binding.buttonQuestionOk.visibility = View.GONE
        }
    }

    fun showButtonCancel(visible: Boolean) {
        if (visible) {
            binding.buttonQuestionCancel.visibility = View.VISIBLE
        } else {
            binding.buttonQuestionCancel.visibility = View.GONE
        }
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