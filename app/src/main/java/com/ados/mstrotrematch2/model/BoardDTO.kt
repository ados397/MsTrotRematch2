package com.ados.mstrotrematch2.model

import java.util.*

data class BoardDTO(var docname: String? = null,
                    var image: String? = null,
                    var imageUrl: String? = null,
                    var title: String? = null,
                    var content: String? = null,
                    var name: String? = null,
                    var time: Date? = null,
                    var password: String? = null,
                    var likeCount: Int? = 0,
                    var dislikeCount: Int? = 0,
                    var isBlock: Boolean = false,
                    var report: String? = null) {
}