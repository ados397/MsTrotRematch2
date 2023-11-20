package com.ados.mstrotrematch2.util

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class MySharedPreferences(context: Context) {
    companion object {
        const val PREF_KEY_REWARD_COUNT = "RewardCount"
        const val PREF_KEY_REWARD_MAX_COUNT = "RewardMaxCount"
        const val PREF_KEY_REWARD_TIME = "RewardTime"
        const val PREF_KEY_TICKET_COUNT = "TicketCount"
        const val PREF_KEY_TICKET_CHARGE_TIME = "ChargeTime"
        const val PREF_KEY_GEM_COUNT = "LottoCount"
        const val PREF_KEY_QUEST_COUNT = "QuestCount"
        const val PREF_KEY_WRITE_CHEERING = "WriteCheering"
        const val PREF_KEY_CHEER_BOARD = "CheerBoard"
        const val PREF_KEY_NOTICE_SUB_READ_TIME = "NoticeSubReadTime"
        const val PREF_KEY_CONGRATULATE_READ_TIME = "CongratulateReadTime"
        const val PREF_KEY_NON_MEMBER_UID = "NonMemberUid"
        const val PREF_KEY_CHEERING_SHOW_FAVORITE = "CheeringShowFavorite"
        const val PREF_KEY_PERMISSION_DENIED_TIME = "PermissionDeniedTime"
        const val PREF_KEY_FAN_CLUB_CHAT_READ_TIME = "FanCLubChatReadTime"
        const val PREF_KEY_FAN_CLUB_CHAT_SEND_TIME = "FanClubChatSendTime"
        const val PREF_KEY_DISPLAY_BOARD_VISIBILITY = "DisplayBoardVisibility"
        const val PREF_KEY_NON_MEMBER_SAVE_UID = "NonMemberSaveUid"
    }

    //private var pref: SharedPreferences = context.getSharedPreferences("storage", Context.MODE_PRIVATE)
    private var pref: SharedPreferences = context.getSharedPreferences("com.ados.mstrotrematch2_preferences", Context.MODE_PRIVATE)

    // 날짜가 추가된 키의 데이터 삽입
    fun putIntDate(key: String?, value: Int) {
        val keyString = "${key}${SimpleDateFormat("yyyyMMdd").format(Date())}"
        putInt(keyString, value)
    }

    fun putInt(key: String?, value: Int) {
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun putLong(key: String?, value: Long) {
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun putString(key: String?, value: String) {
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun putStringSubKey(key: String?, subKey: String?, value: String) {
        val keyString = "${key}_${subKey}"
        putString(keyString, value)
    }

    fun putBoolean(key: String?, value: Boolean) {
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun putBooleanSubKey(key: String?, subKey: String?, value: Boolean) {
        val keyString = "${key}_${subKey}"
        putBoolean(keyString, value)
    }

    // 날짜가 추가된 키의 데이터 획득
    fun getIntDate(key: String?, default: Int): Int {
        val keyString = "${key}${SimpleDateFormat("yyyyMMdd").format(Date())}"
        return getInt(keyString, default)
    }

    fun getInt(key: String?, default: Int): Int {
        return pref.getInt(key, default)
    }

    fun getLong(key: String?, default: Long): Long {
        return pref.getLong(key, default)
    }

    fun getString(key: String?, default: String): String? {
        return pref.getString(key, default)
    }

    fun getStringSubKey(key: String?, subKey: String?, default: String): String? {
        val keyString = "${key}_${subKey}"
        return getString(keyString, default)
    }

    fun getBoolean(key: String?, default: Boolean): Boolean {
        return pref.getBoolean(key, default)
    }

    fun getBooleanSubKey(key: String?, subKey: String?, default: Boolean): Boolean {
        val keyString = "${key}_${subKey}"
        return getBoolean(keyString, default)
    }
}