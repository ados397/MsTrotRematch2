package com.ados.mstrotrematch2.model

import java.util.*

data class RankDTO(
    var image: String? = null,
    var name: String? = null,
    var count: Int? = 0,
    var docname: String? = null,
    var cheeringCount: Int? = 0,
    var likeCount: Int? = 0,
    var dislikeCount: Int? = 0,
    var cheeringCountTotal: Int? = 0,
    var updateDate: Date? = null,
    var subTitle: String? = null,
    var donationUrl: String? = null
) {
}
data class DonationNewsDTO(
    var company: String? = null,
    var url: String? = null,
    var order: Int? = 0
) {
}