package com.ados.mstrotrematch2.firebase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ados.mstrotrematch2.database.DBHelperReport
import com.ados.mstrotrematch2.model.*
import java.util.*

class FirebaseViewModel(application: Application) : AndroidViewModel(application) {

    //<editor-fold desc="@ 변수 선언">

    private val repository : FirebaseRepository = FirebaseRepository()
    val adPolicyDTO = repository.adPolicyDTO
    val preferencesDTO = repository.preferencesDTO
    val userDTO = repository.userDTO
    val fanClubDTO = repository.fanClubDTO
    val fanClubChatDTO = repository.fanClubChatDTO
    val fanClubChatDTOs = repository.fanClubChatDTOs
    val displayBoardDTO = repository.displayBoardDTO
    val displayBoardDTOs = repository.displayBoardDTOs
    val eventDTOs = repository.eventDTOs
    val mailDTOs = repository.mailDTOs
    val updateDTO = repository.updateDTO
    val seasonDTO = repository.seasonDTO
    val peopleDTOs = repository.peopleDTOs
    val jsonMovie = repository.jsonMovie
    val boardDTOsPopular = repository.boardDTOsPopular
    val boardDTOsNew = repository.boardDTOsNew
    val rankDTOsStatistics = repository.rankDTOsStatistics
    val faqDTOs = repository.faqDTOs
    val qnaDTOs = repository.qnaDTOs
    val noticeSubDTO = repository.noticeSubDTO
    val hallOfFameVote = repository.hallOfFameVote
    val hallOfFameCheering = repository.hallOfFameCheering
    val fanClubDTOs = repository.fanClubDTOs
    val token = repository.token

    //</editor-fold>

    // 페이징 변수 초기화
    fun lastVisibleRemove() {
        repository.lastVisibleRemove()
    }

    // 응원글(인기순) 페이징 변수 초기화
    fun lastVisiblePopularRemove() {
        repository.lastVisiblePopularRemove()
    }

    // 응원글(쵯힌순) 페이징 변수 초기화
    fun lastVisibleNewRemove() {
        repository.lastVisibleNewRemove()
    }

    //<editor-fold desc="@ 데이터 획득 함수">

    // 광고 설정 불러오기
    fun getAdPolicy() {
        repository.getAdPolicy()
    }

    // 환경 설정 불러오기 (실시간)
    fun getPreferencesListen() {
        repository.getPreferencesListen()
    }

    // 환경 설정 불러오기 (실시간) 중지
    fun stopPreferencesListen() {
        repository.stopPreferencesListen()
    }

    // 사용자 불러오기(실시간)
    fun getUserListen(uid: String) {
        repository.getUserListen(uid)
    }

    // 사용자 불러오기(실시간) 중지
    fun stopUserListen() {
        repository.stopUserListen()
    }

    // 팬클럽 정보 불러오기(실시간)
    fun getFanClubListen(fanClubId: String) {
        repository.getFanClubListen(fanClubId)
    }

    // 팬클럽 정보 불러오기(실시간) 중지
    fun stopFanClubListen() {
        repository.stopFanClubListen()
    }

    // 팬클럽 채팅 리스트 불러오기(실시간)
    fun getFanClubChatsListen(fanClubId: String, fanClubJoinDate: Date) {
        repository.getFanClubChatsListen(fanClubId, fanClubJoinDate)
    }

    // 팬클럽 채팅 리스트 불러오기(실시간) 중지
    fun stopFanClubChatsListen() {
        repository.stopFanClubChatsListen()
    }

    // 전광판 불러오기(실시간)
    fun getDisplayBoardListen() {
        repository.getDisplayBoardListen()
    }

    // 전광판 불러오기(실시간) 중지
    fun stopDisplayBoardListen() {
        repository.stopDisplayBoardListen()
    }

    // 전광판 리스트 불러오기(실시간)
    fun getDisplayBoardsListen() {
        repository.getDisplayBoardsListen()
    }

    // 전광판 리스트 불러오기(실시간) 중지
    fun stopDisplayBoardsListen() {
        repository.stopDisplayBoardsListen()
    }

    // 이벤트 티켓 불러오기(실시간)
    fun getEventTicketListen() {
        repository.getEventTicketListen()
    }

    // 이벤트 티켓 불러오기(실시간) 중지
    fun stopEventTicketListen() {
        repository.stopEventTicketListen()
    }

    // 메일 리스트 불러오기(실시간)
    fun getMailsListen(uid: String) {
        repository.getMailsListen(uid)
    }

    // 메일 리스트 불러오기(실시간) 중지
    fun stopMailsListen() {
        repository.stopMailsListen()
    }

    // 팬클럽 채팅 불러오기(실시간)
    fun getFanClubChatListen(fanClubId: String, fanClubJoinDate: Date) {
        repository.getFanClubChatListen(fanClubId, fanClubJoinDate)
    }

    // 팬클럽 채팅 불러오기(실시간) 중지
    fun stopFanClubChatListen() {
        repository.stopFanClubChatListen()
    }

    // 업데이트 및 서버점검 체크 (실시간)
    fun getServerUpdateListen() {
        repository.getServerUpdateListen()
    }

    // 업데이트 및 서버점검 체크 (실시간) 중지
    fun stopServerUpdateListen() {
        repository.stopServerUpdateListen()
    }

    // 시즌 정보 불러오기(실시간)
    fun getSeasonListen() {
        repository.getSeasonListen()
    }

    // 시즌 정보 불러오기(실시간) 중지
    fun stopSeasonListen() {
        repository.stopSeasonListen()
    }

    // 이용약관 불러오기
    fun getTermsOfUse(myCallback: (String) -> Unit) {
        repository.getTermsOfUse {
            myCallback(it)
        }
    }

    // 개인정보 처리방침 불러오기
    fun getPrivacyPolicy(myCallback: (String) -> Unit) {
        repository.getPrivacyPolicy {
            myCallback(it)
        }
    }

    // 오픈소스 라이선스 불러오기
    fun getOpenSourceLicense(myCallback: (String) -> Unit) {
        repository.getOpenSourceLicense {
            myCallback(it)
        }
    }

    // 비회원 이용약관 불러오기
    fun getNonMemberDocument(myCallback: (String) -> Unit) {
        repository.getNonMemberDocument {
            myCallback(it)
        }
    }

    // 시즌 정보 불러오기
    fun getSeason() {
        repository.getSeason()
    }

    // 사용자 불러오기
    fun getUser(uid: String, myCallback: (UserDTO?) -> Unit) {
        repository.getUser(uid) {
            myCallback(it)
        }
    }

    // 사용자 전광판 오늘 등록횟수 불러오기
    fun getUserDisplayBoardWriteCount(uid: String, myCallback: (Long) -> Unit) {
        repository.getUserDisplayBoardWriteCount(uid) {
            myCallback(it)
        }
    }

    // 팬클럽 불러오기
    fun getFanClub(fanClubId: String, myCallback: (FanClubDTO?) -> Unit) {
        repository.getFanClub(fanClubId) {
            myCallback(it)
        }
    }

    // 팬클럽 출석체크 인원 불러오기
    fun getFanClubCheckoutCount(fanClubId: String, myCallback: (Long) -> Unit) {
        repository.getFanClubCheckoutCount(fanClubId) {
            myCallback(it)
        }
    }

    // 자주 묻는 질문 리스트 획득(우선순위순)
    fun getFaq() {
        repository.getFaq()
    }

    // 내가 문의한 리스트 획득(날짜순)
    fun getQna(uid: String) {
        repository.getQna(uid)
    }

    // 가수 리스트 불러오기
    fun getPeople(order: FirebaseRepository.PeopleOrder) {
        repository.getPeople(order)
    }

    // 명예의 전당 (득표수) 리스트 불러오기
    fun getHallOfFameVote(docName: String) {
        repository.getHallOfFameVote(docName)
    }

    // 명예의 전당 (응원글) 리스트 불러오기
    fun getHallOfFameCheering(docName: String) {
        repository.getHallOfFameCheering(docName)
    }

    // 명예의 전당 기부내역 불러오기
    fun getDonationNews(docName: String, rankDocName: String, myCallback: (ArrayList<DonationNewsDTO>) -> Unit) {
        repository.getDonationNews(docName, rankDocName) {
            myCallback(it)
        }
    }

    // 토큰 정보 불러오기
    fun getToken() {
        repository.getToken()
    }

    // 개인 출석체크 업데이트
    fun updateUserCheckout(uid: String, myCallback: (Boolean) -> Unit) {
        repository.updateUserCheckout(uid) {
            myCallback(it)
        }
    }

    // 팬클럽 출석체크 업데이트
    fun updateFanClubCheckout(uid: String, myCallback: (Boolean) -> Unit) {
        repository.updateFanClubCheckout(uid) {
            myCallback(it)
        }
    }

    // 사용자 이메일(ID) 사용유무 확인 (true: 사용중, false: 미사용중)
    fun findUserFromEmail(email: String, myCallback: (UserDTO?) -> Unit) {
        repository.findUserFromEmail(email) {
            myCallback(it)
        }
    }

    // 사용자 닉네임 사용유무 확인 (true: 사용중, false: 미사용중)
    fun isUsedUserNickname(nickname: String, myCallback: (Boolean) -> Unit) {
        repository.isUsedUserNickname(nickname) {
            myCallback(it)
        }
    }

    // 유튜브 동영상 리스트 불러오기
    fun getMovieList() {
        repository.getMovieList()
    }

    // 응원글 리스트 불러오기
    fun getCheeringBoard(dbHandler : DBHelperReport, seasonNum: Int, seasonWeek: Int, type: FirebaseRepository.CheeringBoardType, favoriteProfileList: ArrayList<String>?) {
        repository.getCheeringBoard(dbHandler, seasonNum, seasonWeek, type, favoriteProfileList)
    }

    // 응원글 리스트 불러오기(인기순)
    fun getCheeringBoardPopular(dbHandler : DBHelperReport, seasonNum: Int, seasonWeek: Int, favoriteProfileList: ArrayList<String>?) {
        repository.getCheeringBoardPopular(dbHandler, seasonNum, seasonWeek, favoriteProfileList)
    }

    // 응원글 리스트 불러오기(최신순)
    fun getCheeringBoardNew(dbHandler : DBHelperReport, seasonNum: Int, seasonWeek: Int, favoriteProfileList: ArrayList<String>?) {
        repository.getCheeringBoardNew(dbHandler, seasonNum, seasonWeek, favoriteProfileList)
    }

    // 응원글 통계 불러오기
    fun getCheeringStatistics() {
        repository.getCheeringStatistics()
    }

    // 서브 공지 불러오기
    fun getNoticeSub(myCallback: (NoticeDTO?) -> Unit) {
       repository.getNoticeSub() {
           myCallback(it)
       }
    }

    // 팬클럽 리스트 불러오기
    fun getFanClub(order: FirebaseRepository.FanClubOrder) {
        repository.getFanClub(order)
    }

    // 나의 투표 수 불러오기
    fun getVoteCount(uid: String, seasonNum: Int, peopleDocName: String, myCallback: (Long) -> Unit) {
        repository.getVoteCount(uid, seasonNum, peopleDocName) {
            myCallback(it)
        }
    }

    // 나의 전체 투표 수 불러오기
    fun getVoteCountTotal(uid: String, peopleDocName: String, myCallback: (Long) -> Unit) {
        repository.getVoteCountTotal(uid, peopleDocName) {
            myCallback(it)
        }
    }

    //</editor-fold>

    //<editor-fold desc="@ 트랜잭션 함수">

    // 투표 하기
    fun addVoteTicket(docName: String, voteCount: Int, myCallback: (RankDTO?) -> Unit) {
        repository.addVoteTicket(docName, voteCount) {
            myCallback(it)
        }
    }

    // 사용자 티켓 추가
    fun addUserTicket(uid: String, ticketCount: Int, myCallback: (UserDTO?) -> Unit) {
        repository.addUserTicket(uid, ticketCount) {
            myCallback(it)
        }
    }

    // 사용자 티켓 사용
    fun useUserTicket(uid: String, ticketCount: Int, seasonNum: Int, peopleDocName: String, myCallback: (UserDTO?) -> Unit) {
        repository.useUserTicket(uid, ticketCount, seasonNum, peopleDocName) {
            myCallback(it)
        }
    }

    // 사용자 티켓 사용 기록
    fun updateUserVoteStatistics(uid: String, ticketCount: Int, seasonNum: Int, peopleDocName: String, myCallback: (Boolean) -> Unit) {
        repository.updateUserVoteStatistics(uid, ticketCount, seasonNum, peopleDocName) {
            myCallback(it)
        }
    }

    // 사용자 다이아 추가 (gemType : PAID_GEM 유료 다이아 추가, FREE_GEM 무료 다이아 추가)
    fun addUserGem(uid: String, paidGemCount: Int, freeGemCount: Int, firstPack: String? = null, myCallback: (UserDTO?) -> Unit) {
        repository.addUserGem(uid, paidGemCount, freeGemCount, firstPack) {
            myCallback(it)
        }
    }

    // 사용자 다이아 소비
    fun useUserGem(uid: String, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        repository.useUserGem(uid, gemCount) {
            myCallback(it)
        }
    }

    // 사용자 전광판 오늘 등록횟수 추가
    fun updateUserDisplayBoardWriteCount(uid: String, myCallback: (Long?) -> Unit) {
        repository.updateUserDisplayBoardWriteCount(uid) {
            myCallback(it)
        }
    }

    // 프리미엄 패키지 구매
    fun applyPremiumPackage(uid: String, myCallback: (UserDTO?) -> Unit) {
        repository.applyPremiumPackage(uid) {
            myCallback(it)
        }
    }

    // 응원글 좋아요 +-
    fun setCheeringBoardLike(docName: String, seasonNum: Int, seasonWeek: Int, likeCount: Int, myCallback: (BoardDTO?) -> Unit) {
        repository.setCheeringBoardLike(docName, seasonNum, seasonWeek, likeCount) {
            myCallback(it)
        }
    }

    fun updateFanClubMemberCount(fanClubId: String, count: Long, myCallback: (Long?) -> Unit) {
        repository.updateFanClubMemberCount(fanClubId, count) {
            myCallback(it)
        }
    }

    // 팬클럽 출석체크 인원 업데이트
    fun updateFanClubCheckoutCount(fanClubId: String, myCallback: (Long?) -> Unit) {
        repository.updateFanClubCheckoutCount(fanClubId) {
            myCallback(it)
        }
    }

    //</editor-fold>

    //<editor-fold desc="@ 업데이트 및 Set 함수">

    // 1:1 문의하기
    fun sendQna(qna: QnaDTO, myCallback: (Boolean) -> Unit) {
        repository.sendQna(qna) {
            myCallback(it)
        }
    }

    // 사용자 정보 전체 업데이트
    fun updateUser(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUser(user) {
            myCallback(it)
        }
    }

    // 사용자 즐겨찾기 리스트 업데이트
    fun updateUserFavorites(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserFavorites(user) {
            myCallback(it)
        }
    }

    // 사용자 이벤트 티켓 수령 ID 리스트 업데이트
    fun updateUserEventTicketIds(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserEventTicketIds(user) {
            myCallback(it)
        }
    }

    // 사용자 메일 발송
    fun sendUserMail(uid: String, mail: MailDTO, myCallback: (Boolean) -> Unit) {
        repository.sendUserMail(uid, mail) {
            myCallback(it)
        }
    }

    // 전광판 등록
    fun sendDisplayBoard(displayText: String, color: Int, user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.sendDisplayBoard(displayText, color, user) {
            myCallback(it)
        }
    }

    // 팬클럽 채팅 전송
    fun sendFanClubChat(fanClubId: String, chat: DisplayBoardDTO, myCallback: (Boolean) -> Unit) {
        repository.sendFanClubChat(fanClubId, chat) {
            myCallback(it)
        }
    }

    // 사용자 토큰 업데이트
    fun updateUserToken(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserToken(user) {
            myCallback(it)
        }
    }

    // 로그인 시간 기록
    fun updateUserLoginTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserLoginTime(user) {
            myCallback(it)
        }
    }

    // 사용자 프리미엄 패키지 다이아 수령 시간 기록
    fun updateUserPremiumGemGetTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserPremiumGemGetTime(user) {
            myCallback(it)
        }
    }

    // 사용자 메인타이틀 업데이트
    fun updateUserMainTitle(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserMainTitle(user) {
            myCallback(it)
        }
    }

    // 사용자 내 소개 업데이트
    fun updateUserAboutMe(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserAboutMe(user) {
            myCallback(it)
        }
    }

    // 사용자 프로필 업데이트
    fun updateUserProfile(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserProfile(user) {
            myCallback(it)
        }
    }

    // 사용자 닉네임 업데이트
    fun updateUserNickname(user: UserDTO, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        repository.updateUserNickname(user, gemCount) {
            myCallback(it)
        }
    }

    // 사용자 일일 과제 달성 업데이트
    fun updateUserQuestSuccessTimes(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserQuestSuccessTimes(user) {
            myCallback(it)
        }
    }

    // 사용자 일일 과제 보상 업데이트
    fun updateUserQuestGemGetTimes(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserQuestGemGetTimes(user) {
            myCallback(it)
        }
    }

    // 사용자 메일 읽음 상태로 업데이트
    fun updateUserMailRead(uid: String, mailUid: String, myCallback: (Boolean) -> Unit) {
        repository.updateUserMailRead(uid, mailUid) {
            myCallback(it)
        }
    }

    // 사용자 메일 삭제 상태로 업데이트
    fun updateUserMailDelete(uid: String, mailUid: String, myCallback: (Boolean) -> Unit) {
        repository.updateUserMailDelete(uid, mailUid) {
            myCallback(it)
        }
    }

    // 사용자 회원 탈퇴
    fun updateUserDeleteTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserDeleteTime(user) {
            myCallback(it)
        }
    }

    // 사용자 가입된 팬클럽 정보 업데이트
    fun updateUserFanClubId(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserFanClubId(user) {
            myCallback(it)
        }
    }

    // 응원하기 통계 데이터 취합
    fun updateCheeringStatistics(seasonNum: Int, seasonWeek: Int, myCallback: (Boolean) -> Unit) {
        repository.updateCheeringStatistics(seasonNum, seasonWeek) {
            myCallback(it)
        }
    }

    // 응원글 추가
    fun addCheeringBoard(boardDTO: BoardDTO, seasonNum: Int, seasonWeek: Int, myCallback: (Boolean) -> Unit) {
        repository.addCheeringBoard(boardDTO, seasonNum, seasonWeek) {
            myCallback(it)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 삭제 함수">

    // 응원글 삭제
    fun deleteCheeringBoard(docName: String, seasonNum: Int, seasonWeek: Int, myCallback: (Boolean) -> Unit) {
        repository.deleteCheeringBoard(docName, seasonNum, seasonWeek) {
            myCallback(it)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 로그 기록 함수">

    // 신고
    fun sendReport(report: ReportDTO, myCallback: (Boolean) -> Unit) {
        repository.sendReport(report) {
            myCallback(it)
        }
    }

    // 사용자 로그 작성
    fun writeUserLog(uid: String, log: LogDTO, myCallback: (Boolean) -> Unit) {
        repository.writeUserLog(uid, log) {
            myCallback(it)
        }
    }

    // 팬클럽 로그 작성
    fun writeFanClubLog(fanClubId: String, log: LogDTO, myCallback: (Boolean) -> Unit) {
        repository.writeFanClubLog(fanClubId, log) {
            myCallback(it)
        }
    }

    // 관리자 로그 작성
    fun writeAdminLog(log: LogDTO, myCallback: (Boolean) -> Unit) {
        repository.writeAdminLog(log) {
            myCallback(it)
        }
    }

    //</editor-fold>
}