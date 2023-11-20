package com.ados.mstrotrematch2.page

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.database.DBHelperCheeringBoard
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.database.DBHelperReport
import com.ados.mstrotrematch2.databinding.FragmentBoardBinding
import com.ados.mstrotrematch2.dialog.DeleteDialog
import com.ados.mstrotrematch2.dialog.ImageViewDialog
import com.ados.mstrotrematch2.dialog.QuestionDialog
import com.ados.mstrotrematch2.dialog.ReportDialog
import com.ados.mstrotrematch2.firebase.FirebaseStorageViewModel
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentBoard.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentBoard : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()
    private var toast : Toast? = null

    private lateinit var dbHandler : DBHelperCheeringBoard
    private lateinit var dbHandlerReport : DBHelperReport
    private var seasonDTO = SeasonDTO()
    lateinit var recyclerView : RecyclerView
    var item = BoardDTO()
    var itemPosition = 0


    private var reportDialog: ReportDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBoardBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        seasonDTO = (activity as MainActivity?)?.getSeason()!!

        dbHandler = DBHelperCheeringBoard(requireContext())
        dbHandlerReport = DBHelperReport(requireContext())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgPhoto.visibility = View.GONE

        if (item.isBlock) {
            binding.textTitle.text = "차단되었습니다."
            binding.textContent.text = "내가 신고한 글입니다."
            binding.textTitle.setTextColor(Color.parseColor("#CCCCCC"))
            binding.textContent.setTextColor(Color.parseColor("#CCCCCC"))
            binding.imgPhoto.visibility = View.GONE
            binding.imgSiren.visibility = View.GONE
        } else {
            binding.textTitle.text = item.title
            binding.textContent.text = item.content
        }

        val userDTO = (activity as MainActivity?)?.getUser()!!
        if (item.userUid == userDTO.uid) { // 내가 작성한 글이면 신고 버튼 숨김
            binding.imgSiren.visibility = View.GONE
        } else { // 다른 사람이 작성한 글이면 삭제 버튼 숨김
            binding.imgDelete.visibility = View.GONE // 관리자모드
        }

        binding.textName.text = item.name
        binding.textContent.movementMethod = LinkMovementMethod()
        binding.textTime.text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.time)
        binding.textLikeCount.text = item.likeCount.toString()

        /*Glide.with(binding.imgLike.context)
            .asBitmap()
            .load(R.drawable.not_like) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgLike)*/

        if (item.imageUrl.isNullOrEmpty()) {
            var imageID = requireContext().resources.getIdentifier(item.image, "drawable", requireContext().packageName)
            //img_profile.setImageResource(imageID)
            Glide.with(binding.imgProfile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.imgProfile)
        } else {
            Glide.with(binding.imgProfile.context).load(item.imageUrl).apply(
                RequestOptions().centerCrop()).into(binding.imgProfile)
        }

        getPhotoUri(item) { uri ->
            binding.imgPhoto.visibility = View.VISIBLE
            Glide.with(requireContext()).load(uri).optionalFitCenter().into(binding.imgPhoto)
        }

        // 좋아요, 싫어요 했으면 밑줄 표시
        println("아이템 $item")
        if (dbHandler.getLike(item.docname.toString())) {
            binding.textLikeCount.paintFlags = binding.textLikeCount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            /*Glide.with(binding.imgLike.context)
                .asBitmap()
                .load(R.drawable.like) ///feed in path of the image
                .optionalFitCenter()
                .into(binding.imgLike)*/
            binding.imgLike.setImageResource(R.drawable.like)
        }

        binding.imgProfile.setOnClickListener {
            val imageID = requireContext().resources.getIdentifier(item.image, "drawable", requireContext().packageName)
            val dialog = ImageViewDialog(requireContext())
            dialog.imageID = imageID
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.show()
        }

        // 좋아요 클릭
        binding.imgLike.setOnClickListener {
            clickLike()
        }
        binding.textLikeCount.setOnClickListener {
            clickLike()
        }

        binding.imgDelete.setOnClickListener {
            var question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "응원글 삭제",
                "한번 삭제된 응원글은 복구할 수 없습니다.\n\n정말 삭제하시겠습니까?"
            )

            val dialog = QuestionDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.binding.buttonQuestionCancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.binding.buttonQuestionOk.setOnClickListener {
                firebaseViewModel.deleteCheeringBoard(item.docname.toString(), seasonDTO.seasonNum!!, seasonDTO.getWeek()) {
                    setFragmentResult("notifyItemRemoved", bundleOf("position" to itemPosition))
                    callToast("삭제되었습니다.")
                    dialog.dismiss()
                    finishFragment()
                }
            }
        }

        binding.imgSiren.setOnClickListener {
            val userDTO = (activity as MainActivity?)?.getUser()!!
            if (reportDialog == null) {
                reportDialog = ReportDialog(requireContext())
                reportDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                reportDialog?.setCanceledOnTouchOutside(false)
            }
            reportDialog?.reportDTO = ReportDTO(userDTO?.uid, userDTO?.nickname, item.userUid, item.name, item.content, item.docname, ReportDTO.Type.CheeringBoard)
            reportDialog?.show()
            reportDialog?.setInfo()

            reportDialog?.setOnDismissListener {
                if (!reportDialog?.reportDTO?.reason.isNullOrEmpty()) {
                    firebaseViewModel.sendReport(reportDialog?.reportDTO!!) {
                        if (!dbHandlerReport.getBlock(reportDialog?.reportDTO?.contentDocName.toString())) {
                            dbHandlerReport.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 1)
                        } else {
                            dbHandlerReport.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 0)
                        }

                        // 신고된 항목 갱신
                        item.isBlock = true
                        setFragmentResult("notifyItemChanged", bundleOf("position" to itemPosition))
                        callToast("신고 처리 완료되었습니다.")
                        finishFragment()
                    }
                }
            }

            /*val dialog = ReportDialog(requireContext(), item, requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.binding.buttonCancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.setOnDismissListener {
                if (dialog.isReport) {
                    isReport = true
                    binding.textTitle.text = "차단되었습니다."
                    binding.textContent.text = "내가 신고한 응원글 또는 사용자 입니다."
                    binding.textTitle.setTextColor(Color.parseColor("#CCCCCC"))
                    binding.textContent.setTextColor(Color.parseColor("#CCCCCC"))
                    binding.buttonReport.visibility = View.GONE
                }
            }*/
        }

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.layoutMain.setOnClickListener {
            // 뒤에 응원글 리스트 클릭이 되어서 막음
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun callBackPressed() {
        finishFragment()
    }

    private fun finishFragment() {
        parentFragmentManager.popBackStack()
        /*val fragment = FragmentPageCheering()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }*/
    }

    private fun clickLike() {
        var likeCount = -1
        var paintFlag = Paint.ANTI_ALIAS_FLAG
        var imgLike = R.drawable.not_like
        if (!dbHandler.getLike(item.docname.toString())) {
            dbHandler.updateLike(item.docname.toString(), 1)
            likeCount = 1
            paintFlag = binding.textLikeCount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            imgLike = R.drawable.like
            callToast("좋아요")
        } else {
            dbHandler.updateLike(item.docname.toString(), 0)
            callToast("좋아요 취소")
        }

        item.likeCount = item.likeCount!!.plus(likeCount)
        binding.textLikeCount.text = "${item.likeCount}"
        binding.textLikeCount.paintFlags = paintFlag
        /*Glide.with(binding.imgLike.context)
            .asBitmap()
            .load(imgLike) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgLike)*/
        binding.imgLike.setImageResource(imgLike)

        firebaseViewModel.setCheeringBoardLike(item.docname.toString(), seasonDTO.seasonNum!!, seasonDTO.getWeek(), likeCount) {
            item = it!!
            /*binding.textLikeCount.text = "${item.likeCount}"
            if (likeCount > 0) {
                binding.textLikeCount.paintFlags = binding.textLikeCount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                callToast("좋아요")
            } else {
                binding.textLikeCount.paintFlags = Paint.ANTI_ALIAS_FLAG
                callToast("좋아요 취소")
            }*/
        }

        val userDTO = (activity as MainActivity?)?.getUser()!!
        if (!QuestDTO("좋아요", "응원글 좋아요 1회 누르기", 1, userDTO.questSuccessTimes["3"], userDTO.questGemGetTimes["3"]).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
            userDTO.questSuccessTimes["3"] = Date()
            firebaseViewModel.updateUserQuestSuccessTimes(userDTO) {
                callToast("일일 과제 달성! 보상을 획득하세요!")
            }
        }

        Thread.sleep(300L)
    }

    private fun getPhotoUri(item: BoardDTO, myCallback: (Uri?) -> Unit) {
        val seasonDTO = (activity as MainActivity?)?.getSeason()!!
        if (item.isPhoto!!) {
            firebaseStorageViewModel.getCheeringBoardImage(seasonDTO.seasonNum!!, item.docname.toString()) { uri ->
                myCallback(uri)
            }
        } else {
            myCallback(null)
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentBoard.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentBoard().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}