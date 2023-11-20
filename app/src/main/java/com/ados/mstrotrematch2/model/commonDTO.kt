package com.ados.mstrotrematch2.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

data class EditTextDTO(val title: String? = null,
                       val content: String? = null,
                       val length: Int? = 0,
                       val regex: String? = null,
                       val regexErrorMsg: String? = null
) { }

data class CalendarDTO(
    var StartDate: Boolean = false,
    var EndDate: String? = null
) {}

data class LogDTO(
    var log: String? = null,
    var insertTime: Date? = null
) {}



data class SignUpDTO(
    var isSelected: Boolean = false,
    var isChecked: Boolean = false,
    val name: String? = null
) {}

data class WeekDTO(
    var week: Int? = 0,
    var year: Int? = 0,
    var month: Int? = 0,
    var startDate: Date? = null,
    val endDate: Date? = null
) {}

data class ReportDTO(
    var fromUserUid: String? = null, // 신고자
    var fromUserNickname: String? = null,
    var toUserUid: String? = null, // 신고대상
    var toUserNickname: String? = null,
    var content: String? = null,
    var contentDocName: String? = null,
    var type: Type = Type.DisplayBoard,
    var reason: String? = null,
    var reportTime: Date? = null
) {
    enum class Type {
        DisplayBoard, CheeringBoard, User, FanClub, Schedule, FanClubChat
    }

    fun getCollectionName() : String {
        return when(type) {
            Type.CheeringBoard -> "cheeringBoard"
            Type.DisplayBoard -> "displayBoard"
            Type.User -> "user"
            Type.FanClub -> "fanClub"
            Type.Schedule -> "schedule"
            Type.FanClubChat -> "fanClubChat"
        }
    }
}

data class FaqDTO(
    var question: String? = null, // 질문
    var answer: String? = null, // 답변
    var imageUrl: String? = null,
    var order: Int? = 0
) {}

data class QnaDTO(
    var userUid: String? = null,
    var userNickname: String? = null,
    var title: String? = null,
    var content: String? = null,
    var imageUrl: String? = null,
    var answer: String? = null, // 답변
    var createTime: Date? = null,
    var answerTime: Date? = null
) {}

data class NotificationBody(
    val to: String,
    val data: NotificationData
) {
    data class NotificationData(
        val title: String,
        val userId : String,
        val message: String
    )
}