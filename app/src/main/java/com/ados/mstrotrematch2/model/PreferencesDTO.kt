package com.ados.mstrotrematch2.model

import java.util.*

data class PreferencesDTO (
    var IntervalTime: Long? = 0,
    var maxTicketCount: Int? = 0,
    var ticketChargeCount: Int? = 0,
    var runHotTime: Boolean? = false,
    var rewardName: String? = null,
    var rewardName2: String? = null,
    var rewardBonus: Int? = 0,
    var rewardBonus2: Int? = 0,
    var rewardCount: Int? = 0,
    var rewardIntervalTime: Long? = 0,
    var rewardIntervalTimeSec: Long? = 0,
    var questCount: Int? = 0,
    var lottoMaxCount: Int? = 0,
    var ticketSaveMaxCount: Int? = 0
) {
}

data class NewsDTO (var title : String? = null,
                    var time : String? = null,
                    var content : String? = null)

data class EventDTO (var title : String? = null,
                     var uid : String? = null,
                     var count : Int? = 0,
                     var limit : Date? = null)

data class SeasonDTO (var seasonNum : Int? = 1,
                      var endDate : String? = null)

data class UpdateDTO (var version : String? = null,
                      var visibility : Boolean? = false,
                      var essential : String? = null,
                      var updateUrl : String? = null,
                      var maintainance : Boolean? = false,
                      var maintainanceTitle : String? = null,
                      var maintainanceDesc : String? = null)
