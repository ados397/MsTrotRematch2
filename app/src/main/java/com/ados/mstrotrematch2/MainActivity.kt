package com.ados.mstrotrematch2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.ados.mstrotrematch2.database.DBHelperReport
import com.ados.mstrotrematch2.databinding.ActivityMainBinding
import com.ados.mstrotrematch2.dialog.*
import com.ados.mstrotrematch2.firebase.FirebaseRepository
import com.ados.mstrotrematch2.firebase.FirebaseStorageViewModel
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.*
import com.ados.mstrotrematch2.page.FragmentPageMusic
import com.ados.mstrotrematch2.util.AdsBannerManager
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.ados.mstrotrematch2.util.ZoomOutPageTransformer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.FileDescriptor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private val tabLayoutText = listOf(
        "랭킹",
        "투표",
        "팬클럽",
        "영상",
        "응원",
        "설정",
    )
    private val tabIcon = listOf(
        R.drawable.tab_rank,
        R.drawable.tab_vote,
        R.drawable.tab_rank, // 팬클럽 적용
        R.drawable.tab_music,
        R.drawable.tab_cheering,
        R.drawable.tab_account,
    )

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    lateinit var context : Context
    lateinit var fragmentPageMusic: FragmentPageMusic

    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null
    private val firebaseViewModel : FirebaseViewModel by viewModels() // 뷰모델 연결
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels() // 뷰모델 연결
    lateinit var dbHandler : DBHelperReport
    private var adsBannerManager : AdsBannerManager? = null // AD

    private var loadingDialog : LoadingDialog? = null
    private var questionDialog: QuestionDialog? = null

    private var oldUserDTO: UserDTO? = null
    private var currentUserExDTO = UserExDTO()

    private var backWaitTime = 0L //뒤로가기 연속 클릭 대기 시간
    private var displayCount = 0 // 전광판 일정 시간 유지를 위한 변수
    private var displayIndex = 0 // 전광판 돌아가며 표시하기 위한 인덱스
    private var displayList = arrayListOf<DisplayBoardDTO>() // 표시할 전광판을 Queue 형식으로 저장
    private lateinit var currentDate : String // 12시 지나서 날짜 변경을 체크하기 위한 변수

    // 최초에 모든 항목들이 로딩 완료 되었을 때 ViewPager 호출
    // currentLoadingCount 가 successLoadingCount 와 같아져야 로딩이 완료된 것
    private var checkLoadingCount = 0 // 일정 시간이 지나도 완료가 안되면 강제로 완료 처리
    private var reTryLoadingCount = 0 // 데이터 로딩에 실패했을 때 재실행 수
    private var currentLoadingCount = 0
    private var successLoadingCount = 8
    private var ticketCheckStart = false // 해당 변수가 활성화 되고 나서 티켓 타이머 시작

    private var runEvents : MutableSet<String> = mutableSetOf()
    private var isTicketTimerStart = false
    lateinit var ticketCountDownTimer : CountDownTimer

    private var loadingData = mutableMapOf<Int, Boolean>() // 주요 데이터 로딩 체크

    private var toast : Toast? = null
    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(this)
    }

    //private var favoriteImageList = mutableMapOf<String, String>() // 최애가수 Image, DocName 맵으로 저장 응원하기에서 탐색을 위해

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val visibility = sharedPreferences.getBoolean(MySharedPreferences.PREF_KEY_DISPLAY_BOARD_VISIBILITY, true)
        if (visibility) {
            binding.displayBoard.layoutMain.visibility = View.VISIBLE
        } else {
            binding.displayBoard.layoutMain.visibility = View.GONE
        }

        val curTime = System.currentTimeMillis()
        val deniedTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_PERMISSION_DENIED_TIME, 0L)
        var diffTime = (curTime - deniedTime) / 1000
        if (diffTime > (60*60*24*10)) { // 10일이 지났으면 다시 출력
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                TedPermission.create()
                    .setPermissionListener(object : PermissionListener {
                        override fun onPermissionGranted() {

                        }
                        override fun onPermissionDenied(deniedPermissions: List<String>) {
                            sharedPreferences.putLong(MySharedPreferences.PREF_KEY_PERMISSION_DENIED_TIME, curTime)
                        }
                    })
                    .setDeniedMessage("각종 이벤트 및 주요 공지를 받기 위해 알람을 허용해 주세요. [알림] > [알림 허용]")
                    .setPermissions(
                        Manifest.permission.POST_NOTIFICATIONS)
                    .check()
            }
        }

        updateCheck()

        autoTimeCheck()

        loading()
        currentDate = SimpleDateFormat("yyyyMMdd").format(Date())
        dbHandler = DBHelperReport(this)

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        // 로그인 체크
        loginCheck()
        for (i in 1..successLoadingCount) {
            loadingData[i] = false
        }
        println("[로딩] 석세스 카운트 $successLoadingCount")

        setInfo()

        /*val gemCount = 5
        val calendar= Calendar.getInstance()
        calendar.add(Calendar.DATE, 7)
        val docName = "master${System.currentTimeMillis()}"
        var mail = MailDTO(docName,"티켓", "티켓", "운영자", MailDTO.Item.TICKET, gemCount, Date(), calendar.time)
        firebaseViewModel.sendUserMail(oldUserDTO?.uid.toString(), mail) {
        }
        val docName2 = "master${System.currentTimeMillis()}"
        var mail2 = MailDTO(docName2,"다이아", "다이아", "운영자", MailDTO.Item.FREE_GEM, gemCount, Date(), calendar.time)
        firebaseViewModel.sendUserMail(oldUserDTO?.uid.toString(), mail2) {
        }

        var mail = MailDTO(docName,"리매치 가입을 축하합니다!", "새로운 리매치에 오신걸 환영합니다!\n\n새롭게 바뀐 '스타 투표 리매치'에서\n즐겁고 행복한 응원도 하고\n최애 이름으로 기부도 하고\n유튜브 광고로 전세계에 홍보도 하며\n보람찬 '덕질 라이프'를 즐겨 보세요!\n\n\n회원가입을 축하하며\n소정의 축하 다이아를 드립니다.", "운영자", MailDTO.Item.FREE_GEM, gemCount, Date(), calendar.time)
        firebaseViewModel.sendUserMail(oldUserDTO?.uid.toString(), mail) {
        }*/

        firebaseViewModel.getToken() // 토큰 획득
        firebaseViewModel.token.observe(this) {
            // 토큰이 변경되었다면 데이터 반영
            if(it != oldUserDTO?.token) {
                Log.d(TAG, "profileLoad: 토큰 변경되었음.")

                oldUserDTO?.token = it
                firebaseViewModel.updateUserToken(oldUserDTO!!) {
                    Log.d(TAG, "사용자 토큰 정보 업데이트 완료.")
                }
            }
        }

        // 로그인 시간 기록 (중복 로그인 방지)
        oldUserDTO?.loginTime = Date()
        firebaseViewModel.updateUserLoginTime(oldUserDTO!!) {
            // 환경 설정을 가장 먼저 획득하도록 수정
            // 2. 환경 설정 획득
            if (checkLoadingData(2)) {
                firebaseViewModel.stopPreferencesListen()
                firebaseViewModel.getPreferencesListen()
                println("[02.로딩] 환경 설정 획득")
            }

            // 2. 환경 설정 획득
            firebaseViewModel.preferencesDTO.observe(this) {
                loadingData[2] = true
                println("[02.로딩] 환경 설정 획득 성공")

                // 중요 데이터 로딩
                loadingMainData()
                observeMainData()
            }
        }

        // 가수 생일을 체크해서 생일인 가수 축하 메시지 출력
        firebaseViewModel.getPeople(FirebaseRepository.PeopleOrder.COUNT_ASC)
        firebaseViewModel.peopleDTOs.observe(this) {
            if (firebaseViewModel.peopleDTOs.value != null) {
                val nowDate = SimpleDateFormat("yyyyMMdd").format(Date())
                val nowMonthDay = SimpleDateFormat("MMdd").format(Date())
                for (person in firebaseViewModel.peopleDTOs.value!!) {
                    //favoriteImageList[person.docname.toString()] = person.image.toString()

                    if (person.birthday != null) {
                        val writeDate = sharedPreferences.getStringSubKey(MySharedPreferences.PREF_KEY_CONGRATULATE_READ_TIME, person.docname, "")
                        if (nowDate != writeDate) {
                            val birthDay = SimpleDateFormat("MMdd").format(person.birthday)
                            if (nowMonthDay == birthDay) {
                                val dialog = CongratulateDialog(this)
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                dialog.setCanceledOnTouchOutside(false)
                                dialog.rankDTO = person
                                dialog.show()

                                dialog.binding.buttonNoticeOk.setOnClickListener { // No
                                    dialog.dismiss()

                                    if (dialog.isStopToday) {
                                        sharedPreferences.putStringSubKey(MySharedPreferences.PREF_KEY_CONGRATULATE_READ_TIME, person.docname, nowDate)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 데이터 획득이 끝나면 ViewPager 호출
        timer(period = 100)
        {
            if (checkLoadingCount > 50) { // 데이터 로딩에 실패 했을 경우
                if (reTryLoadingCount >= 2) { // 재시도 횟수까지 모두 실패하면 프로그램 종료
                    cancel()
                    runOnUiThread {
                        failedLoading()
                    }
                } else {
                    reTryLoadingCount++
                    checkLoadingCount = 0
                    runOnUiThread {
                        loadingMainData()
                    }
                }
            }

            if (loadingData.filterValues { !it }.keys.isEmpty()) {
                //if (currentLoadingCount >= successLoadingCount) {
                println("[로딩] 카운트 $checkLoadingCount")
                cancel()
                runOnUiThread {
                    // 프로그램 종료된 동안 충전된 티켓 수 적용
                    // 현재 충전 가능한 티켓 수 = 최대 티켓 수 - 현재 티켓 수
                    var marginCount = firebaseViewModel.preferencesDTO.value?.ticketChargeCount!! - firebaseViewModel.userDTO.value?.ticketCount!!
                    if (marginCount > 0) {
                        var chargeTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_TICKET_CHARGE_TIME, 0) // 마지막 충전 시간

                        // 종료된 동안 충전된 티켓 수 = (현재 시간 - 마지막 충전 시간) / 티켓 충전 시간
                        var chargedTicketCount = ((System.currentTimeMillis() - chargeTime) / firebaseViewModel.preferencesDTO.value?.getIntervalTimeMillis()!!).toInt()

                        println("티켓 충전 chargeTime $chargeTime  chargedTicketCount $chargedTicketCount")

                        if (chargedTicketCount > 0) { // 프로그램 종료된 동안 충전된 티켓 수가 있을 경우 현재 티켓수에 누적 시켜 준다
                            // 충전된 티켓 수가 최대 충전 가능 수 보다 클 경우 최대 충전 가능 수 만큼만 충전
                            var chargeCount =  if (chargedTicketCount > marginCount) {
                                marginCount
                            } else {
                                chargedTicketCount
                            }
                            firebaseViewModel.addUserTicket(currentUserExDTO.userDTO?.uid.toString(), chargeCount) {}

                            // 추가 충전을 했다면 충전 시간 계산해서 저장
                            sharedPreferences.putLong(MySharedPreferences.PREF_KEY_TICKET_CHARGE_TIME, chargeTime)
                        }
                    }
                    ticketCheckStart = true

                    loadingEnd()
                    setViewPager()

                    val info: PackageInfo = packageManager.getPackageInfo(packageName, 0)
                    val localVersion = info.versionName
                    println("업데이트 버전 - 현재 : $localVersion, 서버 : ${firebaseViewModel.updateDTO.value!!}")

                    checkDeleteAccount()

                    // 서버 점검이 아닐때만 튜토리얼 및 공지 대화상자 호출
                    if (!firebaseViewModel.updateDTO.value!!.maintenance!!) {
                        // 공지 대화상자 실행
                        //onNoticeDialog()

                        // 업데이트가 필요할 경우 업데이트 창 호출
                        if (localVersion < firebaseViewModel.updateDTO.value!!.minVersion.toString()) {
                            // 앱 구동 최소 버전을 만족하지 못하므로 강제 업데이트
                            if (firebaseViewModel.updateDTO.value!!.minVersionDisplay!!) { // 표시 여부 확인 후 표시
                                onUpdateDialog(MaintenanceDialog.JobType.UPDATE_IMMEDIATE, localVersion)
                            }
                        } else if (localVersion < firebaseViewModel.updateDTO.value!!.updateVersion.toString()) {
                            // 앱 권장 버전을 만족하지 못하므로 업데이트 권장
                            if (firebaseViewModel.updateDTO.value!!.updateVersionDisplay!!) { // 표시 여부 확인 후 표시
                                onUpdateDialog(MaintenanceDialog.JobType.UPDATE_FLEXIBLE, localVersion)
                            }
                        }
                    }
                }
            }
            checkLoadingCount++
        }

        showNoticeDialog()
        showNoticeSubDialog()

        binding.displayBoard.layoutMain.setOnClickListener {
            var intent = Intent(this, DisplayBoardActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            intent.putExtra("preferences", firebaseViewModel.preferencesDTO.value)
            startActivity(intent)
        }

        binding.layoutPremium.setOnClickListener {
            var intent = Intent(this, PremiumPackageActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            intent.putExtra("preferences", firebaseViewModel.preferencesDTO.value)
            startActivity(intent)
        }

        binding.layoutMail.setOnClickListener {
            var intent = Intent(this, MailActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            //intent.putParcelableArrayListExtra("mails", mails)
            intent.putParcelableArrayListExtra("mails", firebaseViewModel.mailDTOs.value)
            startActivity(intent)
        }

        binding.layoutQuest.setOnClickListener {
            var intent = Intent(this, QuestActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //appExit()

        /*if (tutorialStep.value == 0) { // 튜토리얼이 진행중이 아닐때만 종료
            if(System.currentTimeMillis() - backWaitTime >=2000 ) {
                backWaitTime = System.currentTimeMillis()
                Snackbar.make(binding.layoutMain,"'뒤로' 버튼을 한번 더 누르면 앱이 종료됩니다.", Snackbar.LENGTH_LONG).show()
            } else {
                finish() //액티비티 종료
            }
        }*/
    }

    private fun dateCheck() : Boolean {
        val result = android.provider.Settings.Global.getInt(contentResolver, android.provider.Settings.Global.AUTO_TIME, 0)
        if (result == 1) {
            println("DateTime Sync: On")
            return true
        } else {
            var question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "날짜 및 시간 자동설정이 꺼져있습니다.",
                "날짜 및 시간 자동설정을 설정 후 앱을 이용해 주세요.\n설정 방법은 아래 링크를 참조하세요.\nhttps://info-hub.tistory.com/13"
            )

            val dialog = QuestionDialog(this, question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.showButtonOk(false)
            dialog.setButtonCancel("확인")
            dialog.binding.buttonQuestionCancel.setOnClickListener { // No
                dialog.dismiss()
                appExit()
            }
            return false
        }
        return false
    }

    private fun showNoticeDialog() {
        val dialog = NoticeDialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        dialog.binding.buttonCancel.setOnClickListener { // No
            dialog.dismiss()
        }
    }

    private fun showNoticeSubDialog() {
        val nowDate = SimpleDateFormat("yyyyMMdd").format(Date())
        val writeDate = sharedPreferences.getString(MySharedPreferences.PREF_KEY_NOTICE_SUB_READ_TIME, "")
        if (nowDate != writeDate) {
            firebaseViewModel.getNoticeSub() { noticeDTO ->
                if (noticeDTO?.visibility!!) {
                    val dialog = NoticeSubDialog(this)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.noticeDTO = noticeDTO
                    dialog.show()

                    dialog.binding.buttonNoticeOk.setOnClickListener { // No
                        dialog.dismiss()

                        if (dialog.isStopToday) {
                            sharedPreferences.putString(MySharedPreferences.PREF_KEY_NOTICE_SUB_READ_TIME, SimpleDateFormat("yyyyMMdd").format(Date()))
                        }
                    }
                }
            }
        }
    }

    fun showMainCtrl(show: Boolean) {
        if (show) {
            binding.tabs.visibility = View.VISIBLE
            //layout_adview.visibility  = View.VISIBLE
        } else {
            binding.tabs.visibility = View.GONE
            //layout_adview.visibility  = View.GONE
        }
    }
    fun showAdCtrl(show: Boolean) {
        if (show) {
            when(firebaseViewModel.adPolicyDTO.value?.ad_banner) {
                getString(R.string.adtype_admob),
                getString(R.string.adtype_cauly),
                getString(R.string.adtype_adfit) -> {
                    binding.layoutAdview.visibility = View.VISIBLE
                }
                else -> {
                    // 모두 비활성
                    binding.layoutAdview.visibility = View.GONE
                }
            }
        } else {
            binding.layoutAdview.visibility  = View.GONE
        }
    }

    fun callHallOfFameActivity() {
        //var intent = Intent(this, HallOfFameActivity::class.java)
        var intent = Intent(this, HallOfFameSelectActivity::class.java)
        intent.putExtra("adPolicy", getAdPolicy())
        intent.putExtra("season", getSeason())
        //intent.putExtra("title", item.title)
        //intent.putExtra("docname", item.docname)
        startActivity(intent)

    }

    private fun setInfo() {
        binding.imgPremiumEnable.visibility = View.GONE
        binding.imgPremiumDisable.visibility = View.VISIBLE
        binding.imgPremiumNew.visibility = View.VISIBLE
        binding.textTicketTimer.text = ""

        // 전광판 애니메이션 설정
        val anim = AlphaAnimation(0.1f, 1.0f)
        anim.duration = 800
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        binding.displayBoard.textDisplayBoard.startAnimation(anim)
        binding.displayBoard.textDisplayBoard.requestFocus()
        binding.displayBoard.textDisplayBoard.setOnFocusChangeListener { _, b ->
            if (!b) {
                binding.displayBoard.textDisplayBoard.requestFocus()
            }
        }

        // 타이머 설정
        displayTimer()
        dateCheckTimer()
    }

    // 전광판 타이머
    private fun displayTimer() {
        timer(period = 1000)
        {
            // 전광판은 5번 깜빡이고 다음 문자열 표시
            if (firebaseViewModel.preferencesDTO.value != null) {
                if (displayList.size > 0 && displayCount >= firebaseViewModel.preferencesDTO.value?.displayBoardPeriod!!) {
                    runOnUiThread {
                        //displayList.removeAt(0)
                        displayCount = 0

                        // 대기열에 있다면 다음 문자열 표시
                        if (displayList.size > 0 && displayList.size > displayIndex) {
                            if (!displayList[displayIndex].displayText.isNullOrEmpty()) {
                                binding.displayBoard.textDisplayBoard.text = displayList[displayIndex].displayText
                                binding.displayBoard.textDisplayBoard.setTextColor(displayList[displayIndex].color!!)
                            }
                            displayIndex++

                            if (displayList.size <= displayIndex)
                                displayIndex = 0
                        }
                    }
                }
                displayCount++
            }
        }
    }

    // 날짜 변경 체크 타이머
    private fun dateCheckTimer() {
        timer(period = 1000)
        {
            val checkDate = SimpleDateFormat("yyyyMMdd").format(Date())
            if (currentDate != checkDate) {
                runOnUiThread {
                    println("날짜가 변경되었습니다 $currentDate -> $checkDate")
                    currentDate = checkDate

                    checkPremium()
                    checkQuestNew()
                }
            }
        }
    }

    // 실시간 점검 모니터링
    private fun observeUpdate() {
        firebaseViewModel.updateDTO.observe(this) {
            if (firebaseViewModel.updateDTO.value != null) {
                if (firebaseViewModel.updateDTO.value!!.maintenance!!) { // 서버 점검 대화상자 출력
                    onMaintenanceDialog()
                }

                //println("[로딩] 업데이트 설정 획득")
                //addLoadingCount()
                loadingData[3] = true
                println("[03.로딩] 업데이트 설정 획득 성공")
            }
        }
    }

    // 실시간 사용자 정보 모니터링
    private fun observeUser() {
        firebaseViewModel.userDTO.observe(this) {
            if (firebaseViewModel.userDTO.value != null) {
                println("로딩 사용자 ${firebaseViewModel.userDTO.value}")
                currentUserExDTO.userDTO = it
                if (currentUserExDTO.userDTO?.imgProfile == null) {
                    currentUserExDTO.imgProfileUri = null
                } else {
                    firebaseStorageViewModel.getUserProfileImage(it?.uid.toString()) { uri ->
                        currentUserExDTO.imgProfileUri = uri
                    }
                }

                // 차단 여부 확인
                checkBlock()

                // 로그인 시간이 변경되었으면 중복 로그인으로 판단하여 종료
                checkMultiLogin()

                // 비회원 사용자
                checkNotMember()

                // 티켓 실시간 반영
                binding.textTicketCount.text = "${decimalFormat.format(firebaseViewModel.userDTO.value?.ticketCount)}"
                if (ticketCheckStart) {
                    if (firebaseViewModel.preferencesDTO.value?.ticketChargeCount!! > firebaseViewModel.userDTO.value?.ticketCount!!) { // 이미 충전된 투표권을 뺏지 않기 위해서 조건 추가
                        resetTicketTimer()
                    } else {
                        if (isTicketTimerStart)
                            ticketCountDownTimer.cancel()
                        isTicketTimerStart = false
                        binding.textTicketTimer.text = ""
                    }
                }

                // 다이아 실시간 반영
                binding.textGemCount.text = "${decimalFormat.format(firebaseViewModel.userDTO.value?.getTotalGem())}"

                // 프리미엄 패키지
                checkPremium()

                // 일일 퀘스트 수령할 보상이 있다면 UI 출력
                checkQuestNew()

                oldUserDTO = it?.copy() // 기존 정보와 변경사항 체크를 위해 복사
                //println("[로딩] 사용자 획득")
                //addLoadingCount()
                loadingData[4] = true
                println("[04.로딩] 사용자 정보 획득 성공")
            }
        }
    }

    // 실시간 전광판 모니터링
    private fun observeDisplayBoard() {
        /*firebaseViewModel.displayBoardDTO.observe(this) {
            if (firebaseViewModel.displayBoardDTO.value != null) {
                if (!firebaseViewModel.displayBoardDTO.value?.displayText.isNullOrEmpty()) {
                    // 차단된 전광판
                    var displayBoard = firebaseViewModel.displayBoardDTO.value!!
                    if (dbHandler.getBlock(firebaseViewModel.displayBoardDTO.value!!.docName.toString())) {
                        displayBoard.displayText = "내가 신고한 글입니다."
                        displayBoard.color = ContextCompat.getColor(this, R.color.text_disable)
                    }

                    // 대기열에 없다면 즉시 전광판 표시
                    if (displayList.size == 0) {
                        binding.displayBoard.textDisplayBoard.text = displayBoard.displayText
                        binding.displayBoard.textDisplayBoard.setTextColor(displayBoard.color!!)
                    }

                    displayList.add(displayBoard)

                    loadingData[5] = true
                    println("[05.로딩] 전광판 리스트 획득 성공")
                }
            }
        }*/

        firebaseViewModel.displayBoardDTOs.observe(this) {
            if (firebaseViewModel.displayBoardDTOs.value != null) {
                displayIndex = 0
                var displays = arrayListOf<DisplayBoardDTO>()
                for (display in firebaseViewModel.displayBoardDTOs.value!!) {
                    if (displays.size >= firebaseViewModel.preferencesDTO.value?.displayBoardCount!!) {
                        break
                    }

                    if (!dbHandler.getBlock(display.docName.toString())) { // 차단된 전광판은 넘김
                        displays.add(display)
                    }
                }

                // 대기열에 없다면 즉시 전광판 표시
                if (displayList.size == 0 && displays.size > 0) {
                    displayCount = firebaseViewModel.preferencesDTO.value?.displayBoardPeriod!!
                    binding.displayBoard.textDisplayBoard.text = displays[0].displayText
                    binding.displayBoard.textDisplayBoard.setTextColor(displays[0].color!!)
                }
                displayList = displays

                loadingData[5] = true
                println("[05.로딩] 전광판 리스트 획득 성공")
            }

        }
    }

    // 실시간 메일 모니터링
    private fun observeMail() {
        firebaseViewModel.mailDTOs.observe(this) {
            if (firebaseViewModel.mailDTOs.value != null) {
                var count = 0
                println("로딩 메일 - ${firebaseViewModel.mailDTOs.value}")
                if (firebaseViewModel.mailDTOs.value!!.size > 0) {
                    // 읽지 않은 메일이 있으면 새로운 메일 알림 표시
                    for (mail in firebaseViewModel.mailDTOs.value!!) {
                        if (mail.read == false) {
                            count++
                            break
                        }
                    }
                }
                if (count > 0) {
                    binding.imgMailNew.visibility = View.VISIBLE
                } else {
                    binding.imgMailNew.visibility = View.GONE
                }

                /*if (firebaseViewModel.mailDTOs.value?.size!! > 0) {
                    binding.imgMailNew.visibility = View.VISIBLE
                } else {
                    binding.imgMailNew.visibility = View.GONE
                }*/
                //println("[로딩] 메일 획득")
                //addLoadingCount()
                loadingData[6] = true
                println("[06.로딩] 메일 리스트 획득 성공")
            }
        }
    }

    // 실시간 이벤트 티켓 수령
    private fun observeEventTickets() {
        firebaseViewModel.eventDTOs.observe(this) {
            if (firebaseViewModel.eventDTOs.value != null) {
                for (event in firebaseViewModel.eventDTOs.value!!) {
                    /*val uid = sharedPreferences.getString(event.uid, "")
                    if (uid.isNullOrEmpty()) { // 값이 없어야 아직 수령안한 티켓이다.
                        showEventDialog(event)
                    }*/

                    println("이벤트 티켓 ${currentUserExDTO.userDTO?.eventTicketIds}, uid ${event.uid}")
                    println("이벤트 티켓22 ${currentUserExDTO.userDTO}")
                    if (currentUserExDTO.userDTO?.eventTicketIds == null || currentUserExDTO.userDTO?.eventTicketIds?.contains(event.uid) == false) { // 값이 없어야 아직 수령안한 티켓이다.
                        showEventDialog(event)
                    }
                }
                loadingData[8] = true
                println("[08.로딩] 이벤트 티켓 획득 성공")
            }
        }
    }

    // 이벤트 티켓 대화상자 호출
    private fun showEventDialog(event: EventDTO) {
        // 이미 동일한 uid의 대화상자가 실행중인지 확인
        if (null == runEvents.find { it.startsWith(event.uid.toString()) } ) {
            runEvents.add(event.uid.toString())

            val dialog = EventDialog(this, event)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.binding.buttonCancel.setOnClickListener { // No
                dialog.dismiss()
                runEvents.remove(event.uid.toString())
            }

            dialog.binding.buttonOk.setOnClickListener { // Yes
                dialog.dismiss()
                firebaseViewModel.addUserTicket(currentUserExDTO.userDTO?.uid.toString(), event.count!!) {

                }

                //sharedPreferences.putString(event.uid, "ok")
                currentUserExDTO.userDTO?.eventTicketIds?.add(event.uid.toString())
                firebaseViewModel.updateUserEventTicketIds(currentUserExDTO.userDTO!!) {

                }

                //initTicketTimer()

                runEvents.remove(event.uid.toString())
                Toast.makeText(this, "투표권이 ${event.count}장 추가되었습니다.", Toast.LENGTH_SHORT).show()
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

    private fun onUpdateDialog(jobType: MaintenanceDialog.JobType, version: String) {
        val maintenanceDialog = MaintenanceDialog(this, jobType)
        maintenanceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        maintenanceDialog.setCanceledOnTouchOutside(false)
        maintenanceDialog.updateDTO = firebaseViewModel.updateDTO.value
        maintenanceDialog.currentVersion = version
        maintenanceDialog.show()
        maintenanceDialog.binding.buttonMaintenanceOk.setOnClickListener {
            maintenanceDialog.dismiss()
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(firebaseViewModel.updateDTO.value?.updateUrl)
                setPackage("com.android.vending")
            }
            startActivity(intent)
            finish() //액티비티 종료
        }
        maintenanceDialog.binding.buttonMaintenanceCancel.setOnClickListener {
            maintenanceDialog.dismiss()
            Toast.makeText(this, "업데이트가 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /*private fun initTicketTimer() {
        if (firebaseViewModel.preferencesDTO.value?.ticketChargeCount!! > ticketCount) { // 이미 충전된 투표권을 뺏지 않기 위해서 조건 추가
            resetTicketTimer()
        } else {
            if (isTicketTimerStart)
                ticketCountDownTimer.cancel()
            isTicketTimerStart = false
            binding.textTicketCount.text = "${decimalFormat.format(ticketCount)}"
            binding.textTicketTimer.text = ""
        }
    }*/

    private fun resetTicketTimer() {
        // 티켓 충전시간 파일에서 읽기
        var chargeTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_TICKET_CHARGE_TIME, 0)
        var interval = (chargeTime + firebaseViewModel.preferencesDTO.value!!.getIntervalTimeMillis()) - System.currentTimeMillis()
        println("타이머 $interval $chargeTime, ${firebaseViewModel.preferencesDTO.value!!.getIntervalTimeMillis()}, ${firebaseViewModel.preferencesDTO.value!!.IntervalTime}, ${System.currentTimeMillis()}")

        // 타이머가 동작중이면 종료 후 다시 실행
        if (isTicketTimerStart)
            ticketCountDownTimer.cancel()

        isTicketTimerStart = true
        ticketCountDownTimer = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                isTicketTimerStart = false
                //ticketCount.value = ticketCount.value?.plus(1)
                println("타이머 티켓충전  ${currentUserExDTO.userDTO?.uid.toString()}")
                firebaseViewModel.addUserTicket(currentUserExDTO.userDTO?.uid.toString(), 1) {

                }

                // 충전 시간, 티켓 카운트 기록
                sharedPreferences.putLong(MySharedPreferences.PREF_KEY_TICKET_CHARGE_TIME, System.currentTimeMillis())



                /*if (firebaseViewModel.preferencesDTO.value?.ticketChargeCount!! > ticketCount.value!!) { // 티켓 충전이 가능하다면 타이머 다시 호출
                    binding.textTicketCount.text = "${decimalFormat.format(ticketCount)}"
                    resetTicketTimer()
                } else { // 티켓 충전 Full
                    binding.textTicketCount.text = "${decimalFormat.format(ticketCount)}"
                    binding.textTicketTimer.text = ""
                }*/

                /*text_ticket_count.text = "${decimalFormat.format(ticketCount)}"
                text_ticket_timer.text = ""
                isTimerStart = false*/
            }

            override fun onTick(millisUntilFinished: Long) {
                var totalsec = millisUntilFinished / 1000
                var hour = totalsec / 3600
                var min = (totalsec % 3600) / 60
                var sec = totalsec % 60

                binding.textTicketTimer.text = "(${String.format("%02d",min)}:${String.format("%02d", sec)})"
            }

        }.start()
    }

    private fun rotatedBitmap(fd: FileDescriptor, bitmap: Bitmap?): Bitmap? {
        val matrix = Matrix()
        when(getOrientationOfImage(fd)){
            0 -> matrix.setRotate(0F)
            90 -> matrix.setRotate(90F)
            180 -> matrix.setRotate(180F)
            270 -> matrix.setRotate(270F)
        }
        var resultBitmap : Bitmap? = try{
            bitmap?.let { Bitmap.createBitmap(it, 0, 0, bitmap.width, bitmap.height, matrix, true) }
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
        return resultBitmap
    }

    private fun getOrientationOfImage(fd: FileDescriptor): Int? {
        var exif: ExifInterface?
        var result: Int? = null

        try{
            exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExifInterface(fd)
            } else {
                return -1
            }
        }catch (e: Exception){
            e.printStackTrace()
            return -1
        }

        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
        if(orientation != -1){
            result = when(orientation){
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
        return result
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun decodeSampledBitmapFileDescriptor(
        fd: FileDescriptor,
        reqWidth: Int,
        reqHeight: Int,
    ): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(fd, null, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            rotatedBitmap(fd, BitmapFactory.decodeFileDescriptor(fd, null, this))
        }
    }

    private fun updateCheck() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)

        println("업데이트 시작")
        appUpdateManager.let {
            it.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                println("업데이트 ? ${appUpdateInfo.updateAvailability()}")
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                    // or AppUpdateType.FLEXIBLE
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE, // or AppUpdateType.FLEXIBLE
                        this,
                        100
                    )
                }
            }
        }
    }

    private fun autoTimeCheck() : Boolean {
        val result = android.provider.Settings.Global.getInt(contentResolver, android.provider.Settings.Global.AUTO_TIME, 0)
        return if (result == 1) {
            println("DateTime Sync: On")
            true
        } else {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "[날짜 및 시간 자동설정]을 사용하지 않으면 리매치를 이용할 수 없습니다.",
                "설정에서 [날짜 및 시간 자동설정]을 사용함으로 변경 후 리매치를 이용해 주세요.\n자세한 설정 방법은 아래 링크를 참조하세요.\nhttps://info-hub.tistory.com/13"
            )
            onAppExit(question)
            false
        }
    }

    private fun failedLoading() {
        val question = QuestionDTO(
            QuestionDTO.Stat.ERROR,
            "데이터 로딩 실패",
            "데이터 로딩에 실패하였습니다.\n리매치 종료 후 다시 실행해 주세요."
        )
        onAppExit(question)
    }

    private fun loginCheck() {
        oldUserDTO = intent.getParcelableExtra("user")
        if (oldUserDTO == null) {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "사용자 확인 실패",
                "로그인 정보가 올바르지 않습니다.\n리매치를 종료합니다."
            )
            onAppExit(question)
        }

        /*if (oldUserDTO?.fanClubId.isNullOrEmpty()) {
            successLoadingCount = successLoadingCount.minus(2)
        }*/
    }

    // 프리미엄 패키지 확인 및 UI 출력
    private fun checkPremium() {
        if (firebaseViewModel.userDTO.value?.isPremium()!!) {
            binding.imgPremiumEnable.visibility = View.VISIBLE
            binding.imgPremiumDisable.visibility = View.GONE
            when {
                firebaseViewModel.userDTO.value?.isPremiumRenew()!! || !firebaseViewModel.userDTO.value?.isPremiumGemGet()!! -> { // 프리미엄 패키지 갱신 기간이거나 매일 다이아 수령을 안했다면 알림 표시
                    binding.imgPremiumNew.visibility = View.VISIBLE
                }
                else -> {
                    binding.imgPremiumNew.visibility = View.GONE
                }
            }
        } else {
            binding.imgPremiumEnable.visibility = View.GONE
            binding.imgPremiumDisable.visibility = View.VISIBLE
            binding.imgPremiumNew.visibility = View.VISIBLE
        }
    }

    // 일일 퀘스트 수령할 보상이 있다면 UI 출력
    private fun checkQuestNew() {
        var successCount = 0
        var isQuestNew = false
        for (i in 1..firebaseViewModel.userDTO.value?.questSuccessTimes?.size!!) { // 1 ~ 8
            val quest = QuestDTO("", "", 1, firebaseViewModel.userDTO.value?.questSuccessTimes?.get("$i"), firebaseViewModel.userDTO.value?.questGemGetTimes?.get("$i"))
            if (quest.isQuestSuccess()) { // 퀘스트 완료 했는데 보상 수령안한 항목
                successCount++
                if (!quest.isQuestGemGet()) {
                    isQuestNew = true
                    break
                }
            }
        }

        if (!isQuestNew) {
            val quest = QuestDTO("", "", 1, null, firebaseViewModel.userDTO.value?.questGemGetTimes?.get("0"))
            if (successCount == firebaseViewModel.userDTO.value?.questSuccessTimes?.size && !quest.isQuestGemGet()) {
                isQuestNew = true
            }
        }

        if (isQuestNew) {
            binding.imgQuestNew.visibility = View.VISIBLE
        } else {
            binding.imgQuestNew.visibility = View.GONE
        }
    }

    // 로딩해야 할 데이터라면 true 반환, 아니라면 false 반환
    private fun checkLoadingData(number: Int) : Boolean {
        return if (number <= successLoadingCount) {
            loadingData[number] == false
        } else
            false
    }

    // 중요 데이터 로딩
    private fun loadingMainData() {
        // 1. 광고 설정 획득
        if (checkLoadingData(1)) {
            firebaseViewModel.getAdPolicy()
            println("[01.로딩] 광고 설정 획득")
        }

        // 2. 환경 설정 획득
        /*if (checkLoadingData(2)) {
            firebaseViewModel.stopPreferencesListen()
            firebaseViewModel.getPreferencesListen()
            println("[02.로딩] 환경 설정 획득")
        }*/

        // 3. 업데이트 설정 획득
        if (checkLoadingData(3)) {
            firebaseViewModel.stopServerUpdateListen()
            firebaseViewModel.getServerUpdateListen()
            println("[03.로딩] 업데이트 설정 획득")
        }

        // 4. 사용자 정보 획득
        if (checkLoadingData(4)) {
            firebaseViewModel.stopUserListen()
            firebaseViewModel.getUserListen(oldUserDTO?.uid.toString())
            println("[04.로딩] 사용자 정보 획득")
        }

        // 5. 전광판 리스트 획득
        if (checkLoadingData(5)) {
            //firebaseViewModel.stopDisplayBoardListen()
            //firebaseViewModel.getDisplayBoardListen() // 전광판 리스트 획득
            firebaseViewModel.stopDisplayBoardsListen()
            firebaseViewModel.getDisplayBoardsListen() // 전광판 리스트 획득
            println("[05.로딩] 전광판 리스트 획득")
        }

        // 6. 메일 리스트 획득
        if (checkLoadingData(6)) {
            firebaseViewModel.stopMailsListen()
            firebaseViewModel.getMailsListen(oldUserDTO?.uid.toString())
            println("[06.로딩] 메일 리스트 획득")
        }

        // 7. 시즌 정보 획득
        if (checkLoadingData(7)) {
            firebaseViewModel.getSeason()
            println("[07.로딩] 시즌 정보 획득")
        }

        // 8. 이벤트 티켓 획득
        if (checkLoadingData(8)) {
            firebaseViewModel.stopEventTicketListen()
            firebaseViewModel.getEventTicketListen()
            //println("[04.로딩] 공지사항 획득")
            println("[08.로딩] 이벤트 티켓 획득")
        }
    }

    // 중요 데이터 로딩
    private fun observeMainData() {
        // 1. 광고 설정 획득
        firebaseViewModel.adPolicyDTO.observe(this) {
            loadingData[1] = true
            println("[01.로딩] 광고 획득 성공")

            adsBannerManager = AdsBannerManager(this, lifecycle, firebaseViewModel.adPolicyDTO.value!!, binding.adViewAdmob, binding.xmladview, binding.adViewKakao)
            adsBannerManager?.callBanner {

            }
        }

        // 2. 환경 설정 획득
        /*firebaseViewModel.preferencesDTO.observe(this) {
            loadingData[2] = true
            println("[02.로딩] 환경 설정 획득 성공")
        }*/

        // 3. 업데이트 설정 획득
        observeUpdate()

        // 4. 사용자 정보 획득
        observeUser()
        /*if (checkLoadingData(4)) {
            firebaseViewModel.stopUserListen()
            firebaseViewModel.getUserListen(oldUserDTO?.uid.toString())
            println("[04.로딩] 사용자 정보 획득")
        }*/

        // 5. 전광판 리스트 획득
        observeDisplayBoard()

        // 6. 메일 리스트 획득
        observeMail()

        // 7. 시즌 정보 획득
        firebaseViewModel.seasonDTO.observe(this) {
            loadingData[7] = true
            println("[07.로딩] 시즌 정보 획득 성공")
        }

        // 8. 이벤트 티켓 획득
        observeEventTickets()
    }

    // 사용자 차단 확인
    private fun checkBlock() {
        if (firebaseViewModel.userDTO.value?.isBlock()!!) {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "회원님의 계정은 이용이 제한되었습니다.",
                "제한 일시 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(firebaseViewModel.userDTO.value?.blockStartTime!!)}\n" +
                        "해제 일시 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(firebaseViewModel.userDTO.value?.blockEndTime!!)}\n" +
                        "제한 사유 : ${firebaseViewModel.userDTO.value?.blockReason}\n\n" +
                        "리매치를 종료합니다."
            )
            onAppExit(question)
        }
    }

    // 회원 탈퇴 확인
    private fun checkDeleteAccount() {
        if (firebaseViewModel.userDTO.value?.deleteTime != null) {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "탈퇴처리된 사용자입니다.", "리매치를 종료합니다."
            )
            firebaseAuth?.signOut()
            googleSignInClient?.signOut()?.addOnCompleteListener { }
            onAppExit(question)
        }
    }

    // 로그인 시간이 변경되었으면 중복 로그인으로 판단하여 종료
    private fun checkMultiLogin() {
        if (oldUserDTO?.loginTime != firebaseViewModel.userDTO.value?.loginTime) {
            // 로그아웃 처리됨
            firebaseAuth?.signOut()
            //Auth.GoogleSignInApi.signOut()
            googleSignInClient?.signOut()?.addOnCompleteListener { }

            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "중복 로그인",
                "다른 기기에서 해당 계정으로 접속하여 리매치를 종료합니다."
            )
            onAppExit(question)
        }
    }

    // 비회원 사용자
    private fun checkNotMember() {
        if (firebaseViewModel.userDTO.value?.loginType == UserDTO.LoginType.NON_MEMBER) {
            // 비회원 복구 ID 저장 여부
            val saveUid = sharedPreferences.getBooleanSubKey(MySharedPreferences.PREF_KEY_NON_MEMBER_SAVE_UID, firebaseViewModel.userDTO.value?.uid, false)
            if (!saveUid) {
                var dialog = NonMemberSaveUidDialog(this, firebaseViewModel.userDTO.value!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()

                dialog.binding.buttonOk.setOnClickListener {
                    if (dialog.isDisabled) {
                        sharedPreferences.putBooleanSubKey(MySharedPreferences.PREF_KEY_NON_MEMBER_SAVE_UID, firebaseViewModel.userDTO.value?.uid, true)
                    }
                    dialog.dismiss()
                }
            }

            // 비회원 닉네임 변경 여부
            if (firebaseViewModel.userDTO.value?.nicknameChangeDate == null) {
                val item = EditTextDTO("비회원 계정을 사용중입니다! 기억하기 쉬운 닉네임으로 변경해주세요.", firebaseViewModel.userDTO.value?.nickname, 15, "^[가-힣ㄱ-ㅎa-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_-]{1,15}\$", "사용할 수 없는 문자열이 포함되어 있습니다.")

                var editTextModifyDialog = EditTextModifyDialog(this, item)
                editTextModifyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                editTextModifyDialog.setCanceledOnTouchOutside(false)
                editTextModifyDialog.show()
                editTextModifyDialog.setInfo()
                editTextModifyDialog.binding.buttonModifyCancel.setOnClickListener { // No
                    editTextModifyDialog.dismiss()
                }
                editTextModifyDialog.binding.buttonModifyOk.setOnClickListener { // Ok
                    val nickname = editTextModifyDialog.binding.editContent.text.toString().trim()
                    val question = QuestionDTO(
                        QuestionDTO.Stat.WARNING,
                        "닉네임 변경",
                        "[${nickname}] 으로 변경하시겠습니까?",
                    )
                    if (questionDialog == null) {
                        questionDialog = QuestionDialog(this, question)
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
                    questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                        questionDialog?.dismiss()
                        questionDialog = null

                        firebaseViewModel.isUsedUserNickname(nickname) { isUsed ->
                            if (isUsed) {
                                callToast("닉네임이 이미 존재합니다.")
                            } else {
                                editTextModifyDialog?.dismiss()
                                val oldNickname = firebaseViewModel.userDTO.value?.nickname
                                if (firebaseViewModel.userDTO.value?.nickname != nickname) {
                                    loading()

                                    firebaseViewModel.userDTO.value?.nickname = nickname
                                    firebaseViewModel.updateUserNickname(firebaseViewModel.userDTO.value!!, 0) { userDTO ->
                                        if (userDTO != null) {
                                            firebaseViewModel.userDTO.value = userDTO

                                            var log = LogDTO("닉네임 변경 ($oldNickname -> $nickname)", Date())
                                            firebaseViewModel.writeUserLog(firebaseViewModel.userDTO.value?.uid.toString(), log) { }

                                            loadingEnd()

                                            callToast("닉네임 변경 완료!")
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

    private fun setViewPager() {
        fragmentPageMusic = FragmentPageMusic()

        binding.viewPager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
        binding.viewPager.apply {
            adapter = MyPagerAdapter(
                fragmentPageMusic,
                context as FragmentActivity
            )
            setPageTransformer(ZoomOutPageTransformer())
        }

        TabLayoutMediator(
            binding.tabs,
            binding.viewPager
        ) { tab, position ->
            //tab.text = "${tabLayoutText[position]}"
            //tab.setIcon(tabIcon[position])
            tab.setCustomView(R.layout.custom_tab)
            (tab.customView!!.findViewById(R.id.text_title) as TextView).text = "${tabLayoutText[position]}"
            (tab.customView!!.findViewById(R.id.img_icon) as ImageView).setImageResource(tabIcon[position])

        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when (position) {
                    //2 -> {
                    3 -> { // 팬클럽 적용
                        showAdCtrl(false)
                    }
                    else -> {
                        showAdCtrl(true)
                        // 전체 화면으로 동영상 시청 중 탭 변경 시 화면 원상복귀 시킴
                        showMainCtrl(true) // 메인 컨트롤 복구
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //세로 화면으로 고정
                        fragmentPageMusic.onPause()
                    }
                }
            }
        })
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    //<editor-fold desc="@ Fragment 참조 함수">
    fun backPressed() {
        if(System.currentTimeMillis() - backWaitTime >=2000 ) {
            backWaitTime = System.currentTimeMillis()
            Snackbar.make(binding.layoutMain,"'뒤로' 버튼을 한번 더 누르면 앱이 종료됩니다.", Snackbar.LENGTH_LONG).show()
        } else {
            appExit() // 앱 종료
        }
    }

    fun loading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
            loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadingDialog?.setCanceledOnTouchOutside(false)
        }
        loadingDialog?.show()
    }

    fun loadingEnd() {
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

    fun onAppExit(question: QuestionDTO) {
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

    // 광고 설정
    fun getAdPolicy() : AdPolicyDTO {
        return firebaseViewModel.adPolicyDTO.value!!
    }

    // 환경 설정
    fun getPreferences() : PreferencesDTO {
        return firebaseViewModel.preferencesDTO.value!!
    }

    fun getSeason() : SeasonDTO {
        return firebaseViewModel.seasonDTO.value!!
    }

    fun getUser() : UserDTO {
        return firebaseViewModel.userDTO.value!!
    }

    fun getUserEx() : UserExDTO? {
        return currentUserExDTO
    }

    fun getBitmap(uri: Uri): Bitmap? {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor

        return decodeSampledBitmapFileDescriptor(fileDescriptor, 300, 300)
    }

    /*fun getFavoriteProfileList() : ArrayList<String> {
        var favoriteProfileList = arrayListOf<String>()
        for (favorite in firebaseViewModel.userDTO.value!!.favorites) {
            favoriteImageList[favorite]?.let { favoriteProfileList.add(it) }
        }
        return favoriteProfileList
    }*/

    fun getFanClub() : FanClubDTO? {
        return firebaseViewModel.fanClubDTO.value
    }

    fun setDisplayBoardVisible(visibility: Boolean) {
        if (visibility) {
            binding.displayBoard.layoutMain.visibility = View.VISIBLE
            callToast("전광판 표시")
            sharedPreferences.putBoolean(MySharedPreferences.PREF_KEY_DISPLAY_BOARD_VISIBILITY, true)
        } else {
            binding.displayBoard.layoutMain.visibility = View.GONE
            callToast("전광판 숨김")
            sharedPreferences.putBoolean(MySharedPreferences.PREF_KEY_DISPLAY_BOARD_VISIBILITY, false)
        }
    }

    //</editor-fold>
}
