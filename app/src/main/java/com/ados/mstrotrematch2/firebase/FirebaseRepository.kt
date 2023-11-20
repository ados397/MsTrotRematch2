package com.ados.mstrotrematch2.firebase

import android.os.SystemClock
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.MutableLiveData
import com.ados.mstrotrematch2.database.DBHelperReport
import com.ados.mstrotrematch2.dialog.DonationCertificateDialog
import com.ados.mstrotrematch2.model.*
import com.ados.mstrotrematch2.util.Utility
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class FirebaseRepository() {
    private val TAG = "FirebaseRepository"
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    //<editor-fold desc="@ 변수 선언">

    enum class PeopleOrder {
        COUNT_ASC, COUNT_DESC, NAME_ASC, NAME_DESC
    }

    enum class FanClubOrder {
        MEMBER_ASC, MEMBER_DESC, NAME_ASC, NAME_DESC, EXP_ASC, EXP_DESC
    }

    enum class GemType {
        PAID_GEM, FREE_GEM
    }

    enum class MemberType {
        ALL, MEMBER_ONLY, GUEST_ONLY, ADMIN_ONLY
    }

    enum class CheeringBoardType {
        POPULAR, NEW, STATISTICS
    }

    val adPolicyDTO = MutableLiveData<AdPolicyDTO?>() // 광고 설정
    val preferencesDTO = MutableLiveData<PreferencesDTO?>() // 환경 설정
    var preferencesDTOListener : ListenerRegistration? = null
    val userDTO = MutableLiveData<UserDTO?>() // 사용자 정보
    var userDTOListener : ListenerRegistration? = null
    val fanClubDTO = MutableLiveData<FanClubDTO?>() // 팬클럽 정보
    var fanClubDTOListener : ListenerRegistration? = null
    val fanClubChatDTO = MutableLiveData<DisplayBoardDTO?>() // 팬클럽 채팅 정보
    var fanClubChatDTOListener : ListenerRegistration? = null
    val fanClubChatDTOs = MutableLiveData<ArrayList<DisplayBoardDTO>>() // 팬클럽 채팅 리스트
    var fanClubChatDTOsListener : ListenerRegistration? = null
    val displayBoardDTO = MutableLiveData<DisplayBoardDTO?>() // 전광판 정보
    var displayBoardDTOListener : ListenerRegistration? = null
    val displayBoardDTOs = MutableLiveData<ArrayList<DisplayBoardDTO>>() // 전광판 리스트
    var displayBoardDTOsListener : ListenerRegistration? = null
    val eventDTOs = MutableLiveData<ArrayList<EventDTO>>() // 이벤트 티켓 리스트
    var eventDTOsListener : ListenerRegistration? = null
    val mailDTOs = MutableLiveData<ArrayList<MailDTO>>() // 메일 리스트
    var mailDTOsListener : ListenerRegistration? = null
    val updateDTO = MutableLiveData<UpdateDTO?>() // 업데이트 설정
    var updateDTOListener : ListenerRegistration? = null
    val seasonDTO = MutableLiveData<SeasonDTO?>() // 시즌 정보
    var seasonDTOListener : ListenerRegistration? = null
    val peopleDTOs = MutableLiveData<ArrayList<RankDTO>>() // 가수 리스트
    val jsonMovie = MutableLiveData<String>() // 유튜브 동영상 리스트
    val boardDTOsPopular = MutableLiveData<ArrayList<BoardDTO>>() // 응원글 리스트(인기순)
    val boardDTOsNew = MutableLiveData<ArrayList<BoardDTO>>() // 응원글 리스트(최신순)
    val rankDTOsStatistics = MutableLiveData<ArrayList<RankDTO>>() // 응원글 가수별 통계 리스트
    val faqDTOs = MutableLiveData<ArrayList<FaqDTO>>() // 자주 묻는 질문 리스트 정보
    val qnaDTOs = MutableLiveData<ArrayList<QnaDTO>>() // 내가 문의한 리스트 정보
    val noticeSubDTO = MutableLiveData<NoticeDTO>() // 서브 공지
    val hallOfFameVote = MutableLiveData<ArrayList<RankDTO>>() // 명예의 전당 (득표수)
    val hallOfFameCheering = MutableLiveData<ArrayList<RankDTO>>() // 명예의 전당 (응원글)
    val fanClubDTOs = MutableLiveData<ArrayList<FanClubDTO>>() // 팬클럽 리스트
    val token = MutableLiveData<String>() // 토큰 정보

    // Firestore 초기화
    private val firestore = FirebaseFirestore.getInstance()

    // FireMessaging 초기화
    private val fireMessaging = FirebaseMessaging.getInstance()

    private var lastVisible: DocumentSnapshot? = null
    private var lastVisiblePopular: DocumentSnapshot? = null // 응원글(인기순) 페이징 쿼리 커서
    private var lastVisibleNew: DocumentSnapshot? = null // 응원글(최신순) 페이징 쿼리 커서

    //</editor-fold>

    // 페이징 변수 초기화
    fun lastVisibleRemove() {
        lastVisible = null
        lastVisiblePopular = null
        lastVisibleNew = null
    }

    // 응원글(인기순) 페이징 변수 초기화
    fun lastVisiblePopularRemove() {
        lastVisiblePopular = null
    }

    // 응원글(쵯힌순) 페이징 변수 초기화
    fun lastVisibleNewRemove() {
        lastVisibleNew = null
    }

    //<editor-fold desc="@ 데이터 획득 함수">

    // 환경 설정 불러오기 (실시간)
    fun getPreferencesListen() {
        if (preferencesDTOListener == null) {
            preferencesDTOListener = firestore.collection("preferences").document("preferences").addSnapshotListener { documentSnapshot, _ ->
                if (documentSnapshot == null) return@addSnapshotListener
                val preferences = documentSnapshot.toObject(PreferencesDTO::class.java)
                preferencesDTO.value = preferences
            }
        }
    }

    // 환경 설정 불러오기 (실시간) 중지
    fun stopPreferencesListen() {
        if (preferencesDTOListener != null) {
            preferencesDTOListener?.remove()
            preferencesDTOListener = null
            preferencesDTO.value = null
        }
    }

    // 사용자 불러오기(실시간)
    fun getUserListen(uid: String) {
        if (userDTOListener == null) {
            userDTOListener = firestore.collection("user").document(uid).addSnapshotListener { documentSnapshot, _ ->
                if (documentSnapshot == null || !documentSnapshot.exists()) return@addSnapshotListener
                val user = documentSnapshot.toObject(UserDTO::class.java)
                userDTO.value = user
            }
        }
    }

    // 사용자 불러오기(실시간) 중지
    fun stopUserListen() {
        if (userDTOListener != null) {
            userDTOListener?.remove()
            userDTOListener = null
            userDTO.value = null
        }
    }

    // 팬클럽 정보 불러오기(실시간)
    fun getFanClubListen(fanClubId: String) {
        if (fanClubDTOListener == null) {
            fanClubDTOListener = firestore.collection("fanClub").document(fanClubId).addSnapshotListener { documentSnapshot, _ ->
                if (documentSnapshot == null || !documentSnapshot.exists()) return@addSnapshotListener
                val fanClub = documentSnapshot.toObject(FanClubDTO::class.java)
                fanClubDTO.value = fanClub
            }
        }
    }

    // 팬클럽 정보 불러오기(실시간) 중지
    fun stopFanClubListen() {
        if (fanClubDTOListener != null) {
            fanClubDTOListener?.remove()
            fanClubDTOListener = null
            fanClubDTO.value = null
        }
    }

    // 팬클럽 채팅 리스트 불러오기(실시간)
    fun getFanClubChatsListen(fanClubId: String, fanClubJoinDate: Date) {
        if (fanClubChatDTOsListener == null) {
            val limit = 30L
            fanClubChatDTOsListener = firestore.collection("fanClub")
                .document(fanClubId).collection("chat").whereGreaterThan("createTime", fanClubJoinDate).orderBy("createTime", Query.Direction.DESCENDING)
                .limit(limit).addSnapshotListener { querySnapshot, _ ->
                    if (querySnapshot == null) return@addSnapshotListener

                    var displayBoards : ArrayList<DisplayBoardDTO> = arrayListOf()
                    for(snapshot in querySnapshot){
                        var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)
                        displayBoards.add(displayBoard)
                    }
                    if (displayBoards.size < limit) {
                        while (displayBoards.size <= limit) {
                            displayBoards.add(DisplayBoardDTO("", ""))
                        }
                        /*for (i in displayBoards.size..limit) {
                            displayBoards.add(DisplayBoardDTO("", ""))
                        }*/
                    }
                    fanClubChatDTOs.value = displayBoards
                }
        }
    }

    // 팬클럽 채팅 리스트 불러오기(실시간) 중지
    fun stopFanClubChatsListen() {
        if (fanClubChatDTOsListener != null) {
            fanClubChatDTOsListener?.remove()
            fanClubChatDTOsListener = null
            fanClubChatDTOs.value = arrayListOf()
        }
    }

    // 전광판 불러오기(실시간)
    fun getDisplayBoardListen() {
        if (displayBoardDTOListener == null) {
            displayBoardDTOListener = firestore.collection("displayBoard").orderBy("order", Query.Direction.DESCENDING).limit(1).addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot){
                    var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)
                    displayBoardDTO.value = displayBoard
                }
            }
        }
    }

    // 전광판 불러오기(실시간) 중지
    fun stopDisplayBoardListen() {
        if (displayBoardDTOListener != null) {
            displayBoardDTOListener?.remove()
            displayBoardDTOListener = null
            displayBoardDTO.value = null
        }
    }

    // 전광판 리스트 불러오기(실시간)
    fun getDisplayBoardsListen() {
        if (displayBoardDTOsListener == null) {
            val limit = 30L
            displayBoardDTOsListener = firestore.collection("displayBoard").orderBy("order", Query.Direction.DESCENDING).limit(limit).addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot == null) return@addSnapshotListener

                var displayBoards : ArrayList<DisplayBoardDTO> = arrayListOf()
                for(snapshot in querySnapshot){
                    var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)
                    displayBoards.add(displayBoard)
                }
                if (displayBoards.size < limit) {
                    while (displayBoards.size <= limit) {
                        displayBoards.add(DisplayBoardDTO("", ""))
                    }
                    /*for (i in displayBoards.size..limit) {
                        displayBoards.add(DisplayBoardDTO("", ""))
                    }*/
                }
                displayBoardDTOs.value = displayBoards
            }
        }
    }

    // 전광판 리스트 불러오기(실시간) 중지
    fun stopDisplayBoardsListen() {
        if (displayBoardDTOsListener != null) {
            displayBoardDTOsListener?.remove()
            displayBoardDTOsListener = null
            displayBoardDTOs.value = arrayListOf()
        }
    }

    // 이벤트 티켓 불러오기(실시간)
    fun getEventTicketListen() {
        if (eventDTOsListener == null) {
            eventDTOsListener = firestore.collection("event").addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot == null) return@addSnapshotListener
                var now = Date()
                var eventTickets : ArrayList<EventDTO> = arrayListOf()
                for (snapshot in querySnapshot) {
                    var event = snapshot.toObject(EventDTO::class.java)!!

                    // 수령 시간이 안지난 유효 티켓만 저장
                    if (now < event.limit) {
                        eventTickets.add(event)
                    }
                }
                eventDTOs.value = eventTickets
            }
        }
    }

    // 이벤트 티켓 불러오기(실시간) 중지
    fun stopEventTicketListen() {
        if (eventDTOsListener != null) {
            eventDTOsListener?.remove()
            eventDTOsListener = null
            eventDTOs.value = arrayListOf()
        }
    }

    // 메일 리스트 불러오기(실시간)
    fun getMailsListen(uid: String) {
        if (mailDTOsListener == null) {
            mailDTOsListener = firestore.collection("user").document(uid).collection("mail").whereEqualTo("deleted", false).addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot == null) return@addSnapshotListener

                var mails : ArrayList<MailDTO> = arrayListOf()
                val date = Date()
                for(snapshot in querySnapshot){
                    var mail = snapshot.toObject(MailDTO::class.java)

                    // 유효한 우편만 획득
                    if (date < mail.expireTime && !mail.deleted) {
                        mails.add(mail)
                    }
                }
                mails.sortByDescending { it.expireTime }
                mailDTOs.value = mails
            }
        }
    }

    // 메일 리스트 불러오기(실시간) 중지
    fun stopMailsListen() {
        if (mailDTOsListener != null) {
            mailDTOsListener?.remove()
            mailDTOsListener = null
            mailDTOs.value = arrayListOf()
        }
    }

    // 팬클럽 채팅 불러오기(실시간)
    fun getFanClubChatListen(fanClubId: String, fanClubJoinDate: Date) {
        if (fanClubChatDTOListener == null) {
            fanClubChatDTOListener = firestore.collection("fanClub")
                .document(fanClubId).collection("chat").whereGreaterThan("createTime", fanClubJoinDate).orderBy("createTime", Query.Direction.DESCENDING)
                .limit(1).addSnapshotListener { querySnapshot, _ ->
                    if (querySnapshot == null) return@addSnapshotListener
                    for(snapshot in querySnapshot){
                        var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)
                        fanClubChatDTO.value = displayBoard
                    }
                }
        }
    }

    // 팬클럽 채팅 불러오기(실시간) 중지
    fun stopFanClubChatListen() {
        if (fanClubChatDTOListener != null) {
            fanClubChatDTOListener?.remove()
            fanClubChatDTOListener = null
            fanClubChatDTO.value = null
        }
    }

    // 업데이트 및 서버점검 체크 (실시간)
    fun getServerUpdateListen() {
        if (updateDTOListener == null) {
            updateDTOListener = firestore.collection("preferences").document("updateInfo").addSnapshotListener { documentSnapshot, _ ->
                if (documentSnapshot == null) return@addSnapshotListener
                val update = documentSnapshot.toObject(UpdateDTO::class.java)
                updateDTO.value = update
            }
        }
    }

    // 업데이트 및 서버점검 체크 (실시간) 중지
    fun stopServerUpdateListen() {
        if (updateDTOListener != null) {
            updateDTOListener?.remove()
            updateDTOListener = null
            updateDTO.value = null
        }
    }

    // 시즌 정보 불러오기(실시간)
    fun getSeasonListen() {
        if (seasonDTOListener == null) {
            seasonDTOListener = firestore.collection("preferences").document("season").addSnapshotListener { documentSnapshot, _ ->
                if (documentSnapshot == null) return@addSnapshotListener
                val season = documentSnapshot.toObject(SeasonDTO::class.java)
                seasonDTO.value = season
            }
        }
    }

    // 시즌 정보 불러오기(실시간) 중지
    fun stopSeasonListen() {
        if (seasonDTOListener != null) {
            seasonDTOListener?.remove()
            seasonDTOListener = null
            seasonDTO.value = null
        }
    }

    // 광고 설정 불러오기
    fun getAdPolicy() {
        firestore.collection("preferences").document("ad_policy").get().addOnCompleteListener { task ->
            println("광고 정보 요청")
            if (task.isSuccessful && task.result.exists()) {
                println("광고 정보 성공")
                adPolicyDTO.value = task.result.toObject(AdPolicyDTO::class.java)
            } else {
                println("광고 정보 실패")
            }
        }
    }

    // 토큰 정보 불러오기
    fun getToken() {
        fireMessaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            token.value = task.result
        })
    }

    // 개인 출석체크 업데이트
    fun updateUserCheckout(uid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(uid).update("checkoutTime", Date()).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 팬클럽 출석체크 업데이트
    fun updateFanClubCheckout(uid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(uid).update("fanClubCheckoutTime", Date()).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 이메일(ID) 사용유무 확인 (true: 사용중, false: 미사용중)
    fun findUserFromEmail(email: String, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        firestore.collection("user").whereEqualTo("userId", email).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.size() > 0) {
                for (document in task.result) { // 사용자 찾음
                    user = document.toObject(UserDTO::class.java)
                    myCallback(user)
                }
            } else { // 사용자 못 찾음
                myCallback(user)
            }
        }
    }

    // 사용자 닉네임 사용유무 확인 (true: 사용중, false: 미사용중)
    fun isUsedUserNickname(nickname: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").whereEqualTo("nickname", nickname).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.size() > 0) { // 이름 사용중
                myCallback(true)
            } else { // 사용 가능한 이름
                myCallback(false)
            }
        }
    }

    // 이용약관 불러오기
    fun getTermsOfUse(myCallback: (String) -> Unit) {
        firestore.collection("admin").document("documents").get().addOnCompleteListener { task ->
            var doc = ""
            if (task.isSuccessful && task.result.exists()) {
                if (task.result.contains("terms_of_use")) {
                    doc = task.result["terms_of_use"] as String
                }
            }
            //documents.value = doc.replace("\\n","\n")
            myCallback(doc.replace("\\n","\n"))
        }
    }

    // 개인정보 처리방침 불러오기
    fun getPrivacyPolicy(myCallback: (String) -> Unit) {
        firestore.collection("admin").document("documents").get().addOnCompleteListener { task ->
            var doc = ""
            if (task.isSuccessful && task.result.exists()) {
                if (task.result.contains("privacy_policy")) {
                    doc = task.result["privacy_policy"] as String
                }
            }
            //documents.value = doc.replace("\\n","\n")
            myCallback(doc.replace("\\n","\n"))
        }
    }

    // 오픈소스 라이선스 불러오기
    fun getOpenSourceLicense(myCallback: (String) -> Unit) {
        firestore.collection("admin").document("documents").get().addOnCompleteListener { task ->
            var doc = ""
            if (task.isSuccessful && task.result.exists()) {
                if (task.result.contains("open_source_license")) {
                    doc = task.result["open_source_license"] as String
                }
            }
            //documents.value = doc.replace("\\n","\n")
            myCallback(doc.replace("\\n","\n"))
        }
    }

    // 비회원 약관 불러오기
    fun getNonMemberDocument(myCallback: (String) -> Unit) {
        firestore.collection("admin").document("documents").get().addOnCompleteListener { task ->
            var doc = ""
            if (task.isSuccessful && task.result.exists()) {
                if (task.result.contains("non_member")) {
                    doc = task.result["non_member"] as String
                }
            }
            //documents.value = doc.replace("\\n","\n")
            myCallback(doc.replace("\\n","\n"))
        }
    }

    // 시즌 정보 불러오기
    fun getSeason() {
        firestore.collection("preferences").document("season").get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                println("광고 정보 성공")
                seasonDTO.value = task.result.toObject(SeasonDTO::class.java)
            }
        }
    }

    // 사용자 불러오기
    fun getUser(uid: String, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        firestore.collection("user").document(uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                user = task.result.toObject(UserDTO::class.java)
            }
            myCallback(user)
        }
    }

    // 사용자 전광판 오늘 등록횟수 불러오기
    fun getUserDisplayBoardWriteCount(uid: String, myCallback: (Long) -> Unit) {
        var nowDate = SimpleDateFormat("yyyyMMdd").format(Date())
        var count = 0L
        firestore.collection("user").document(uid).collection("otherOption").document("displayBoardWriteCount").get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                if (task.result.contains(nowDate)) {
                    count = task.result[nowDate] as Long
                }
            }
            myCallback(count)
        }
    }

    // 팬클럽 불러오기
    fun getFanClub(fanClubId: String, myCallback: (FanClubDTO?) -> Unit) {
        var fanClub: FanClubDTO? = null
        firestore.collection("fanClub").document(fanClubId).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                fanClub = task.result.toObject(FanClubDTO::class.java)
            }
            myCallback(fanClub)
        }
    }

    // 팬클럽 출석체크 인원 불러오기
    fun getFanClubCheckoutCount(fanClubId: String, myCallback: (Long) -> Unit) {
        var nowDate = SimpleDateFormat("yyyyMMdd").format(Date())
        var count = 0L
        firestore.collection("fanClub").document(fanClubId).collection("otherOption").document("checkoutCount").get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                if (task.result.contains(nowDate)) {
                    count = task.result[nowDate] as Long
                }
            }
            myCallback(count)
        }
    }

    // 자주 묻는 질문 리스트 획득(우선순위순)
    fun getFaq() {
        firestore.collection("faq").orderBy("order", Query.Direction.ASCENDING).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var faqList : ArrayList<FaqDTO> = arrayListOf()
                for (document in task.result) {
                    var faq = document.toObject(FaqDTO::class.java)
                    faqList.add(faq)
                }
                faqDTOs.value = faqList
            }
        }
    }

    // 내가 문의한 리스트 획득(날짜순)
    fun getQna(uid: String) {
        firestore.collection("qna").whereEqualTo("userUid", uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var qnaList : ArrayList<QnaDTO> = arrayListOf()
                for (document in task.result) {
                    var qna = document.toObject(QnaDTO::class.java)
                    qnaList.add(qna)
                }
                qnaList.sortByDescending { it.createTime }
                qnaDTOs.value = qnaList
            }
        }
    }

    // 가수 리스트 불러오기
    fun getPeople(order: PeopleOrder) {
        var tsDoc = when (order) {
                PeopleOrder.COUNT_ASC -> firestore.collection("people").orderBy("count", Query.Direction.ASCENDING)
                PeopleOrder.COUNT_DESC -> firestore.collection("people").orderBy("count", Query.Direction.DESCENDING)
                PeopleOrder.NAME_ASC -> firestore.collection("people").orderBy("name", Query.Direction.ASCENDING)
                PeopleOrder.NAME_DESC -> firestore.collection("people").orderBy("name", Query.Direction.DESCENDING)
        }

        tsDoc.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var people : ArrayList<RankDTO> = arrayListOf()
                for (document in task.result) {
                    var person = document.toObject(RankDTO::class.java)
                    people.add(person)
                }
                peopleDTOs.value = people
            }
        }
    }

    // 명예의 전당 (득표수) 리스트 불러오기
    fun getHallOfFameVote(docName: String) {
        firestore.collection("season_result").document(docName).collection("vote").orderBy("count", Query.Direction.DESCENDING).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var people : ArrayList<RankDTO> = arrayListOf()
                for (document in task.result) {
                    var person = document.toObject(RankDTO::class.java)
                    people.add(person)
                }
                hallOfFameVote.value = people
            }
        }
    }

    // 명예의 전당 (응원글) 리스트 불러오기
    fun getHallOfFameCheering(docName: String) {
        firestore.collection("season_result").document(docName).collection("cheering").orderBy("cheeringCountTotal", Query.Direction.DESCENDING).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var people: ArrayList<RankDTO> = arrayListOf()
                for (document in task.result) {
                    var person = document.toObject(RankDTO::class.java)!!
                    people.add(person)
                }
                hallOfFameCheering.value = people
            }
        }
    }

    // 명예의 전당 기부내역 불러오기
    fun getDonationNews(docName: String, rankDocName: String, myCallback: (ArrayList<DonationNewsDTO>) -> Unit) {
        firestore.collection("season_result").document(docName).collection("vote").document(rankDocName).collection("donationNews").orderBy("order", Query.Direction.ASCENDING).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var donationNews: ArrayList<DonationNewsDTO> = arrayListOf()
                for (document in task.result) {
                    var news = document.toObject(DonationNewsDTO::class.java)!!
                    donationNews.add(news)
                }
                myCallback(donationNews)
            }
        }
    }

    // 유튜브 동영상 리스트 불러오기
    fun getMovieList() {
        firestore.collection("json").document("popular_list").get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                if (task.result.contains("body")) {
                    jsonMovie.value = task.result["body"] as String
                }
            }
        }
    }

    // 응원글 리스트 불러오기
    fun getCheeringBoard(dbHandler : DBHelperReport, seasonNum: Int, seasonWeek: Int, type: CheeringBoardType, favoriteProfileList: ArrayList<String>?) {
        val field = if (type == CheeringBoardType.POPULAR) { // 인기순
            "likeCount"
        } else { // 최신순
            "time"
        }

        var lastVisible = if (type == CheeringBoardType.POPULAR) { // 인기순
            lastVisiblePopular
        } else { // 최신순
            lastVisibleNew
        }

        val documentName = "season${String.format("%04d",seasonNum)}"
        val collectionName = "week${String.format("%04d",seasonWeek)}"

        /*var favoriteProfileList = arrayListOf<String>() // 최애가수 리스트 프로필명으로 저장
        favoriteProfileList.add("profile39")
        favoriteProfileList.add("profile23")*/

        val tsDoc = if (favoriteProfileList == null) {
            firestore.collection("cheering").document(documentName).collection(collectionName).orderBy(field, Query.Direction.DESCENDING)
        } else {
            firestore.collection("cheering").document(documentName).collection(collectionName).whereIn("peopleDocName", favoriteProfileList).orderBy(field, Query.Direction.DESCENDING)
        }

        //val tsDoc = firestore.collection("cheering").document(documentName).collection(collectionName).orderBy(field, Query.Direction.DESCENDING)
        //val tsDoc = firestore.collection("cheering").document(documentName).collection(collectionName).whereIn("image", favoriteProfileList).orderBy(field, Query.Direction.DESCENDING)
        //val tsDoc = firestore.collection("cheering").document(documentName).collection(collectionName).whereIn("image", mutableListOf("profile35", "profile23"))
        //val tsDoc = firestore.collection("cheeringboard_s13").orderBy(field, Query.Direction.DESCENDING)
        if (lastVisible == null) {
            tsDoc.limit(30).get().addOnSuccessListener { result ->
            //tsDoc.get().addOnSuccessListener { result ->
                var boards : ArrayList<BoardDTO> = arrayListOf() // 새로운 데이터 추가
                for (document in result) {
                    var board = document.toObject(BoardDTO::class.java)!!
                    board.isBlock = dbHandler.getBlock(board.docname.toString())
                    boards.add(board)
                }
                if (result.size() > 0) {
                    lastVisible = result.documents[result.size() - 1]
                }

                if (type == CheeringBoardType.POPULAR) { // 인기순
                    boardDTOsPopular.value = boards
                } else { // 최신순
                    boardDTOsNew.value = boards
                }
            }.addOnFailureListener {
            }
        } else {
            tsDoc.startAfter(lastVisible!!).limit(30).get().addOnSuccessListener { result ->
                var boards = if (type == CheeringBoardType.POPULAR) { // 인기순
                    boardDTOsPopular.value!!
                } else { // 최신순
                    boardDTOsNew.value!!
                }
                for (document in result) {
                    var board = document.toObject(BoardDTO::class.java)!!
                    board.isBlock = dbHandler.getBlock(board.docname.toString())
                    boards.add(board) // 기존 데이터에 추가
                }
                if (result.size() > 0) {
                    lastVisible = result.documents[result.size() - 1]
                }

                if (type == CheeringBoardType.POPULAR) { // 인기순
                    boardDTOsPopular.value = boards
                } else { // 최신순
                    boardDTOsNew.value = boards
                }
            }.addOnFailureListener {
            }
        }

    }

    // 응원글 리스트 불러오기(인기순)
    fun getCheeringBoardPopular(dbHandler : DBHelperReport, seasonNum: Int, seasonWeek: Int, favoriteProfileList: ArrayList<String>?) {
        val documentName = "season${String.format("%04d",seasonNum)}"
        val collectionName = "week${String.format("%04d",seasonWeek)}"

        val tsDoc = if (favoriteProfileList == null) {
            firestore.collection("cheering").document(documentName).collection(collectionName).orderBy("likeCount", Query.Direction.DESCENDING)
            //firestore.collection("cheeringboard_s6").orderBy("likeCount", Query.Direction.DESCENDING)
        } else {
            firestore.collection("cheering").document(documentName).collection(collectionName).whereIn("peopleDocName", favoriteProfileList).orderBy("likeCount", Query.Direction.DESCENDING)
        }
        if (lastVisiblePopular == null) {
            tsDoc.limit(30).get().addOnSuccessListener { result ->
                var boards : ArrayList<BoardDTO> = arrayListOf() // 새로운 데이터 추가
                for (document in result) {
                    var board = document.toObject(BoardDTO::class.java)!!
                    board.isBlock = dbHandler.getBlock(board.docname.toString())
                    boards.add(board)
                }
                boardDTOsPopular.value = boards

                if (result.size() > 0) {
                    lastVisiblePopular = result.documents[result.size() - 1]
                }
            }.addOnFailureListener {
            }
        } else {
            tsDoc.startAfter(lastVisiblePopular!!).limit(30).get().addOnSuccessListener { result ->
                var boards = boardDTOsPopular.value!!
                for (document in result) {
                    var board = document.toObject(BoardDTO::class.java)!!
                    board.isBlock = dbHandler.getBlock(board.docname.toString())
                    boards.add(board) // 기존 데이터에 추가
                }
                boardDTOsPopular.value = boards

                if (result.size() > 0) {
                    lastVisiblePopular = result.documents[result.size() - 1]
                }
            }.addOnFailureListener {
            }
        }
    }

    // 응원글 리스트 불러오기(최신순)
    fun getCheeringBoardNew(dbHandler : DBHelperReport, seasonNum: Int, seasonWeek: Int, favoriteProfileList: ArrayList<String>?) {
        val documentName = "season${String.format("%04d",seasonNum)}"
        val collectionName = "week${String.format("%04d",seasonWeek)}"

        val tsDoc = if (favoriteProfileList == null) {
            firestore.collection("cheering").document(documentName).collection(collectionName).orderBy("time", Query.Direction.DESCENDING)
        } else {
            firestore.collection("cheering").document(documentName).collection(collectionName).whereIn("peopleDocName", favoriteProfileList).orderBy("time", Query.Direction.DESCENDING)
        }
        if (lastVisibleNew == null) {
            tsDoc.limit(30).get().addOnSuccessListener { result ->
                var boards : ArrayList<BoardDTO> = arrayListOf() // 새로운 데이터 추가
                for (document in result) {
                    var board = document.toObject(BoardDTO::class.java)!!
                    board.isBlock = dbHandler.getBlock(board.docname.toString())
                    boards.add(board)
                }
                boardDTOsNew.value = boards

                if (result.size() > 0) {
                    lastVisibleNew = result.documents[result.size() - 1]
                }
            }.addOnFailureListener {
            }
        } else {
            tsDoc.startAfter(lastVisibleNew!!).limit(30).get().addOnSuccessListener { result ->
                var boards = boardDTOsNew.value!!
                for (document in result) {
                    var board = document.toObject(BoardDTO::class.java)!!
                    board.isBlock = dbHandler.getBlock(board.docname.toString())
                    boards.add(board) // 기존 데이터에 추가
                }
                boardDTOsNew.value = boards

                if (result.size() > 0) {
                    lastVisibleNew = result.documents[result.size() - 1]
                }
            }.addOnFailureListener {
            }
        }

    }

    // 응원글 통계 불러오기
    fun getCheeringStatistics() {
        firestore.collection("people_cheering").orderBy("cheeringCountTotal", Query.Direction.DESCENDING).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var people: ArrayList<RankDTO> = arrayListOf() // 새로운 데이터 추가
                for (document in task.result) {
                    var person = document.toObject(RankDTO::class.java)!!
                    people.add(person)
                }
                rankDTOsStatistics.value = people
            }
        }
    }

    // 서브 공지 불러오기
    fun getNoticeSub(myCallback: (NoticeDTO?) -> Unit) {
        var notice: NoticeDTO? = null
        firestore.collection("preferences").document("notice").get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                notice = task.result.toObject(NoticeDTO::class.java)
            }
            myCallback(notice)
        }
    }

    // 팬클럽 리스트 불러오기
    fun getFanClub(order: FanClubOrder) {
        var tsDoc = when (order) {
            FanClubOrder.MEMBER_ASC -> firestore.collection("fanClub").orderBy("memberCount", Query.Direction.ASCENDING)
            FanClubOrder.MEMBER_DESC -> firestore.collection("fanClub").orderBy("memberCount", Query.Direction.DESCENDING)
            FanClubOrder.NAME_ASC -> firestore.collection("fanClub").orderBy("name", Query.Direction.ASCENDING)
            FanClubOrder.NAME_DESC -> firestore.collection("fanClub").orderBy("name", Query.Direction.DESCENDING)
            FanClubOrder.EXP_ASC -> firestore.collection("fanClub").orderBy("exp", Query.Direction.DESCENDING)
            FanClubOrder.EXP_DESC -> firestore.collection("fanClub").orderBy("exp", Query.Direction.DESCENDING)
        }

        tsDoc.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var fanClub : ArrayList<FanClubDTO> = arrayListOf()
                for (document in task.result) {
                    var club = document.toObject(FanClubDTO::class.java)
                    fanClub.add(club)
                }
                fanClubDTOs.value = fanClub
            }
        }
    }

    // 나의 투표 수 불러오기
    fun getVoteCount(uid: String, seasonNum: Int, peopleDocName: String, myCallback: (Long) -> Unit) {
        val documentName = "season${String.format("%04d",seasonNum)}"
        var count = 0L
        firestore.collection("user").document(uid).collection("vote").document(documentName).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                if (task.result.contains(peopleDocName)) {
                    count = task.result[peopleDocName] as Long
                }
            }
            myCallback(count)
        }
    }

    // 나의 전체 투표 수 불러오기
    fun getVoteCountTotal(uid: String, peopleDocName: String, myCallback: (Long) -> Unit) {
        var count = 0L
        firestore.collection("user").document(uid).collection("vote").get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                for (document in task.result) {
                    if (document.contains(peopleDocName)) {
                        count += document[peopleDocName] as Long
                    }
                }
            }
            myCallback(count)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 트랜잭션 함수">

    // 투표 하기
    fun addVoteTicket(docName: String, voteCount: Int, myCallback: (RankDTO?) -> Unit) {
        var rank: RankDTO? = null
        var tsDoc = firestore.collection("people").document(docName)
        firestore.runTransaction { transaction ->
            rank = transaction.get(tsDoc).toObject(RankDTO::class.java)
            rank?.count = rank?.count?.plus(voteCount)

            // 100만으로 나눠서 100만 이상일 때 최초 1회 전광판에 공지를 함
            // 공지 후 celebrateCount에 기록함, 2번 중복되지 않도록
            val celebrateCount = rank?.count?.div(1000000)!!
            if (rank?.celebrateCount == null) { // 초창기 버전 예외 처리
                rank?.celebrateCount = celebrateCount
            } else {
                val oldCelebrateCount = rank?.celebrateCount!!
                if (oldCelebrateCount < celebrateCount) {
                    val countString = Utility.getNumKorString(rank?.count?.div(1000000)?.times(1000000)!!) // 100만 단위 이하 절삭을 위해 나눴다가 다시 곱함
                    sendDisplayBoard("\uD83C\uDF8A경축\uD83C\uDF8A ${rank?.name} \uD83D\uDD25${countString}표\uD83D\uDD25 달성!! \uD83C\uDF89축하드립니다\uD83C\uDF89", -16711702, UserDTO(nickname="시스템")) {
                    }
                    rank?.celebrateCount = celebrateCount
                }
            }

            transaction.set(tsDoc, rank!!)
        }.addOnSuccessListener {
            myCallback(rank)
        }.addOnFailureListener {
            myCallback(rank)
        }
    }

    // 사용자 티켓 추가
    fun addUserTicket(uid: String, ticketCount: Int, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user").document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc).toObject(UserDTO::class.java)

            user?.ticketCount = user?.ticketCount?.plus(ticketCount)

            transaction.set(tsDoc, user!!)
        }.addOnSuccessListener {
            myCallback(user)
        }.addOnFailureListener {
            myCallback(user)
        }
    }

    // 사용자 티켓 사용
    fun useUserTicket(uid: String, ticketCount: Int, seasonNum: Int, peopleDocName: String, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user").document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc).toObject(UserDTO::class.java)

            val userTicketCount = user?.ticketCount?.minus(ticketCount)!!
            if (userTicketCount <= 0) {
                user?.ticketCount = 0
            } else {
                user?.ticketCount = userTicketCount
            }

            updateUserVoteStatistics(uid, ticketCount, seasonNum, peopleDocName) { }

            transaction.set(tsDoc, user!!)
        }.addOnSuccessListener {
            myCallback(user)
        }.addOnFailureListener {
            myCallback(user)
        }
    }


    // 사용자 티켓 사용 기록
    fun updateUserVoteStatistics(uid: String, ticketCount: Int, seasonNum: Int, peopleDocName: String, myCallback: (Boolean) -> Unit) {
        val documentName = "season${String.format("%04d",seasonNum)}"
        var count = 0L
        var tsDoc = firestore.collection("user").document(uid).collection("vote").document(documentName)
        tsDoc.get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                if (task.result.contains(peopleDocName)) {
                     count = task.result[peopleDocName] as Long
                }
            }
            count = count.plus(ticketCount)
            if (!task.result.exists()) { // document 없으면 생성
                val docData = hashMapOf(peopleDocName to count)
                tsDoc.set(docData).addOnCompleteListener {
                    myCallback(true)
                }
            } else { // document 있으면 기존 데이터에 update
                tsDoc.update(peopleDocName, count).addOnCompleteListener {
                    myCallback(true)
                }
            }
        }

    }

    // 사용자 다이아 추가 (gemType : PAID_GEM 유료 다이아 추가, FREE_GEM 무료 다이아 추가)
    fun addUserGem(uid: String, paidGemCount: Int, freeGemCount: Int, firstPack: String?, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user").document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc).toObject(UserDTO::class.java)

            if (!firstPack.isNullOrEmpty()) { // 첫 구매 패키지 적용
                user!!.firstGemPackage[firstPack] = false
            }

            if (paidGemCount > 0) { // 유료 다이아 추가
                user?.paidGem = user?.paidGem?.plus(paidGemCount)
            }

            if (freeGemCount > 0) { // 무료 다이아 추가
                user?.freeGem = user?.freeGem?.plus(freeGemCount)
            }

            transaction.set(tsDoc, user!!)
        }.addOnSuccessListener {
            myCallback(user)
        }.addOnFailureListener {
            myCallback(user)
        }
    }

    // 사용자 다이아 소비
    fun useUserGem(uid: String, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user").document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc).toObject(UserDTO::class.java)

            user?.useGem(gemCount) // 다이아 차감

            transaction.set(tsDoc, user!!)
        }.addOnSuccessListener {
            myCallback(user)
        }.addOnFailureListener {
            myCallback(user)
        }
    }

    // 사용자 전광판 오늘 등록횟수 추가
    fun updateUserDisplayBoardWriteCount(uid: String, myCallback: (Long?) -> Unit) {
        var newCount : Long? = null
        var nowDate = SimpleDateFormat("yyyyMMdd").format(Date())
        var tsDoc = firestore.collection("user").document(uid).collection("otherOption").document("displayBoardWriteCount")
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(tsDoc)
            newCount = snapshot.getLong(nowDate)
            newCount = if (newCount == null) {
                1L
            } else {
                newCount?.plus(1)
            }

            //transaction.update(tsDoc, nowDate, newCount)
            val docData = hashMapOf(nowDate to newCount)
            transaction.set(tsDoc, docData)
        }.addOnSuccessListener {
            myCallback(newCount)
        }.addOnFailureListener {
            myCallback(newCount)
        }
    }

    // 프리미엄 패키지 구매
    fun applyPremiumPackage(uid: String, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user").document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc).toObject(UserDTO::class.java)

            val calendar= Calendar.getInstance()
            // 프리미엄 패키지 만료전에 갱신 시 남은 날짜 + 30일
            if (user?.premiumExpireTime!! > calendar.time) {
                calendar.time = user?.premiumExpireTime!!
                calendar.add(Calendar.DATE, 30)
            } else { // 새로 구입 시 오늘 날짜 + 29일 (총 30일)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.add(Calendar.DATE, 29)
            }
            user?.premiumExpireTime = calendar.time // 프리미엄 패키지 유효기간 적용

            transaction.set(tsDoc, user!!)
        }.addOnSuccessListener {
            myCallback(user)
        }.addOnFailureListener {
            myCallback(user)
        }
    }

    // 응원글 좋아요 +-
    fun setCheeringBoardLike(docName: String, seasonNum: Int, seasonWeek: Int, likeCount: Int, myCallback: (BoardDTO?) -> Unit) {
        var board: BoardDTO? = null
        val documentName = "season${String.format("%04d",seasonNum)}"
        val collectionName = "week${String.format("%04d",seasonWeek)}"
        val tsDoc = firestore.collection("cheering").document(documentName).collection(collectionName).document(docName)
        //val tsDoc = firestore.collection("cheeringboard_s13").document(docName)
        firestore.runTransaction { transaction ->
            board = transaction.get(tsDoc).toObject(BoardDTO::class.java)

            board?.likeCount = board?.likeCount?.plus(likeCount)

            transaction.set(tsDoc, board!!)
        }.addOnSuccessListener {
            myCallback(board)
        }.addOnFailureListener {
            myCallback(board)
        }
    }

    // 팬클럽 총 인원 업데이트
    fun updateFanClubMemberCount(fanClubId: String, count: Long, myCallback: (Long?) -> Unit) {
        var newCount : Long? = null
        var tsDoc = firestore.collection("fanClub").document(fanClubId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(tsDoc)
            newCount = snapshot.getLong("memberCount")?.plus(count)

            transaction.update(tsDoc, "memberCount", newCount)
        }.addOnSuccessListener {
            myCallback(newCount)
        }.addOnFailureListener {
            myCallback(newCount)
        }
    }

    // 팬클럽 출석체크 인원 업데이트
    fun updateFanClubCheckoutCount(fanClubId: String, myCallback: (Long?) -> Unit) {
        var newCount : Long? = null
        var nowDate = SimpleDateFormat("yyyyMMdd").format(Date())
        var tsDoc = firestore.collection("fanClub").document(fanClubId).collection("otherOption").document("checkoutCount")
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(tsDoc)
            newCount = snapshot.getLong(nowDate)
            newCount = if (newCount == null) {
                1L
            } else {
                newCount?.plus(1)
            }

            //transaction.update(tsDoc, nowDate, newCount)
            val docData = hashMapOf(nowDate to newCount)
            transaction.set(tsDoc, docData)
        }.addOnSuccessListener {
            myCallback(newCount)
        }.addOnFailureListener {
            myCallback(newCount)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 업데이트 및 Set 함수">

    // 1:1 문의하기
    fun sendQna(qna: QnaDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("qna").document().set(qna).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 정보 전체 업데이트
    fun updateUser(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).set(user).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 즐겨찾기 리스트 업데이트
    fun updateUserFavorites(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("favorites", user.favorites).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 이벤트 티켓 수령 ID 리스트 업데이트
    fun updateUserEventTicketIds(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("eventTicketIds", user.eventTicketIds).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 메일 발송
    fun sendUserMail(uid: String, mail: MailDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(uid)
            .collection("mail").document(mail.docName.toString()).set(mail).addOnCompleteListener {
                myCallback(true)
            }
    }

    // 전광판 등록
    fun sendDisplayBoard(displayText: String, color: Int, user: UserDTO, myCallback: (Boolean) -> Unit) {
        // 마지막 전광판 항목을 찾아서 order 증가
        firestore.collection("displayBoard").orderBy("order", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    var displayBoardDTO = document.toObject(DisplayBoardDTO::class.java)

                    var newDisplayBoard = DisplayBoardDTO()
                    newDisplayBoard.docName = Utility.randomDocumentName()
                    newDisplayBoard.displayText = displayText
                    newDisplayBoard.userUid = user.uid
                    newDisplayBoard.userNickname = user.nickname
                    newDisplayBoard.color = color
                    newDisplayBoard.order = displayBoardDTO.order?.plus(1)
                    newDisplayBoard.createTime = Date()

                    firestore.collection("displayBoard").document(newDisplayBoard.docName.toString()).set(newDisplayBoard).addOnCompleteListener {
                        updateUserDisplayBoardWriteCount(user.uid.toString()) { }
                        myCallback(true)
                    }
                }
            }
        }
    }

    // 팬클럽 채팅 전송
    fun sendFanClubChat(fanClubId: String, chat: DisplayBoardDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")
            .document(fanClubId).collection("chat").document().set(chat).addOnCompleteListener {
                myCallback(true)
            }
    }

    // 사용자 프리미엄 패키지 다이아 수령 시간 기록
    fun updateUserPremiumGemGetTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("premiumGemGetTime", user.premiumGemGetTime).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 메인타이틀 업데이트
    fun updateUserMainTitle(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("mainTitle", user.mainTitle).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 내 소개 업데이트
    fun updateUserAboutMe(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("aboutMe", user.aboutMe).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 프로필 업데이트
    fun updateUserProfile(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("imgProfile", user.imgProfile).addOnCompleteListener {
            firestore.collection("user").document(user.uid.toString()).update("imgProfileUpdateTime", Date()).addOnCompleteListener {
                myCallback(true)
            }
        }
    }

    // 사용자 닉네임 업데이트
    fun updateUserNickname(user: UserDTO, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        var resultUser: UserDTO? = null
        var tsDoc = firestore.collection("user").document(user.uid.toString())
        firestore.runTransaction { transaction ->
            resultUser = transaction.get(tsDoc).toObject(UserDTO::class.java)

            resultUser?.nickname = user.nickname
            resultUser?.nicknameChangeDate = Date()

            if (gemCount > 0) {
                resultUser?.useGem(gemCount) // 다이아 차감
            }

            transaction.set(tsDoc, resultUser!!)
        }.addOnSuccessListener {
            myCallback(resultUser)
        }.addOnFailureListener {
            myCallback(resultUser)
        }
    }

    // 사용자 일일 과제 달성 업데이트
    fun updateUserQuestSuccessTimes(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("questSuccessTimes", user.questSuccessTimes).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 일일 과제 보상 업데이트
    fun updateUserQuestGemGetTimes(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("questGemGetTimes", user.questGemGetTimes).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 토큰 업데이트
    fun updateUserToken(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("token", user.token).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 로그인 시간 기록
    fun updateUserLoginTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("loginTime", user.loginTime).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 메일 읽음 상태로 업데이트
    fun updateUserMailRead(uid: String, mailUid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(uid)
            .collection("mail").document(mailUid).update("read", true).addOnCompleteListener {
                myCallback(true)
            }
    }

    // 사용자 메일 삭제 상태로 업데이트
    fun updateUserMailDelete(uid: String, mailUid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(uid)
            .collection("mail").document(mailUid).update("deleted", true).addOnCompleteListener {
                myCallback(true)
            }
    }

    // 사용자 회원 탈퇴
    fun updateUserDeleteTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("deleteTime", Date()).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 가입된 팬클럽 정보 업데이트
    fun updateUserFanClubId(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(user.uid.toString()).update("fanClubId", user.fanClubId).addOnCompleteListener {
            if (user.fanClubId == null) { // 팬클럽 탈퇴
                firestore.collection("user").document(user.uid.toString()).update("fanClubQuitDate", Date()).addOnCompleteListener {
                    myCallback(true)
                }
            } else { // 팬클럽 가입
                firestore.collection("user").document(user.uid.toString()).update("fanClubJoinDate", Date()).addOnCompleteListener {
                    myCallback(true)
                }
            }
        }
    }


    // 응원하기 통계 데이터 취합
    fun updateCheeringStatistics(seasonNum: Int, seasonWeek: Int, myCallback: (Boolean) -> Unit) {
        val documentName = "season${String.format("%04d",seasonNum)}"
        val collectionName = "week${String.format("%04d",seasonWeek)}"
        val tsDoc = firestore.collection("cheering").document(documentName).collection(collectionName)
        //val tsDoc = firestore.collection("cheeringboard_s13")
        var nowDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        firestore.collection("people_cheering").get().addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!

                println("취합 : ${person.name}")
                SystemClock.sleep(500)
                tsDoc.whereEqualTo("image", person.image).get().addOnSuccessListener { result ->
                    println("취합 : 갯수 ${result.size()}")
                    person.cheeringCount = result.size()
                    person.likeCount = 0
                    person.dislikeCount = 0
                    person.updateDate = Date()
                    for (document in result) {
                        var post = document.toObject(BoardDTO::class.java)!!
                        if (post.likeCount!! < 55555) {
                            person.likeCount = person.likeCount!!.plus(post.likeCount!!)
                            person.dislikeCount = person.dislikeCount!!.plus(post.dislikeCount!!)
                        }
                    }

                    // 전체 점수는 응원글 50점, 좋아요 1점
                    person.cheeringCountTotal = person.cheeringCount!!.times(50).plus(person.likeCount!!)

                    firestore.collection("people_cheering").document(person.docname.toString()).set(person)
                }.addOnFailureListener {
                }
            }
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    // 응원글 추가
    fun addCheeringBoard(boardDTO: BoardDTO, seasonNum: Int, seasonWeek: Int, myCallback: (Boolean) -> Unit) {
        val documentName = "season${String.format("%04d",seasonNum)}"
        val collectionName = "week${String.format("%04d",seasonWeek)}"
        val tsDoc = firestore.collection("cheering").document(documentName).collection(collectionName).document(boardDTO.docname.toString())
        //val tsDoc = firestore.collection("cheeringboard_s13").document(boardDTO.docname.toString())
        tsDoc.set(boardDTO).addOnCompleteListener {
            myCallback(true)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 삭제 함수">

    // 응원글 삭제
    fun deleteCheeringBoard(docName: String, seasonNum: Int, seasonWeek: Int, myCallback: (Boolean) -> Unit) {
        val documentName = "season${String.format("%04d",seasonNum)}"
        val collectionName = "week${String.format("%04d",seasonWeek)}"
        val tsDoc = firestore.collection("cheering").document(documentName).collection(collectionName).document(docName)
        //val tsDoc = firestore.collection("cheeringboard_s13").document(docName)
        tsDoc.delete().addOnCompleteListener {
            myCallback(true)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 로그 기록 함수">

    // 신고
    fun sendReport(report: ReportDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("admin").document("report")
            .collection(report.getCollectionName()).document().set(report).addOnCompleteListener {
                myCallback(true)
            }
    }

    // 사용자 로그 작성
    fun writeUserLog(uid: String, log: LogDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user").document(uid)
            .collection("log").document().set(log).addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 로그 작성
    fun writeFanClubLog(fanClubId: String, log: LogDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub").document(fanClubId)
            .collection("log").document().set(log).addOnCompleteListener {
                myCallback(true)
            }
    }

    // 관리자 로그 작성
    fun writeAdminLog(log: LogDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("adminLog").document().set(log).addOnCompleteListener {
            myCallback(true)
        }
    }

    //</editor-fold>
}