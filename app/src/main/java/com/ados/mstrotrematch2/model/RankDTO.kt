package com.ados.mstrotrematch2.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class RankDTO(
    var image: String? = null,
    var name: String? = null,
    var count: Long? = 0L,
    var docname: String? = null,
    var cheeringCount: Int? = 0,
    var likeCount: Int? = 0,
    var dislikeCount: Int? = 0,
    var cheeringCountTotal: Int? = 0,
    var updateDate: Date? = null,
    var subTitle: String? = null,
    var donationUrl: String? = null,
    var celebrateCount: Long? = null, // 100만, 200만 축하 공지를 전광판에 띄우고 기록
    var birthday: Date? = null, // 가수 생일
) : Parcelable {
}

data class RankExDTO(
    var rankDTO: RankDTO? = null,
    var favorite: Boolean? = false
) {
}

@Parcelize
data class DonationNewsDTO(
    var company: String? = null,
    var url: String? = null,
    var order: Int? = 0
) : Parcelable {
}