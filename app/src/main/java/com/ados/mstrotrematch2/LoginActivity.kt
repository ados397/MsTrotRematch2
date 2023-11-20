package com.ados.mstrotrematch2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ados.mstrotrematch2.databinding.ActivityLoginBinding
import com.ados.mstrotrematch2.dialog.*
import com.ados.mstrotrematch2.model.UserDTO
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.EditTextDTO
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.ados.mstrotrematch2.util.Utility
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*


class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var toast : Toast? = null
    private var documentDialog : DocumentDialog? = null
    private var loadingDialog : LoadingDialog? = null
    private var editTextModifyDialog: EditTextModifyDialog? = null

    private var backWaitTime = 0L //뒤로가기 연속 클릭 대기 시간

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // 업데이트 설정 획득
        firebaseViewModel.getServerUpdateListen()
        observeUpdate()

        // 구글 로그인 처리
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            println("구글 로그인 $result")
            if (result.resultCode == RESULT_OK) {
                var signResult = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data!!)!!
                if (signResult.isSuccess) {
                    var account = signResult.signInAccount
                    firebaseAuthWithGoogle(account)
                }
            }
        }

        // 구글 로그인 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        binding.buttonJoin.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
            /*firebaseViewModel.getTermsOfUse { document ->
                if (documentDialog == null) {
                    documentDialog = DocumentDialog(this, document)
                    documentDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    documentDialog?.setCanceledOnTouchOutside(false)
                } else {
                    documentDialog?.content = document
                }
                documentDialog?.show()
                documentDialog?.setInfo()
                documentDialog?.setButtonOk("동의")
                documentDialog?.setButtonCancel("동의안함")
                documentDialog?.binding?.buttonDocumentCancel?.setOnClickListener { // No
                    documentDialog?.dismiss()
                    callToast("약관에 동의해야 회원가입이 가능합니다.")
                }
                documentDialog?.binding?.buttonDocumentOk?.setOnClickListener { // Ok
                    documentDialog?.dismiss()
                    callToast("약관에 동의 하셨습니다.")

                    startActivity(Intent(this, JoinActivity::class.java))
                }
            }*/
        }

        binding.buttonNonMemberLogin.setOnClickListener {
            firebaseViewModel.getNonMemberDocument { document ->
                if (documentDialog == null) {
                    documentDialog = DocumentDialog(this, document)
                    documentDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    documentDialog?.setCanceledOnTouchOutside(false)
                } else {
                    documentDialog?.content = document
                }
                documentDialog?.show()
                documentDialog?.setInfo()
                documentDialog?.setButtonOk("동의")
                documentDialog?.setButtonCancel("동의안함")
                documentDialog?.binding?.buttonDocumentCancel?.setOnClickListener { // No
                    documentDialog?.dismiss()
                    callToast("약관에 동의해야 시작 가능합니다.")
                }
                documentDialog?.binding?.buttonDocumentOk?.setOnClickListener { // Ok
                    documentDialog?.dismiss()
                    callToast("약관에 동의 하셨습니다.")

                    val ticketCount = sharedPreferences.getInt(MySharedPreferences.PREF_KEY_TICKET_COUNT, 10)
                    val gemCount = sharedPreferences.getInt(MySharedPreferences.PREF_KEY_GEM_COUNT, 0)
                    getTicketGem(ticketCount, gemCount) {
                        if (it) {
                            loading()

                            // 랜덤 닉네임 생성
                            var nickname = Utility.randomNickName()
                            firebaseViewModel.isUsedUserNickname(nickname) { isUsed ->
                                if (isUsed) { // 닉네임이 존재하면 다시 생성
                                    callToast("데이터 처리에 실패 했습니다. 잠시 후 다시 시도해 주세요.")
                                    loadingEnd()
                                } else {
                                    var addUser = UserDTO()
                                    addUser.uid = Utility.randomDocumentName()
                                    addUser.loginType = UserDTO.LoginType.NON_MEMBER
                                    addUser.userId = UUID.randomUUID().toString()
                                    addUser.nickname = nickname
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
                                    firebaseViewModel.updateUser(addUser) {
                                        loadingEnd()

                                        sharedPreferences.putString(MySharedPreferences.PREF_KEY_NON_MEMBER_UID, addUser.uid.toString())

                                        var intent = Intent(this, MainActivity::class.java)
                                        intent.putExtra("user", addUser)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.buttonFindPassword.setOnClickListener {
            startActivity(Intent(this, FindPasswordActivity::class.java))
        }

        binding.editPassword.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KEYCODE_ENTER) {
                login()
            }
            false
        }

        binding.buttonLogin.setOnClickListener {
            login()
        }

        binding.buttonLoginGoogle.setOnClickListener {
            val signInIntent = googleSignInClient?.signInIntent
            //startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
            resultLauncher.launch(signInIntent)
        }

        binding.buttonLoginFacebook.setOnClickListener {
            //facebookLogin()
        }

        binding.buttonNonMemberRecovery.setOnClickListener {
            val item = EditTextDTO("비회원 복구 코드를 입력하세요.")
            if (editTextModifyDialog == null) {
                editTextModifyDialog = EditTextModifyDialog(this, item)
                editTextModifyDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                editTextModifyDialog?.setCanceledOnTouchOutside(false)
            } else {
                editTextModifyDialog?.item = item
            }
            editTextModifyDialog?.show()
            editTextModifyDialog?.setInfo()
            editTextModifyDialog?.binding?.buttonModifyCancel?.setOnClickListener { // No
                editTextModifyDialog?.dismiss()
            }
            editTextModifyDialog?.binding?.buttonModifyOk?.setOnClickListener { // Ok
                loading()

                val code = editTextModifyDialog?.binding?.editContent?.text.toString().trim()
                firebaseViewModel.getUser(code) { userDTO ->
                    if (userDTO == null) {
                        callToast("사용자를 찾을 수 없습니다.")
                        loadingEnd()
                    } else {
                        if (userDTO.deleteTime != null) { // 탈퇴한 사용자
                            callToast("회원전환 또는 탈퇴한 사용자입니다.")
                            loadingEnd()
                        } else if (userDTO.loginType != UserDTO.LoginType.NON_MEMBER) { // 비회원 로그인이 아니라면? (이럴 경우는 없어야 함)
                            callToast("비회원 사용자가 아닙니다.")
                            loadingEnd()
                        } else {
                            editTextModifyDialog?.dismiss()
                            callToast("비회원 사용자 복구 완료")

                            sharedPreferences.putString(MySharedPreferences.PREF_KEY_NON_MEMBER_UID, code)
                            var intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("user", userDTO)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
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

    // 업데이트 모니터링
    private fun observeUpdate() {
        firebaseViewModel.updateDTO.observe(this) {
            if (firebaseViewModel.updateDTO.value != null) {
                if (firebaseViewModel.updateDTO.value!!.maintenance!!) { // 서버 점검 대화상자 출력
                    onMaintenanceDialog()
                }
            }
        }
    }

    private fun onMaintenanceDialog() {
        val maintenanceDialog = MaintenanceDialog(this, MaintenanceDialog.JobType.MAINTENANCE)
        maintenanceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        maintenanceDialog.setCanceledOnTouchOutside(false)
        maintenanceDialog.updateDTO = firebaseViewModel.updateDTO.value
        maintenanceDialog.show()
        maintenanceDialog.binding.buttonMaintenanceOk.setOnClickListener {
            maintenanceDialog.dismiss()
            finish() //액티비티 종료
        }
    }

    private fun facebookLogin() {
        /*LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                println("페이스북 onSuccess")
                handleFacebookAccessToken(result?.accessToken)

            }

            override fun onCancel() {
                println("페이스북 onCancel")
            }

            override fun onError(error: FacebookException?) {
                println("페이스북 onError")
            }

        })
         */
    }

    private fun login() {
        val email = binding.editEmail.text.toString().trim()
        when {
            email.isNullOrEmpty() -> {
                callToast("이메일을 입력하세요.")
            }
            binding.editPassword.text.isNullOrEmpty() -> {
                callToast("비밀번호를 입력하세요.")
            }
            else -> {
                firebaseAuth?.signInWithEmailAndPassword(email, binding.editPassword.text.toString())?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (firebaseAuth?.currentUser?.isEmailVerified!!) { // 인증 받은 사용자인지 확인
                            firebaseViewModel.getUser(firebaseAuth?.uid!!) { userDTO ->
                                if (userDTO != null) {
                                    if (userDTO.deleteTime != null) { // 탈퇴한 사용자
                                        firebaseAuth?.signOut()
                                        googleSignInClient?.signOut()?.addOnCompleteListener { }
                                        callToast("탈퇴처리된 사용자입니다.")
                                    } else {
                                        callMainActivity(userDTO)
                                        callToast("로그인 성공")

                                        // 로그인 성공 시 비회원 로그인 정보 삭제
                                        sharedPreferences.putString(MySharedPreferences.PREF_KEY_NON_MEMBER_UID, "")
                                    }
                                } else {
                                    callToast("로그인에 실패하였습니다. 관리자에게 문의 하세요.")
                                }
                            }
                            /*firestore?.collection("user")?.document(firebaseAuth?.uid!!)?.get()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    if (task.result.exists()) { // document 있음
                                        var user = task.result.toObject(UserDTO::class.java)!!
                                        callMainActivity(user)
                                        callToast("로그인 성공")
                                    } else { // document 없으면 회원 가입 페이지로 이동
                                        callToast("로그인에 실패하였습니다. 관리자에게 문의 하세요.")
                                    }
                                }
                            }*/
                        } else {
                            firebaseAuth?.signOut()
                            callLoginEmailVerifyActivity(email, binding.editPassword.text.toString())
                            //callToast("이메일 인증이 완료되지 않았습니다. 이메일 인증 완료 후 로그인 가능합니다.")
                        }
                    } else if (!task.exception?.message.isNullOrEmpty()) {
                        //callToast(task.exception?.message)
                        callToast("로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.")
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        // 구글 로그인은 동일한 이메일로 로그인한 계정(페이스북, 이메일)이 있어도 로그인이 되어버리기 때문에 이미 가입된 정보가 있는지 확인해서 없을때만 처리
        firebaseViewModel.findUserFromEmail(account?.email.toString()) { userDTO ->
            if (userDTO != null) { // 로그인한 구글 계정과 동일한 이메일의 사용자 존재
                if (userDTO.deleteTime != null) { // 탈퇴한 사용자
                    firebaseAuth?.signOut()
                    googleSignInClient?.signOut()?.addOnCompleteListener { }
                    callToast("탈퇴처리된 사용자입니다.")
                    return@findUserFromEmail
                } else if (userDTO.loginType != UserDTO.LoginType.GOOGLE) { // 구글 로그인이 아니라면 다른 방법으로 이미 가입한 사용자
                    callToast("이미 가입된 사용자 입니다.")
                    googleSignInClient?.signOut()?.addOnCompleteListener { }
                    return@findUserFromEmail
                }
            }

            var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
            firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var addUser = UserDTO()
                    addUser.uid = firebaseAuth?.currentUser?.uid
                    addUser.userId = firebaseAuth?.currentUser?.email
                    addUser.loginType = UserDTO.LoginType.GOOGLE
                    addUser.level = 1
                    addUser.exp = 0L
                    addUser.paidGem = 0
                    addUser.freeGem = 0
                    addUser.aboutMe = ""
                    addUser.mainTitle = ""
                    addUser.premiumExpireTime = Date()
                    addUser.createTime = Date()

                    callToast("구글 로그인 성공")
                    loginOrJoin(addUser)
                } else if (!task.exception?.message.isNullOrEmpty()) {
                    //callToast(task.exception?.message)
                    //callToast("구글 로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.")
                    callToast("이미 가입된 사용자 입니다.")
                }
            }
        }

        /*firestore?.collection("user")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    var user = document.toObject(UserDTO::class.java)!!
                    if (user.loginType != UserDTO.LoginType.GOOGLE) {
                        when {
                            user.userId!! == account?.email.toString() -> {
                                callToast("이미 가입된 이메일 입니다.")
                                googleSignInClient?.signOut()?.addOnCompleteListener {

                                }
                                return@addOnCompleteListener
                            }
                        }
                    }
                }

                var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
                firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var addUser = UserDTO()
                        addUser.uid = firebaseAuth?.currentUser?.uid
                        addUser.userId = firebaseAuth?.currentUser?.email
                        addUser.loginType = UserDTO.LoginType.GOOGLE
                        addUser.level = 1
                        addUser.exp = 0L
                        addUser.paidGem = 0
                        addUser.freeGem = 0
                        addUser.aboutMe = ""
                        addUser.mainTitle = ""
                        addUser.premiumExpireTime = Date()
                        addUser.createTime = Date()

                        callToast("구글 로그인 성공")
                        loginOrJoin(addUser)
                    } else if (!task.exception?.message.isNullOrEmpty()) {
                        //callToast(task.exception?.message)
                        //callToast("구글 로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.")
                        callToast("이미 가입된 이메일 입니다.")
                    }
                }
            }
        }*/
    }

    /*private fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var addUser = UserDTO()
                addUser.uid = firebaseAuth?.currentUser?.uid
                addUser.userId = firebaseAuth?.currentUser?.email
                addUser.loginType = UserDTO.LoginType.FACEBOOK
                addUser.level = 1
                addUser.exp = 0L
                addUser.paidGem = 0
                addUser.freeGem = 0
                addUser.aboutMe = ""
                addUser.mainTitle = ""
                addUser.premiumExpireTime = Date()
                addUser.createTime = Date()

                callToast("페이스북 로그인 성공")
                loginOrJoin(addUser)
            } else if (!task.exception?.message.isNullOrEmpty()) {
                //callToast(task.exception?.message)
                callToast("페이스북 로그인에 실패하였습니다. 동일한 이메일을 사용하는 사용자가 이미 존재할 수 있습니다.")
            }
        }
    }*/

    private fun loginOrJoin(user: UserDTO) {
        firebaseViewModel.getUser(user.uid!!) { userDTO ->
            if (userDTO != null) {
                if (userDTO.nickname.isNullOrEmpty()) { // 닉네임이 없으면 회원 가입 페이지로 이동
                    callJoinActivity(userDTO)
                } else {
                    // 로그인 성공 시 비회원 로그인 정보 삭제
                    sharedPreferences.putString(MySharedPreferences.PREF_KEY_NON_MEMBER_UID, "")
                    callMainActivity(userDTO)
                }
            } else {
                callJoinActivity(user)
            }
        }
        /*firestore?.collection("user")?.document(userDTO.uid!!)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result.exists()) { // document 있음
                    var user = task.result.toObject(UserDTO::class.java)!!
                    if (user.nickname.isNullOrEmpty()) { // 닉네임이 없으면 회원 가입 페이지로 이동
                        callJoinActivity(user)
                    } else {
                        callMainActivity(user)
                    }
                } else { // document 없으면 회원 가입 페이지로 이동
                    callJoinActivity(userDTO)
                }
            }
        }*/
    }

    private fun callJoinActivity(user: UserDTO) {
        var intent = Intent(this, JoinActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun callMainActivity(user: UserDTO) {
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
        finish()
    }

    private fun callLoginEmailVerifyActivity(email: String, password: String) {
        var intent = Intent(this, LoginEmailVerifyActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("password", password)
        startActivity(intent)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        //appExit()
        if(System.currentTimeMillis() - backWaitTime >=2000 ) {
            backWaitTime = System.currentTimeMillis()
            Snackbar.make(binding.layoutMain,"'뒤로' 버튼을 한번 더 누르면 앱이 종료됩니다.", Snackbar.LENGTH_LONG).show()
        } else {
            finish() //액티비티 종료
        }
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

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }
}