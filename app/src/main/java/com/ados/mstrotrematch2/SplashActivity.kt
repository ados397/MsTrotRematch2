package com.ados.mstrotrematch2

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ados.mstrotrematch2.databinding.ActivitySplashBinding
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.util.AdsInterstitialManager
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class SplashActivity : AppCompatActivity() {
    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var googleSignInClient : GoogleSignInClient? = null

    // AD
    private var adsInterstitialManager : AdsInterstitialManager? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        firebaseViewModel.getAdPolicy()
        firebaseViewModel.adPolicyDTO.observe(this) {
            adsInterstitialManager = AdsInterstitialManager(this, firebaseViewModel.adPolicyDTO.value!!)
            adsInterstitialManager?.callInterstitial {
                startActivity()
            }
        }
    }

    private fun callLoginActivity() {
        firebaseAuth?.signOut()
        //Auth.GoogleSignInApi.signOut()
        googleSignInClient?.signOut()?.addOnCompleteListener { }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun isLogin(uid: String) : Boolean {
        return if (uid.isNotEmpty()) // 비회원 로그인
            true
        else
            firebaseAuth?.currentUser != null
    }

    private fun startActivity() {
        var uid = sharedPreferences.getString(MySharedPreferences.PREF_KEY_NON_MEMBER_UID, "")!!
        if (isLogin(uid)) { // 로그인 정보가 있으면 메인 페이지 이동
            if (uid.isEmpty()) { // 값이 없으면 비회원 로그인이 아님
                uid = firebaseAuth?.currentUser?.uid!!
            }
            firebaseViewModel.getUser(uid) { userDTO ->
                if (userDTO != null) {
                    if (userDTO.nickname.isNullOrEmpty()) { // 닉네임이 없으면 로그아웃 후 로그인 페이지로 이동
                        callLoginActivity()
                    } else { // 데이터가 모두 있을때만 자동 로그인
                        // 이벤트 티켓 획득 ID 리스트 정리
                        // 정리 안하면 계속 쌓이기 때문 2일이 지난 항목은 삭제
                        val calendar= Calendar.getInstance()
                        calendar.add(Calendar.DATE, -2)
                        val nowDate = "${SimpleDateFormat("yyMMdd0000").format(calendar.time)}"
                        var isChange = false
                        val it = userDTO.eventTicketIds.iterator()
                        while (it.hasNext()) {
                            var id = it.next()
                            if (id.substring(1) < nowDate) {
                                it.remove()
                                isChange = true
                            }
                        }

                        if (isChange) {
                            firebaseViewModel.updateUserEventTicketIds(userDTO) {

                            }
                        }

                        var intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("user", userDTO)
                        startActivity(intent)
                        finish()
                    }
                } else { // document 없으면 로그아웃 후 로그인 페이지로 이동
                    callLoginActivity()
                }
            }
        } else { // 로그인 정보가 없으면 로그인 페이지 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}