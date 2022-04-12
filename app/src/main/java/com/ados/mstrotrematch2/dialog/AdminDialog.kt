package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.EventDTO
import com.ados.mstrotrematch2.model.NewsDTO
import com.ados.mstrotrematch2.model.PreferencesDTO
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.admin_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdminDialog(context: Context) : Dialog(context), View.OnClickListener {

    var firestore : FirebaseFirestore? = null
    private val layout = R.layout.admin_dialog
    var preferencesDTO : PreferencesDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            preferencesDTO = documentSnapshot?.toObject(PreferencesDTO::class.java)
        }

        init()

        /*button_ticket_morning.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0901").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 오전 티켓이 『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 정오 티켓이 『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1801").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 저녁 티켓이 『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd2201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 깜짝 티켓은 특별히 『9』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 9, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/



        // 노말 티켓 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        button_ticket_morning.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0901").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("오전 티켓이 2장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 2, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("정오 티켓이 2장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 2, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1801").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("저녁 티켓이 2장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 2, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd2201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("깜짝 티켓이 2장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 2, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }



        // 핫타임 티켓 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        button_ticket_morning_hottime.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0901").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 오전 티켓이 '5'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 5, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon_hottime.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 정오 티켓이 '5'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 5, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening_hottime.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1801").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 저녁 티켓이 '5'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 5, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night_hottime.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd2201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83C\uDFC6핫타임\uD83D\uDD25 깜짝 티켓이 '5'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 5, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        /*button_ticket_morning_hottime.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0901").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDFC6시즌6\uD83C\uDFC6 개막식 \uD83D\uDD25핫타임\uD83D\uDD25 오전 티켓이 \uD83C\uDF8110장\uD83C\uDF81 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon_hottime.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDFC6시즌6\uD83C\uDFC6 개막식 \uD83D\uDD25핫타임\uD83D\uDD25 정오 티켓이 \uD83C\uDF8110장\uD83C\uDF81 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening_hottime.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1801").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDFC6시즌6\uD83C\uDFC6 개막식 \uD83D\uDD25핫타임\uD83D\uDD25 저녁 티켓이 \uD83C\uDF8110장\uD83C\uDF81 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night_hottime.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd2201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83C\uDFC6시즌6\uD83C\uDFC6 개막식 \uD83D\uDD25핫타임\uD83D\uDD25 깜짝 티켓이 \uD83C\uDF8110장\uD83C\uDF81 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_hottime_special.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0001").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDFC6시즌6\uD83C\uDFC6 개막식 \uD83D\uDD25핫타임\uD83D\uDD25 스페셜 티켓이 \uD83D\uDC9B20장\uD83D\uDC9B 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 20, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"스페셜 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/



        // 이벤트 티켓 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        button_ticket_morning_hottime2.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0901").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF33식목일\uD83C\uDF32 기념 모닝 티켓이 \uD83C\uDF3110장\uD83C\uDF40 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon_hottime2.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF33식목일\uD83C\uDF32 기념 정오 티켓이 \uD83C\uDF3110장\uD83C\uDF40 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening_hottime2.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1801").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF33식목일\uD83C\uDF32 기념 저녁 티켓이 \uD83C\uDF3110장\uD83C\uDF40 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night_hottime2.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd2201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83C\uDF33식목일\uD83C\uDF32 기념 깜짝 티켓이 \uD83C\uDF3110장\uD83C\uDF40 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon_weekend.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0001").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF33식목일\uD83C\uDF32 기념 스페셜 티켓이 \uD83C\uDF3114장\uD83C\uDF40 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 14, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"스페셜 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }



        // 이벤트 티켓 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        button_ticket_morning_hottime3.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0901").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDFC6시즌5\uD83C\uDFC6 마지막 \uD83D\uDD25핫타임\uD83D\uDD25 오전 티켓이 \uD83D\uDCA510장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon_hottime3.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDFC6시즌5\uD83C\uDFC6 마지막 \uD83D\uDD25핫타임\uD83D\uDD25 정오 티켓이 \uD83D\uDCA510장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening_hottime3.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd1801").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDFC6시즌5\uD83C\uDFC6 마지막 \uD83D\uDD25핫타임\uD83D\uDD25 저녁 티켓이 \uD83D\uDCA510장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night_hottime3.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd2201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83C\uDFC6시즌5\uD83C\uDFC6 마지막 \uD83D\uDD25핫타임\uD83D\uDD25 깜짝 티켓이 \uD83D\uDCA510장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 10, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_special3.setOnClickListener {
            val uid = "s${SimpleDateFormat("yyMMdd0001").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDFC6시즌5\uD83C\uDFC6 마지막 \uD83D\uDD25핫타임\uD83D\uDD25 스페셜 티켓이 \uD83D\uDC9B15장\uD83D\uDC9B 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 15, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"스페셜 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }



        /*button_ticket_afternoon_weekend.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1201").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF1F주말\uD83C\uDF1F 정오 티켓이 '5'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 5, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"주말 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/


        button_ticket_random.setOnClickListener {
            var ticketTime = number_picker_time.value * 5

            val uid = "sr${SimpleDateFormat("yyMMddHHmm").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.MINUTE, ticketTime)

            val eventDTO = EventDTO("\uD83C\uDF82축하드립니다! \uD83C\uDF40행운의\uD83C\uDF40 티켓을 발견했습니다\uD83C\uDF8A\\n금방 사라지니 빨리 수령하세요.\\n지금 수령 하시겠습니까?", uid, number_picker_ticket.value, cal.time)
            firestore?.collection("event")?.document(uid)?.set(eventDTO)

            Toast.makeText(context,"${ticketTime}분, ${number_picker_ticket.value}장 행운의 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_hottime_start.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 5,
                runHotTime = true,
                rewardCount = 100,
                rewardIntervalTime = 1,
                rewardIntervalTimeSec = 60
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
                Toast.makeText(context,"핫타임 시작.", Toast.LENGTH_SHORT).show()
            }
        }

        button_hottime_start2.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 1,
                runHotTime = true,
                rewardCount = 240,
                rewardIntervalTime = 1,
                rewardIntervalTimeSec = 24
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
                Toast.makeText(context,"이벤트 시작.", Toast.LENGTH_SHORT).show()
            }
        }

        button_hottime_stop.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 15,
                runHotTime = false,
                rewardCount = 50,
                rewardIntervalTime = 2,
                rewardIntervalTimeSec = 120
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
                Toast.makeText(context,"핫타임 종료.", Toast.LENGTH_SHORT).show()
            }
        }

        number_picker_ticket.minValue = 1
        number_picker_ticket.maxValue = 20
        number_picker_ticket.value = 7
        number_picker_ticket.wrapSelectorWheel = false

        var yearList = (5..200 step 5).toList()
        var yearStrConvertList = yearList.map { it.toString() }
        number_picker_time.minValue = 1
        number_picker_time.maxValue = yearStrConvertList.size
        number_picker_time.value = 12
        number_picker_time.displayedValues = yearStrConvertList.toTypedArray()
        number_picker_time.wrapSelectorWheel = false
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }
}