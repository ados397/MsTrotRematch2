package com.ados.mstrotrematch2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ados.mstrotrematch2.adapter.OnDisplayBoardItemClickListener
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterDisplayBoard
import com.ados.mstrotrematch2.databinding.ActivityDisplayBoardBinding
import com.ados.mstrotrematch2.dialog.DisplayBoardAddDialog
import com.ados.mstrotrematch2.dialog.GemQuestionDialog
import com.ados.mstrotrematch2.dialog.ReportDialog
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.database.DBHelperReport
import com.ados.mstrotrematch2.model.*
import java.util.*

class DisplayBoardActivity : AppCompatActivity(), OnDisplayBoardItemClickListener {
    private lateinit var binding: ActivityDisplayBoardBinding

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    lateinit var recyclerViewAdapter : RecyclerViewAdapterDisplayBoard

    private var toast : Toast? = null
    var preferencesDTO : PreferencesDTO? = null
    var currentUser: UserDTO? = null
    var writeCount : Long = 0
    private var reportDialog: ReportDialog? = null
    private var displayBoardAddDialog: DisplayBoardAddDialog? = null
    private var gemQuestionDialog: GemQuestionDialog? = null
    lateinit var dbHandler : DBHelperReport

    private val anim = AlphaAnimation(0.1f, 1.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("user")
        preferencesDTO = intent.getParcelableExtra("preferences")

        dbHandler = DBHelperReport(this)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.rvDisplayBoard.layoutManager = layoutManager

        anim.duration = 800
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        binding.layoutTitle.textDisplayBoard.startAnimation(anim)
        binding.layoutTitle.textDisplayBoard.text = "\uD83C\uDF86 전광판 \uD83C\uDF89 광고 \uD83D\uDCE3"

        firebaseViewModel.getUserDisplayBoardWriteCount(currentUser?.uid.toString()) {
            writeCount = it
        }

        firebaseViewModel.getDisplayBoardsListen()
        firebaseViewModel.displayBoardDTOs.observe(this) {
            val itemsEx: ArrayList<DisplayBoardExDTO> = arrayListOf()
            for (display in firebaseViewModel.displayBoardDTOs.value!!) {
                itemsEx.add(DisplayBoardExDTO(display, dbHandler.getBlock(display.docName.toString())))
            }
            recyclerViewAdapter = RecyclerViewAdapterDisplayBoard(itemsEx, this)
            binding.rvDisplayBoard.adapter = recyclerViewAdapter
            binding.rvDisplayBoard.scrollToPosition(0)
        }

        binding.buttonAddAds.setOnClickListener {
            if (displayBoardAddDialog == null) {
                displayBoardAddDialog = DisplayBoardAddDialog(this)
                displayBoardAddDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                displayBoardAddDialog?.setCanceledOnTouchOutside(false)
            }
            displayBoardAddDialog?.currentUser = currentUser
            displayBoardAddDialog?.preferencesDTO = preferencesDTO
            displayBoardAddDialog?.writeCount = writeCount
            displayBoardAddDialog?.show()
            displayBoardAddDialog?.setInfo()
            displayBoardAddDialog?.binding?.buttonDisplayBoardAddCancel?.setOnClickListener { // Ok
                displayBoardAddDialog?.dismiss()
                displayBoardAddDialog = null
            }
            displayBoardAddDialog?.binding?.buttonDisplayBoardAddOk?.setOnClickListener { // Ok
                val displayText = displayBoardAddDialog?.binding?.editDisplayBoard?.text.toString().trim()
                val color = displayBoardAddDialog?.binding?.layoutDisplayBoardTest?.textDisplayBoard?.currentTextColor!!

                if (displayText.isNullOrEmpty()) {
                    callToast("내용을 입력 하세요.")
                } else if (displayBoardAddDialog?.useWriteCount!! <= 0) {
                    callToast("오늘은 더이상 전광판 등록을 할 수 없습니다.")
                } else {
                    val question = GemQuestionDTO("다이아를 사용해 전광판을 등록합니다.", preferencesDTO?.priceDisplayBoard)
                    if (gemQuestionDialog == null) {
                        gemQuestionDialog = GemQuestionDialog(this, question)
                        gemQuestionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        gemQuestionDialog?.setCanceledOnTouchOutside(false)
                    } else {
                        gemQuestionDialog?.question = question
                    }
                    gemQuestionDialog?.show()
                    gemQuestionDialog?.setInfo()
                    gemQuestionDialog?.binding?.buttonGemQuestionCancel?.setOnClickListener { // No
                        gemQuestionDialog?.dismiss()
                    }
                    gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                        displayBoardAddDialog?.dismiss()
                        displayBoardAddDialog = null
                        gemQuestionDialog?.dismiss()

                        if ((currentUser?.getTotalGem()!!) < preferencesDTO?.priceDisplayBoard!!) {
                            callToast("다이아가 부족합니다.")
                        } else {
                            firebaseViewModel.sendDisplayBoard(displayText, color, currentUser!!) {
                                // 다이아 차감
                                val oldPaidGemCount = currentUser?.paidGem!!
                                val oldFreeGemCount = currentUser?.freeGem!!
                                firebaseViewModel.useUserGem(currentUser?.uid.toString(), preferencesDTO?.priceDisplayBoard!!) { userDTO ->
                                    if (userDTO != null) {
                                        currentUser = userDTO

                                        var log = LogDTO("[다이아 차감] 전광판 등록으로 ${preferencesDTO?.priceDisplayBoard} 다이아 사용 (전광판 내용 -> \"displayText\"), (paidGem : $oldPaidGemCount -> ${currentUser?.paidGem}, freeGem : $oldFreeGemCount -> ${currentUser?.freeGem})", Date())
                                        firebaseViewModel.writeUserLog(currentUser?.uid.toString(), log) { }

                                        callToast("전광판 등록 완료!")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.buttonOk.setOnClickListener {
            finish()
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    override fun onItemClick(item: DisplayBoardExDTO, position: Int) {
        if (!dbHandler.getBlock(item.displayBoardDTO?.docName.toString())) {
            if (reportDialog == null) {
                reportDialog = ReportDialog(this)
                reportDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                reportDialog?.setCanceledOnTouchOutside(false)
            }
            reportDialog?.reportDTO = ReportDTO(currentUser?.uid, currentUser?.nickname, item.displayBoardDTO?.userUid, item.displayBoardDTO?.userNickname, item.displayBoardDTO?.displayText, item.displayBoardDTO?.docName, ReportDTO.Type.DisplayBoard)
            reportDialog?.show()
            reportDialog?.setInfo()

            reportDialog?.setOnDismissListener {
                if (!reportDialog?.reportDTO?.reason.isNullOrEmpty()) {
                    firebaseViewModel.sendReport(reportDialog?.reportDTO!!) {
                        if (!dbHandler.getBlock(reportDialog?.reportDTO?.contentDocName.toString())) {
                            dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 1)
                        } else {
                            dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 0)
                        }

                        item.isBlocked = true
                        recyclerViewAdapter.notifyItemChanged(position)
                        Toast.makeText(this, "신고 처리 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}