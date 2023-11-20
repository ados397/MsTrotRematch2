package com.ados.mstrotrematch2

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Window
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.ados.mstrotrematch2.databinding.ActivityJoinBinding
import com.ados.mstrotrematch2.dialog.*
import com.ados.mstrotrematch2.model.LogDTO
import com.ados.mstrotrematch2.model.MailDTO
import com.ados.mstrotrematch2.model.UserDTO
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.QuestionDTO
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess


class JoinActivity : AppCompatActivity() {
    private var _binding: ActivityJoinBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var toast : Toast? = null

    private var loadingDialog : LoadingDialog? = null
    private var questionDialog: QuestionDialog? = null

    private var emailOK: Boolean = false
    private var passwordOK: Boolean = false
    private var passwordConfirmOK: Boolean = false
    private var nicknameOK: Boolean = false

    private var currentUser: UserDTO? = null
    private var nonMemberUser: UserDTO? = null

    private var documentDialog : DocumentDialog? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        currentUser = intent.getParcelableExtra("user")
        nonMemberUser = intent.getParcelableExtra("non_member")
        if (currentUser != null) { // null 이 아니라면 소셜 로그인, 이메일, 비밀번호는 입력하지 않음
            binding.editEmail.setText(currentUser?.userId)
            binding.editEmail.isEnabled = false
            emailOK = true

            binding.editPassword.setText("password_sample")
            binding.editPassword.isEnabled = false
            passwordOK = true

            binding.editPasswordConfirm.setText("password_sample")
            binding.editPasswordConfirm.isEnabled = false
            passwordConfirmOK = true
        } else if (nonMemberUser != null) { // 비회원 회원가입
            binding.editNickname.setText(nonMemberUser?.nickname)
            nicknameOK = true
        }

        binding.textTermsOfUse.paintFlags = binding.textTermsOfUse.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.textPrivacyPolicy.paintFlags = binding.textPrivacyPolicy.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonOk.setOnClickListener {
            var email = binding.editEmail.text.toString().trim()
            var nickname = binding.editNickname.text.toString().trim()
            var password = binding.editPassword.text.toString().trim()

            if (!verifyNickname(nickname)) {
                Toast.makeText(this, "사용할 수 없는 닉네임 입니다.", Toast.LENGTH_SHORT).show()
            } else {
                firebaseViewModel.findUserFromEmail(email) { userDTO ->
                    if (userDTO != null) {
                        Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        firebaseViewModel.isUsedUserNickname(nickname) { isUsed ->
                            if (nonMemberUser == null && isUsed) { // 비회원은 닉네임 중복 허용
                                Toast.makeText(this, "닉네임이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                val ticketCount = sharedPreferences.getInt(MySharedPreferences.PREF_KEY_TICKET_COUNT, 10)
                                val gemCount = sharedPreferences.getInt(MySharedPreferences.PREF_KEY_GEM_COUNT, 0)
                                getTicketGem(ticketCount, gemCount) {
                                    if (it) {
                                        loading()

                                        var addUser = UserDTO()
                                        if (nonMemberUser != null) { // 비회원 회원전환은 기존 데이터 유지
                                            addUser = nonMemberUser!!.copy()
                                        } else {
                                            addUser.level = 1
                                            addUser.exp = 0L
                                            addUser.paidGem = 0
                                            addUser.freeGem = 0
                                            addUser.mainTitle = ""
                                            addUser.aboutMe = ""
                                            addUser.premiumExpireTime = Date()
                                            addUser.createTime = Date()
                                            addUser.ticketCount = ticketCount
                                            addUser.freeGem = gemCount
                                        }
                                        addUser.userId = email
                                        addUser.nickname = nickname

                                        if (currentUser != null) { // null 이 아니라면 소셜 로그인, 이미 로그인 처리는 되어 있음, firestore 데이터 기록 후 메인페이지 이동
                                            addUser.uid = firebaseAuth?.currentUser?.uid
                                            addUser.loginType = currentUser?.loginType
                                            writeFirestoreAndFinish(addUser, false)
                                        } else {
                                            firebaseAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    firebaseAuth?.currentUser?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                                                        if (verifyTask.isSuccessful) {
                                                            addUser.uid = firebaseAuth?.currentUser?.uid
                                                            addUser.loginType = UserDTO.LoginType.EMAIL
                                                            writeFirestoreAndFinish(addUser, true)
                                                        } else {
                                                            loadingEnd()
                                                            Toast.makeText(this, "인증메일 발송에 실패하였습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                } else if (!task.exception?.message.isNullOrEmpty()) {
                                                    loadingEnd()
                                                    Toast.makeText(this, "회원가입에 실패하였습니다. 잠시 후 다시 시도해 보세요.", Toast.LENGTH_SHORT).show()
                                                    //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.editEmail.doAfterTextChanged {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.editEmail.text).matches()) {
                binding.textEmailError.text = "이메일 형식으로 입력해 주세요."
                binding.editEmail.setBackgroundResource(R.drawable.edit_rectangle_red)
                emailOK = false
            } else {
                binding.textEmailError.text = ""
                binding.editEmail.setBackgroundResource(R.drawable.edit_rectangle)
                emailOK = true
            }

            if (binding.editEmail.text.toString().isEmpty())
                emailOK = false

            visibleOkButton()
        }

        binding.editPassword.doAfterTextChanged {
            if (!isValidPassword(binding.editPassword.text.toString())) {
                binding.textPasswordError.text = "비밀번호는 8자 이상 영문 대소문자, 숫자, 특수문자 중 2종류 이상 조합하여야 합니다."
                binding.editPassword.setBackgroundResource(R.drawable.edit_rectangle_red)
                passwordOK = false
            } else {
                binding.textPasswordError.text = ""
                binding.editPassword.setBackgroundResource(R.drawable.edit_rectangle)
                passwordOK = true
            }

            if (binding.editPassword.text.toString().isEmpty())
                passwordOK = false

            isValidPasswordConfirm()

            visibleOkButton()
        }

        binding.editPasswordConfirm.doAfterTextChanged {
            isValidPasswordConfirm()

            if (binding.editPasswordConfirm.text.toString().isEmpty())
                passwordConfirmOK = false

            visibleOkButton()
        }

        binding.editNickname.doAfterTextChanged {
            if (!isValidNickname(binding.editNickname.text.toString())) {
                binding.textNicknameError.text = "사용할 수 없는 문자열이 포함되어 있습니다."
                binding.editNickname.setBackgroundResource(R.drawable.edit_rectangle_red)
                nicknameOK = false
            } else {
                binding.textNicknameError.text = ""
                binding.editNickname.setBackgroundResource(R.drawable.edit_rectangle)
                nicknameOK = true
            }

            if (binding.editNickname.text.toString().isEmpty())
                nicknameOK = false

            binding.textNicknameLen.text = "${binding.editNickname.text.length}/15"

            visibleOkButton()
        }

        binding.textTermsOfUse.setOnClickListener {
           var dialog = WebViewDialog(this, "http://adosent.com/mstrotrematch2/terms")
           dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
           dialog.setCanceledOnTouchOutside(false)

           dialog.show()


            /*firebaseViewModel.getTermsOfUse { document ->
                onDocumentDialog(document)
            }*/
        }

        binding.textPrivacyPolicy.setOnClickListener {
            var dialog = WebViewDialog(this, "http://adosent.com/mstrotrematch2/personal")
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)

            dialog.show()

            /*firebaseViewModel.getPrivacyPolicy { document ->
                onDocumentDialog(document)
            }*/
        }

        binding.checkboxAll.setOnClickListener { onCheckChanged(binding.checkboxAll) }
        binding.checkboxTermsOfUse.setOnClickListener { onCheckChanged(binding.checkboxTermsOfUse) }
        binding.checkboxPrivacyPolicy.setOnClickListener { onCheckChanged(binding.checkboxPrivacyPolicy) }
        binding.checkboxAge14.setOnClickListener { onCheckChanged(binding.checkboxPrivacyPolicy) }

        /*binding.checkboxAll.setOnCheckedChangeListener { compoundButton, b ->
            binding.checkboxTermsOfUse.isChecked = b
            binding.checkboxPrivacyPolicy.isChecked = b
        }

        binding.checkboxTermsOfUse.setOnCheckedChangeListener { compoundButton, b ->
            setCheckBox()
        }

        binding.checkboxPrivacyPolicy.setOnCheckedChangeListener { compoundButton, b ->
            setCheckBox()
        }*/
    }
    
    private fun onCheckChanged(compoundButton: CompoundButton) {
        when(compoundButton.id) {
            R.id.checkbox_all -> {
                if (binding.checkboxAll.isChecked) {
                    binding.checkboxTermsOfUse.isChecked = true
                    binding.checkboxPrivacyPolicy.isChecked = true
                    binding.checkboxAge14.isChecked = true
                }else {
                    binding.checkboxTermsOfUse.isChecked = false
                    binding.checkboxPrivacyPolicy.isChecked = false
                    binding.checkboxAge14.isChecked = false
                }
            }
            else -> {
                binding.checkboxAll.isChecked = (
                        binding.checkboxTermsOfUse.isChecked && binding.checkboxPrivacyPolicy.isChecked && binding.checkboxAge14.isChecked)
            }
        }

        visibleOkButton()
    }

    private fun getTicketGem(ticketCount: Int, gemCount: Int, myCallback: (Boolean) -> Unit) {
        if (ticketCount > 10 || gemCount > 0) {
            val ticketGemDialog = TicketGemDialog(this, ticketCount, gemCount)
            ticketGemDialog.show()
            ticketGemDialog.binding.buttonOk.setOnClickListener {
                ticketGemDialog.dismiss()
                // 기존 티켓, 뽑기권 데이터 삭제
                sharedPreferences.putInt(MySharedPreferences.PREF_KEY_TICKET_COUNT, 0)
                sharedPreferences.putInt(MySharedPreferences.PREF_KEY_GEM_COUNT, 0)

                myCallback(true)
            }
            ticketGemDialog.binding.buttonCancel.setOnClickListener {
                ticketGemDialog.dismiss()
                callToast("약관에 동의해야 시작 가능합니다.")
                myCallback(false)
            }
        } else {
            myCallback(true)
        }
    }

    private fun verifyNickname(nickname: String) : Boolean {
        return !(nickname == "리매치" ||
                nickname == "관리자" ||
                nickname == "운영자" ||
                nickname == "운영진" ||
                nickname.equals("admin", true) ||
                nickname.equals("administrator", true))
    }

    private fun writeFirestoreAndFinish(user: UserDTO, isEmailAuth: Boolean) {
        firebaseViewModel.updateUser(user) {
            val gemCount = 10
            val calendar= Calendar.getInstance()
            calendar.add(Calendar.DATE, 7)
            val docName = "master${System.currentTimeMillis()}"
            var mail = MailDTO(docName,"리매치W 가입을 축하합니다!", "새로운 리매치에 오신걸 환영합니다!\n\n새롭게 바뀐 '스타 투표 리매치W'에서\n즐겁고 행복한 응원도 하고\n최애 이름으로 기부도 하며\n보람찬 '덕질 라이프'를 즐겨 보세요!\n\n\n회원가입을 축하하며\n소정의 축하 다이아를 드립니다.", "운영자", MailDTO.Item.FREE_GEM, gemCount, Date(), calendar.time)
            firebaseViewModel.sendUserMail(user.uid.toString(), mail) {
                var log = LogDTO("[회원가입] 축하 다이아 $gemCount 개 우편 발송, 유효기간 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.time)}까지", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                if (nonMemberUser != null) {
                    log.log = "[회원전환 성공] 기존 uid : ${nonMemberUser?.uid}, nickname : ${nonMemberUser?.nickname}, padGem : ${nonMemberUser?.paidGem}, freeGem : ${nonMemberUser?.freeGem}, totalGem : ${nonMemberUser?.getTotalGem()}, ticketCount : ${nonMemberUser?.ticketCount}"
                    firebaseViewModel.writeUserLog(user.uid.toString(), log) { }
                }
            }

            if (isEmailAuth) { // 이메일 회원가입 시 이메일 인증 후 로그인됨
                firebaseAuth?.signOut()
                Toast.makeText(this, "회원가입을 위한 인증메일을 보냈습니다. 인증 후 로그인 해주세요.", Toast.LENGTH_LONG).show()
            } else { // 소셜 회원가입 시 바로 로그인됨
                loadingEnd()
                Toast.makeText(this, "회원가입이 완료 되었습니다.", Toast.LENGTH_SHORT).show()

                // 로그인 성공 시 비회원 로그인 정보 삭제
                sharedPreferences.putString(MySharedPreferences.PREF_KEY_NON_MEMBER_UID, "")

                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            }

            if (nonMemberUser != null) { // 기존 비회원 사용자는 닉네임 초기화 및 탈퇴처리
                sharedPreferences.putString(MySharedPreferences.PREF_KEY_NON_MEMBER_UID, "") // 기기의 비회원 ID 초기화
                nonMemberUser?.nickname = ""
                firebaseViewModel.updateUserNickname(nonMemberUser!!, 0) {
                    firebaseViewModel.updateUserDeleteTime(nonMemberUser!!) {
                        loadingEnd()
                        val question = QuestionDTO(
                            QuestionDTO.Stat.INFO,
                            "회원전환",
                            "회원전환 성공!\n앱을 재시작 해주세요. 확인 버튼을 누르면 종료됩니다."
                        )
                        onAppExit(question)
                    }
                }
            } else {
                //loadingEnd()
                finish()
            }
        }
    }

    private fun isValidPasswordConfirm() {
        if (binding.editPasswordConfirm.text.toString() != binding.editPassword.text.toString()) {
            binding.textPasswordConfirmError.text = "비밀번호가 일치하지 않습니다."
            binding.editPasswordConfirm.setBackgroundResource(R.drawable.edit_rectangle_red)
            passwordConfirmOK = false
        } else {
            binding.textPasswordConfirmError.text = ""
            binding.editPasswordConfirm.setBackgroundResource(R.drawable.edit_rectangle)
            passwordConfirmOK = true
        }
    }

    private fun isKorean(s: String): Boolean {
        var i = 0
        while (i < s.length) {
            val c = s.codePointAt(i)
            if (c in 0xAC00..0xD800)
                return true
            i += Character.charCount(c)
        }
        return false
    }


    private fun isValidPassword(password: String) : Boolean {
        if (password.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*\$".toRegex())) {
            return false
        }

        return password.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9~!@#\$%^&*]).{8,30}\$".toRegex())
    }

    private fun isValidNickname(nickname: String) : Boolean {
        val exp = Regex("^[가-힣ㄱ-ㅎa-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_-]{1,15}\$")
        return !nickname.isNullOrEmpty() && exp.matches(nickname)
    }

    private fun visibleOkButton() {
        binding.buttonOk.isEnabled = emailOK && passwordOK && passwordConfirmOK && nicknameOK && binding.checkboxAll.isChecked
    }

    private fun loading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
            loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadingDialog?.setCanceledOnTouchOutside(false)
        }
        loadingDialog?.show()
    }

    private fun loadingEnd() {
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            if (loadingDialog != null) {
                loadingDialog?.dismiss()
            }
        }, 400)
    }

    private fun appExit() {
        finishAffinity() //해당 앱의 루트 액티비티를 종료시킨다. (API  16미만은 ActivityCompat.finishAffinity())
        System.runFinalization() //현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.
        exitProcess(0) // 현재 액티비티를 종료시킨다.
    }

    private fun onAppExit(question: QuestionDTO) {
        if (questionDialog == null) {
            questionDialog = QuestionDialog(this, question)
            questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            questionDialog?.setCanceledOnTouchOutside(false)
        } else {
            questionDialog?.question = question
        }
        questionDialog?.show()
        questionDialog?.setInfo()
        questionDialog?.showButtonOk(false)
        questionDialog?.setButtonCancel("확인")
        questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
            questionDialog?.dismiss()
            questionDialog = null
            appExit()
        }
    }

    private fun onDocumentDialog(document: String) {
        if (documentDialog == null) {
            documentDialog = DocumentDialog(this, document)
            documentDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            documentDialog?.setCanceledOnTouchOutside(false)
        } else {
            documentDialog?.content = document
        }
        documentDialog?.show()
        documentDialog?.setInfo()
        documentDialog?.showButtonOk(false)
        documentDialog?.binding?.buttonDocumentCancel?.setOnClickListener { // No
            documentDialog?.dismiss()
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }
}