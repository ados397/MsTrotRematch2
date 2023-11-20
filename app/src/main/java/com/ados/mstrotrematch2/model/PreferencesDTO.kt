package com.ados.mstrotrematch2.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class PreferencesDTO (
    var IntervalTime: Long? = 0,
    var maxTicketCount: Int? = 0,
    var ticketChargeCount: Int? = 0,
    var cheeringTicketCount: Int? = 0,
    var runHotTime: Boolean? = false,
    var rewardName: String? = null,
    var rewardNamePremium: String? = null,
    var rewardName2: String? = null,
    var rewardBonus: Int? = 0,
    var rewardBonus2: Int? = 0,
    var rewardCount: Int? = 0,
    var rewardIntervalTime: Long? = 0,
    var rewardIntervalTimeSec: Long? = 0,
    var questCount: Int? = 0,
    var lottoMaxCount: Int? = 0,
    var ticketSaveMaxCount: Int? = 0,
    var writeCount: Int? = 1,
    val hotTimeTitle: String? = null, // 핫타임(이벤트) 명칭
    val priceDisplayBoard: Int? = 0, // 전광판 1회 표시 비용
    val priceGamble10: Int? = 0, // 10 뽑기 1회 비용
    val priceGamble30: Int? = 0, // 30 뽑기 1회 비용
    val priceGamble100: Int? = 0, // 100 뽑기 1회 비용
    val displayBoardPeriod: Int? = 0, // 메인 전광판 표시 시간 (초)
    val displayBoardCount: Int? = 0, // 메인 전광판 표시할 항목 수
    val displayBoardWriteCount: Int? = 1, // 메인 전광판 하루에 등록 가능 수
    val rewardUserCheckoutGem: Int? = 0, // 개인 출석체크 다이아 보상
    val rewardPremiumPackBuyGem: Int? = 0, // 프리미엄 패키지 구매 다이아 보상
    val rewardPremiumPackCheckoutGem: Int? = 0, // 프리미엄 패키지 매일 다이아 보상
    val fanClubMembershipConditions: Int? = 0, // 팬클럽 가입 조건 (투표수)
    val fanClubChatDisplayPeriod: Int? = 0, // 팬클럽 메인 채팅 표시 시간 (초)
    val fanClubChatSendDelay: Int? = 0, // 팬클럽 채팅 전송 간격 (초)
) : Parcelable {
    // 티켓 충전시간 밀리 세컨드 단위 반환
    fun getIntervalTimeMillis() : Long {
        return IntervalTime?.times(1000)?.times(60)!!
    }

    fun getRewardIntervalTimeMillis() : Long {
        return rewardIntervalTimeSec?.times(1000)!!
    }
}

data class NewsDTO (var title : String? = null,
                    var time : String? = null,
                    var content : String? = null,
                    var imageUrl: String? = null)

data class EventDTO (var title : String? = null,
                     var uid : String? = null,
                     var count : Int? = 0,
                     var limit : Date? = null)

@Parcelize
data class SeasonDTO (var seasonNum : Int? = 1,
                      var startTime : Date? = null,
                      var endTime : Date? = null,
                      var endDate : String? = null
) : Parcelable {
    // 시즌 몇 주차 인지 반환
    // 시즌 시작일을 기준으로 7일 단위로 주차가 늘어남
    fun getWeek() : Int {
        val calendar = Calendar.getInstance()
        val day = (calendar.time.time - startTime?.time!!) / (60 * 60 * 24 * 1000)

        return day.div(7).plus(1).toInt()
    }

    // 남은 D-Day 표시
    fun getDDay(): Int {
        val interval = ((endTime?.time!!.toLong()) - System.currentTimeMillis()) / 1000
        return (interval / 86400).toInt()
    }
}

data class UpdateDTO (
    var updateUrl : String = "https://play.google.com/store/apps/details?id=com.ados.mstrotrematch2", // 업데이트 Url
    var minVersion : String? = null, // 실행 가능한 최소 버전, 해당 버전 미만은 앱 실행 불가
    var minVersionDisplay : Boolean? = false, // 최소 버전 경고 표시 여부
    var minVersionTitle : String? = null, // 업데이트 경고 제목
    var minVersionDesc : String? = null, // 업데이트 경고 내용
    var updateVersion : String? = null, // 업데이트 필요 버전, 해당 버전 미만은 앱 업데이트 필요
    var updateVersionDisplay : Boolean? = false, // 업데이트 필요 버전 경고 표시 여부
    var updateVersionTitle : String? = null, // 업데이트 경고 제목
    var updateVersionDesc : String? = null, // 업데이트 경고 내용
    var maintenance : Boolean? = false, // 서버 점검 여부
    var maintenanceTitle : String? = null, // 서버 점검 시 표시될 제목
    var maintenanceDesc : String? = null, // 서버 점검 시 표시될 내용
    var maintenanceImgUrl : String? = null // 서버 점검 시 표시할 이미지
)

data class NoticeDTO (
    val title : String? = null,
    val content : String? = null,
    val imageUrl: String? = null,
    val packageName: String? = null,
    val visibility: Boolean? = false
) {}

@Parcelize
data class AdPolicyDTO(
    var ad_banner: String? = null,
    var ad_interstitial: String? = null,
    var ad_reward1: String? = null,
    var ad_reward2: String? = null,
    var ad_reward3: String? = null
) : Parcelable { }

data class RecoveryCodeDTO(
    var code: String? = null,
    var name: String? = null,
    var email: String? = null,
    var count: Int? = 0,
    var useTime: Date? = null
) {}