package com.ados.mstrotrematch2.util

import java.text.SimpleDateFormat
import java.util.*

class Utility {

    companion object{
        fun timeConverter(time: Long) :String {
            val currentTime = System.currentTimeMillis()
            var diffTime = (currentTime - time) / 60000

            return when {
                diffTime < 1 -> {
                    "방금 전"
                }
                diffTime < 60 -> {
                    "${diffTime}분 전"
                }
                diffTime/60 < 24 -> {
                    "${diffTime/60}시간 전"
                }
                else -> {
                    val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd")
                    simpleDataFormat.format(time)
                }
            }
        }

        fun chatTimeConverter(time: Long) :String {
            val simpleDataFormat = SimpleDateFormat("MM-dd HH:mm")
            return simpleDataFormat.format(time)
        }

        fun randomDocumentName() : String {
            val alphabets = ('a'..'z').toMutableList()
            return  "${alphabets[Random().nextInt(alphabets.size)]}${System.currentTimeMillis()}"
        }

        fun randomNickName() : String {
            val alphabets = ('a'..'z').toMutableList()
            return  "닉네임${alphabets[Random().nextInt(alphabets.size)]}${String.format("%06d",Random().nextInt(999999))}${alphabets[Random().nextInt(alphabets.size)]}"
        }

        fun getNumKorString(value: Long): String {
            val billion = value / 100000000 // 억 단위 계산
            val million = (value % 100000000) / 10000 // 억을 뺀 나머지 100만 단위
            var numString = if (billion > 0) "${billion}억" else ""
            if (million > 0) {
                numString += "${million}만"
            }

            if (numString.isNullOrEmpty()) {
                numString = "0"
            }

            return numString
        }
    }
}