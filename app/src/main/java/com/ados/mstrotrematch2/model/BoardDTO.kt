package com.ados.mstrotrematch2.model

import android.net.Uri
import java.util.*

data class BoardDTO(var docname: String? = null,
                    var peopleDocName: String? = null,
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
                    var report: String? = null,
                    var userUid: String? = null,
                    var isPhoto: Boolean = false) {
}

data class DisplayBoardDTO(
    var docName: String? = null,
    var displayText: String? = null,
    var userUid: String? = null,
    var userNickname: String? = null,
    var color: Int? = 0,
    var order: Long? = 0L,
    var createTime: Date? = null
) { }

data class DisplayBoardExDTO(
    val displayBoardDTO: DisplayBoardDTO? = null,
    var isBlocked: Boolean = false,
    var isSelected: Boolean = false,
    var imgProfileUri: Uri? = null
) { }
