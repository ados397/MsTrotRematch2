package com.ados.mstrotrematch2.firebase

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.ados.mstrotrematch2.firebase.FirebaseStorageRepository

class FirebaseStorageViewModel(application: Application) : AndroidViewModel(application) {

    //<editor-fold desc="@ 변수 선언">

    private val repository : FirebaseStorageRepository = FirebaseStorageRepository()

    //</editor-fold>


    //<editor-fold desc="@ 데이터 획득 함수">

    // 사용자 프로필 이미지 불러오기
    fun getUserProfileImage(uid: String, myCallback: (Uri?) -> Unit) {
        repository.getUserProfileImage(uid) {
            myCallback(it)
        }
    }

    // 사용자 프로필 이미지 저장
    fun setUserProfileImage(uid: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        repository.setUserProfileImage(uid, bitmap) {
            myCallback(it)
        }
    }

    // 사용자 프로필 이미지 삭제
    fun deleteUserProfileImage(uid: String, myCallback: (Boolean) -> Unit) {
        repository.deleteUserProfileImage(uid) {
            myCallback(it)
        }
    }

    // 팬클럽 심볼 이미지 불러오기
    fun getFanClubSymbolImage(fanClubId: String, myCallback: (Uri?) -> Unit) {
        repository.getFanClubSymbolImage(fanClubId) {
            myCallback(it)
        }
    }

    // 팬클럽 심볼 이미지 저장
    fun setFanClubSymbolImage(fanClubId: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        repository.setFanClubSymbolImage(fanClubId, bitmap) {
            myCallback(it)
        }
    }

    // 팬클럽 심볼 이미지 삭제
    fun deleteFanClubSymbolImage(fanClubId: String, myCallback: (Boolean) -> Unit) {
        repository.deleteFanClubSymbolImage(fanClubId) {
            myCallback(it)
        }
    }

    // 스케줄 이미지 불러오기
    fun getScheduleImage(uid: String, scheduleId: String, type: FirebaseStorageRepository.ScheduleType, myCallback: (Uri?) -> Unit) {
        repository.getScheduleImage(uid, scheduleId, type) {
            myCallback(it)
        }
    }

    // 스케줄 이미지 저장
    fun setScheduleImage(uid: String, scheduleId: String, type: FirebaseStorageRepository.ScheduleType, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        repository.setScheduleImage(uid, scheduleId, type, bitmap) {
            myCallback(it)
        }
    }

    // 스케줄 이미지 삭제
    fun deleteScheduleImage(uid: String, scheduleId: String, type: FirebaseStorageRepository.ScheduleType, myCallback: (Boolean) -> Unit) {
        repository.deleteScheduleImage(uid, scheduleId, type) {
            myCallback(it)
        }
    }

    // 응원하기 게시글 이미지 불러오기
    fun getCheeringBoardImage(seasonNum: Int, docName: String, myCallback: (Uri?) -> Unit) {
        repository.getCheeringBoardImage(seasonNum, docName) {
            myCallback(it)
        }
    }

    // 응원하기 게시글 이미지 저장
    fun setCheeringBoardImage(seasonNum: Int, docName: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        repository.setCheeringBoardImage(seasonNum, docName, bitmap) {
            myCallback(it)
        }
    }

    // 응원하기 게시글 이미지 삭제
    fun deleteCheeringBoardImage(seasonNum: Int, docName: String, myCallback: (Boolean) -> Unit) {
        repository.deleteCheeringBoardImage(seasonNum, docName) {
            myCallback(it)
        }
    }

    //</editor-fold>
}