package com.ados.mstrotrematch2.page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.mstrotrematch2.JoinActivity
import com.ados.mstrotrematch2.LoginActivity
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.FragmentAccountJoinBinding
import com.ados.mstrotrematch2.dialog.DocumentDialog
import com.ados.mstrotrematch2.dialog.QuestionDialog
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.LogDTO
import com.ados.mstrotrematch2.model.MailDTO
import com.ados.mstrotrematch2.model.QuestionDTO
import com.ados.mstrotrematch2.model.UserDTO
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAccountJoin.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAccountJoin : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAccountJoinBinding? = null
    private val binding get() = _binding!!
    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private var toast : Toast? = null
    private var questionDialog: QuestionDialog? = null
    private var documentDialog : DocumentDialog? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountJoinBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firebaseAuth = FirebaseAuth.getInstance()

        // 구글 로그인 처리
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            println("구글 로그인 $result")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                var signResult = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data!!)!!
                if (signResult.isSuccess) {
                    var account = signResult.signInAccount
                    firebaseAuthWithGoogle(account)
                }
            } else {
                callToast("취소 되었습니다.")
                (activity as MainActivity?)?.loadingEnd()
            }
        }

        // 구글 로그인 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(),gso)

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun callBackPressed() {
        finishFragment()
    }

    private fun finishFragment() {
        val fragment = FragmentAccountSettings()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLoginGoogle.setOnClickListener {
            showQuestionDialog()
            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                questionDialog?.dismiss()

                (activity as MainActivity?)?.loading()

                val signInIntent = googleSignInClient?.signInIntent
                //startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
                resultLauncher.launch(signInIntent)
            }
        }

        binding.buttonJoin.setOnClickListener {
            showQuestionDialog()
            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                questionDialog?.dismiss()

                firebaseViewModel.getTermsOfUse { document ->
                    if (documentDialog == null) {
                        documentDialog = DocumentDialog(requireContext(), document)
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

                        var user = (activity as MainActivity?)?.getUser()!!.copy()
                        var intent = Intent(requireActivity(), JoinActivity::class.java)
                        intent.putExtra("non_member", user)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun showQuestionDialog() {
        val question = QuestionDTO(
            QuestionDTO.Stat.INFO,
            "회원전환",
            "회원으로 전환 하시겠습니까?",
        )
        if (questionDialog == null) {
            questionDialog = QuestionDialog(requireContext(), question)
            questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            questionDialog?.setCanceledOnTouchOutside(false)
        } else {
            questionDialog?.question = question
        }
        questionDialog?.show()
        questionDialog?.setInfo()
        questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
            questionDialog?.dismiss()
            questionDialog = null
        }
    }

    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        // 구글 로그인은 동일한 이메일로 로그인한 계정(페이스북, 이메일)이 있어도 로그인이 되어버리기 때문에 이미 가입된 정보가 있는지 확인해서 없을때만 처리
        firebaseViewModel.findUserFromEmail(account?.email.toString()) { userDTO ->
            if (userDTO != null) { // 사용자가 존재하면 회원전환 할 수 없음.
                if (userDTO.deleteTime == null) { // 탈퇴한 사용자는 가입 가능
                    firebaseAuth?.signOut()
                    googleSignInClient?.signOut()?.addOnCompleteListener { }
                    callToast("이미 가입된 사용자 입니다.")
                    googleSignInClient?.signOut()?.addOnCompleteListener { }
                    (activity as MainActivity?)?.loadingEnd()
                    return@findUserFromEmail
                }
            }

            //println("탈퇴 리턴 안됨")
            var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
            firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val oldUser = (activity as MainActivity?)?.getUser()!!.copy()
                    var addUser = (activity as MainActivity?)?.getUser()!!
                    addUser.uid = firebaseAuth?.currentUser?.uid
                    addUser.userId = firebaseAuth?.currentUser?.email
                    addUser.loginType = UserDTO.LoginType.GOOGLE
                    firebaseViewModel.updateUser(addUser) {
                        val gemCount = 10
                        val calendar= Calendar.getInstance()
                        calendar.add(Calendar.DATE, 7)
                        val docName = "master${System.currentTimeMillis()}"
                        var mail = MailDTO(docName,"리매치W 가입을 축하합니다!", "새로운 리매치에 오신걸 환영합니다!\n\n새롭게 바뀐 '스타 투표 리매치W'에서\n즐겁고 행복한 응원도 하고\n최애 이름으로 기부도 하며\n보람찬 '덕질 라이프'를 즐겨 보세요!\n\n\n회원가입을 축하하며\n소정의 축하 다이아를 드립니다.", "운영자", MailDTO.Item.FREE_GEM, gemCount, Date(), calendar.time)
                        firebaseViewModel.sendUserMail(addUser.uid.toString(), mail) {
                            var log = LogDTO("[회원가입] 축하 다이아 $gemCount 개 우편 발송, 유효기간 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.time)}까지", Date())
                            firebaseViewModel.writeUserLog(addUser.uid.toString(), log) { }

                            log.log = "[회원전환 성공] 기존 uid : ${oldUser.uid}, nickname : ${oldUser.nickname}, padGem : ${oldUser.paidGem}, freeGem : ${oldUser.freeGem}, totalGem : ${oldUser.getTotalGem()}, ticketCount : ${oldUser.ticketCount}"
                            firebaseViewModel.writeUserLog(addUser.uid.toString(), log) { }
                        }

                        // 기존 비회원 사용자는 닉네임 초기화 및 탈퇴처리
                        sharedPreferences.putString(MySharedPreferences.PREF_KEY_NON_MEMBER_UID, "") // 기기의 비회원 ID 초기화
                        oldUser.nickname = ""
                        firebaseViewModel.updateUserNickname(oldUser, 0) {
                            firebaseViewModel.updateUserDeleteTime(oldUser) {
                                (activity as MainActivity?)?.loadingEnd()
                                val question = QuestionDTO(
                                    QuestionDTO.Stat.INFO,
                                    "회원전환",
                                    "구글 회원전환 성공!\n앱을 재시작 해주세요. 확인 버튼을 누르면 종료됩니다."
                                )
                                (activity as MainActivity?)?.onAppExit(question)
                            }
                        }
                    }
                    //callToast("구글 회원전환 성공")
                } else if (!task.exception?.message.isNullOrEmpty()) {
                    //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    //Toast.makeText(this, "구글 로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity?)?.loadingEnd()
                    callToast("이미 가입된 사용자 입니다.")
                }
            }
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentAccountJoin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentAccountJoin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}