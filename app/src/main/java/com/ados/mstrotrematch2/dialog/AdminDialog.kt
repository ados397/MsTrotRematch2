package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.AdminDialogBinding
import com.ados.mstrotrematch2.model.EventDTO
import com.ados.mstrotrematch2.model.PreferencesDTO
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminDialog(context: Context) : Dialog(context), View.OnClickListener {

    lateinit var binding: AdminDialogBinding

    var firestore : FirebaseFirestore? = null
    var preferencesDTO : PreferencesDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            preferencesDTO = documentSnapshot?.toObject(PreferencesDTO::class.java)

            // 기본 설정 지정
            var preferences = preferencesDTO?.copy(
                rewardName = "광고 보고 다이아 1개 받으세요!",
                rewardNamePremium = "광고 보고 다이아 2개 받으세요!",
                writeCount = 10,
                hotTimeTitle = "\uD83D\uDD25핫타임\uD83D\uDD25 이벤트",
                priceDisplayBoard = 50, // 전광판 1회 표시 비용
                priceGamble10 = 1, // 10 뽑기 1회 비용
                priceGamble30 = 3, // 30 뽑기 1회 비용
                priceGamble100 = 9, // 100 뽑기 1회 비용
                displayBoardPeriod = 20, // 메인 전광판 표시 시간 (초)
                displayBoardCount = 10, // 메인 전광판 표시할 항목 수
                rewardUserCheckoutGem = 1, // 개인 출석체크 다이아 보상
                rewardPremiumPackBuyGem = 200, // 프리미엄 패키지 구매 다이아 보상
                rewardPremiumPackCheckoutGem = 20, // 프리미엄 패키지 매일 다이아 보상
            )!!
            firestore?.collection("preferences")?.document("preferences")?.set(preferences)
        }

        init()

        /*button_ticket_morning.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 오전 티켓이『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 정오 티켓이『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 저녁 티켓이『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 깜짝 티켓은 특별히『9』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 9, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/

        // 노말 티켓 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        binding.buttonTicketMorning.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("오전 티켓이 3장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 3, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketAfternoon.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("정오 티켓이 3장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 3, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketEvening.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("저녁 티켓이 3장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 3, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketNight.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("깜짝 티켓이 3장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 3, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }



        // 핫타임 티켓 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        binding.buttonTicketMorningHottime.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 오전 티켓이 '6'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 6, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketAfternoonHottime.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 정오 티켓이 '6'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 6, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketEveningHottime.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 저녁 티켓이 '6'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 6, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketNightHottime.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 깜짝 티켓이 '6'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 6, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }



        // 이벤트 티켓 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        binding.buttonTicketMorningHottime2.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val count = 10
            val eventDTO = EventDTO("⭐특별⭐ 모닝 티켓이 ⭐${count}장⭐ 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketAfternoonHottime2.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val count = 11
            val eventDTO = EventDTO("⭐특별⭐ 정오 티켓이 ⭐${count}장⭐ 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketEveningHottime2.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 12
            val eventDTO = EventDTO("⭐특별⭐ 저녁 티켓이 ⭐${count}장⭐ 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketNightHottime2.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val count = 13
            val eventDTO = EventDTO("⭐특별⭐ 깜짝 티켓이 ⭐${count}장⭐ 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketSpecial2.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0000").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 14
            val eventDTO = EventDTO("⭐특별⭐ 핫타임 스페셜 티켓이 ⭐${count}장⭐ 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"스페셜 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }



        // 이벤트 티켓 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        binding.buttonTicketMorningHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val count = 13
            val eventDTO = EventDTO("\uD83C\uDFC6시즌9\uD83C\uDFC6 마지막\uD83D\uDCA5 주말 \uD83D\uDD25핫타임\uD83D\uDD25 모닝 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketAfternoonHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val count = 14
            val eventDTO = EventDTO("\uD83C\uDFC6시즌9\uD83C\uDFC6 마지막\uD83D\uDCA5 주말 \uD83D\uDD25핫타임\uD83D\uDD25 정오 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketEveningHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 17
            val eventDTO = EventDTO("\uD83C\uDFC6시즌9\uD83C\uDFC6 마지막\uD83D\uDCA5 주말 \uD83D\uDD25핫타임\uD83D\uDD25 저녁 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketNightHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val count = 18
            val eventDTO = EventDTO("\uD83C\uDFC6시즌9\uD83C\uDFC6 마지막\uD83D\uDCA5 주말 \uD83D\uDD25핫타임\uD83D\uDD25 깜짝 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketSpecial3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0000").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 18
            val eventDTO = EventDTO("\uD83C\uDFC6시즌9\uD83C\uDFC6 마지막\uD83D\uDCA5 주말 \uD83D\uDD25핫타임\uD83D\uDD25 스페셜\uD83C\uDF89 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"스페셜 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }

        /*binding.buttonTicketMorningHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val count = 20
            val eventDTO = EventDTO("\uD83C\uDFC6시즌12\uD83C\uDFC6 폐막식\uD83D\uDCA5 오전 티켓이 \uD83E\uDD0E${count}장\uD83E\uDD0E 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketAfternoonHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val count = 15
            val eventDTO = EventDTO("\uD83C\uDFC6시즌12\uD83C\uDFC6 폐막식\uD83D\uDCA5 정오 티켓이 \uD83E\uDD0E${count}장\uD83E\uDD0E 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketEveningHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 17
            val eventDTO = EventDTO("\uD83C\uDFC6시즌12\uD83C\uDFC6 폐막식\uD83D\uDCA5 저녁 티켓이 \uD83E\uDD0E${count}장\uD83E\uDD0E 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketNightHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val count = 20
            val eventDTO = EventDTO("\uD83C\uDFC6시즌12\uD83C\uDFC6 폐막식\uD83D\uDCA5 깜짝 티켓이 \uD83E\uDD0E${count}장\uD83E\uDD0E 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketSpecial3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0000").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 20
            val eventDTO = EventDTO("\uD83C\uDFC6시즌12\uD83C\uDFC6 폐막식\uD83D\uDCA5 기념 \uD83C\uDF89스페셜\uD83C\uDF89 티켓이 \uD83D\uDC9B${count}장\uD83D\uDC9B 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"스페셜 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/
        /*binding.buttonTicketMorningHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val count = 17
            val eventDTO = EventDTO("\uD83C\uDFC6시즌13\uD83C\uDFC6 개막식\uD83D\uDCA5 오전 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketAfternoonHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val count = 20
            val eventDTO = EventDTO("\uD83C\uDFC6시즌13\uD83C\uDFC6 개막식\uD83D\uDCA5 정오 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketEveningHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 19
            val eventDTO = EventDTO("\uD83C\uDFC6시즌13\uD83C\uDFC6 개막식\uD83D\uDCA5 저녁 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketNightHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val count = 15
            val eventDTO = EventDTO("\uD83C\uDFC6시즌13\uD83C\uDFC6 개막식\uD83D\uDCA5 깜짝 티켓이 \uD83D\uDCA5${count}장\uD83D\uDCA5 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketSpecial3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0000").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 20
            val eventDTO = EventDTO("\uD83C\uDFC6시즌13\uD83C\uDFC6 개막식\uD83D\uDCA5 기념 \uD83C\uDF89스페셜\uD83C\uDF89 티켓이 \uD83D\uDC9B${count}장\uD83D\uDC9B 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"스페셜 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/

        /*binding.buttonTicketMorningHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val count = 15
            val eventDTO = EventDTO("\uD83C\uDF91정월 대보름\uD83C\uDF15 모닝 티켓이 \uD83C\uDF87${count}장\uD83C\uDF87 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketAfternoonHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val count = 12
            val eventDTO = EventDTO("\uD83C\uDF91정월 대보름\uD83C\uDF15 정오 티켓이 \uD83C\uDF87${count}장\uD83C\uDF87 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketEveningHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 15
            val eventDTO = EventDTO("\uD83C\uDF91정월 대보름\uD83C\uDF15 저녁 티켓이 \uD83C\uDF87${count}장\uD83C\uDF87 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketNightHottime3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val count = 18
            val eventDTO = EventDTO("\uD83C\uDF91정월 대보름\uD83C\uDF15 깜짝 티켓이 \uD83C\uDF87${count}장\uD83C\uDF87 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"이벤트 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTicketSpecial3.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0000").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val count = 17
            val eventDTO = EventDTO("\uD83C\uDF91정월 대보름\uD83C\uDF15 기념 \uD83C\uDF89스페셜\uD83C\uDF89 티켓이 \uD83C\uDF87${count}장\uD83C\uDF87 도착했습니다.\\n지금 수령 하시겠습니까?", uid, count, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"스페셜 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/


        /*button_ticket_afternoon_weekend.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF1F주말\uD83C\uDF1F 정오 티켓이 '5'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 5, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"주말 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/






        binding.buttonTicketRandom.setOnClickListener {
            var ticketTime = binding.numberPickerTime.value * 5

            val uid = "tr${SimpleDateFormat("yyMMddHHmm").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.MINUTE, ticketTime)

            val eventDTO = EventDTO("\uD83C\uDF82축하드립니다! \uD83C\uDF40행운의\uD83C\uDF40 티켓을 발견했습니다\uD83C\uDF8A\\n금방 사라지니 빨리 수령하세요.\\n지금 수령 하시겠습니까?", uid, binding.numberPickerTicket.value, cal.time)
            firestore?.collection("event")?.document(uid)?.set(eventDTO)

            Toast.makeText(context,"${ticketTime}분, ${binding.numberPickerTicket.value}장 행운의 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonHottimeStart.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 5,
                runHotTime = true,
                rewardCount = 100,
                rewardIntervalTime = 1,
                rewardIntervalTimeSec = 45
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
                Toast.makeText(context,"핫타임 시작.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonHottimeStart2.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 1,
                runHotTime = true,
                rewardCount = 200,
                rewardIntervalTime = 1,
                rewardIntervalTimeSec = 30
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
                Toast.makeText(context,"이벤트1 시작.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonHottimeStart3.setOnClickListener {
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
                Toast.makeText(context,"이벤트2 시작.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonHottimeStop.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 15,
                runHotTime = false,
                rewardCount = 50,
                rewardIntervalTime = 2,
                rewardIntervalTimeSec = 90
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
                Toast.makeText(context,"핫타임 종료.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.numberPickerTicket.minValue = 1
        binding.numberPickerTicket.maxValue = 20
        binding.numberPickerTicket.value = 7
        binding.numberPickerTicket.wrapSelectorWheel = false

        var yearList = (5..200 step 5).toList()
        var yearStrConvertList = yearList.map { it.toString() }
        binding.numberPickerTime.minValue = 1
        binding.numberPickerTime.maxValue = yearStrConvertList.size
        binding.numberPickerTime.value = 12
        binding.numberPickerTime.displayedValues = yearStrConvertList.toTypedArray()
        binding.numberPickerTime.wrapSelectorWheel = false
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