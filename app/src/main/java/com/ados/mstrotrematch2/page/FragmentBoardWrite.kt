package com.ados.mstrotrematch2.page

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.MainActivity
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.adapter.RecyclerViewAdapterVote
import com.ados.mstrotrematch2.databinding.FragmentBoardWriteBinding
import com.ados.mstrotrematch2.dialog.ImageSelectDialog
import com.ados.mstrotrematch2.firebase.FirebaseRepository
import com.ados.mstrotrematch2.firebase.FirebaseStorageViewModel
import com.ados.mstrotrematch2.firebase.FirebaseViewModel
import com.ados.mstrotrematch2.model.BoardDTO
import com.ados.mstrotrematch2.model.QuestDTO
import com.ados.mstrotrematch2.model.RankExDTO
import com.ados.mstrotrematch2.util.AdsInterstitialManager
import com.ados.mstrotrematch2.util.MySharedPreferences
import com.ados.mstrotrematch2.util.Utility
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentBoardWrite.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentBoardWrite : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentBoardWriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()
    private var toast : Toast? = null

    var boardDTO = BoardDTO()
    private var photoBitmap: Bitmap? = null
    lateinit var recyclerView : RecyclerView
    var imagename : String? = null
    var peopleDocName : String? = null
    var imageurl : String? = null
    var isWrite = false

    private var imageOK: Boolean = false
    private var titleOK: Boolean = false
    private var contentOK: Boolean = false

    // AD
    private var adsInterstitialManager : AdsInterstitialManager? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            photoBitmap = (activity as MainActivity?)?.getBitmap(uri)
            if (photoBitmap == null) {
                callToast("사진 불러오기 실패. 잠시 후 다시 시도해 주세요.")
            } else {
                boardDTO.isPhoto = true
                binding.imgPhoto.setImageBitmap(photoBitmap)
                binding.layoutPhoto.visibility = View.VISIBLE
                binding.layoutLoadPhoto.visibility = View.GONE
            }
        } else {
            callToast("사진 불러오기 실패. 잠시 후 다시 시도해 주세요.")
        }
    }

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
        _binding = FragmentBoardWriteBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        binding.layoutPhoto.visibility = View.GONE
        binding.imgPremium.visibility = View.GONE

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
        binding.textDesc.text = "응원글 작성 시 투표권 ${preferencesDTO.cheeringTicketCount}장 획득"

        val userDTO = (activity as MainActivity?)?.getUser()!!
        val writeCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_WRITE_CHEERING, 0)
        var writeCountMax = preferencesDTO.writeCount!!
        if (userDTO.isPremium()) {
            writeCountMax = preferencesDTO.writeCount!!.times(2)
            binding.imgPremium.visibility = View.VISIBLE
        }
        binding.textWriteCount.text = "남은횟수 : ${writeCountMax.minus(writeCount)}"

        Glide.with(binding.imgSelectProfile.context)
            .asBitmap()
            .load(R.drawable.selectimg) ///feed in path of the image
            .optionalFitCenter()
            .into(binding.imgSelectProfile)

        // 가수 리스트를 호출하면 이미지 선택 대화상자 호출
        firebaseViewModel.peopleDTOs.observe(viewLifecycleOwner) {
            if (firebaseViewModel.peopleDTOs.value != null) {
                var peopleExDTOs : ArrayList<RankExDTO> = arrayListOf()
                for (person in firebaseViewModel.peopleDTOs.value!!) {
                    var isFavorite = false
                    if (userDTO.favorites.contains(person.docname)) {
                        isFavorite = true
                    }
                    peopleExDTOs.add(RankExDTO(person, isFavorite))
                }
                peopleExDTOs.sortByDescending { it.favorite }

                val dialog = ImageSelectDialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.peopleExDTOs = peopleExDTOs
                dialog.show()

                dialog.binding.buttonCancel.setOnClickListener { // No
                    dialog.dismiss()
                }

                dialog.setOnDismissListener {
                    if (dialog.imgUrl.isNotEmpty()) {
                        imageurl = dialog.imgUrl
                        Glide.with(binding.imgSelectProfile.context).load(dialog.imgUrl).apply(
                            RequestOptions().centerCrop()).into(binding.imgSelectProfile)
                    } else if (dialog.imgName.isNotEmpty()) {
                        imagename = dialog.imgName
                        peopleDocName = dialog.peopleDocName
                        var imageID = requireContext().resources.getIdentifier(imagename, "drawable", requireContext().packageName)
                        //img_select_profile?.setImageResource(imageID)
                        Glide.with(binding.imgSelectProfile.context)
                            .asBitmap()
                            .load(imageID) ///feed in path of the image
                            .optionalFitCenter()
                            .into(binding.imgSelectProfile)

                        binding.textPeople.text = dialog.peopleName

                        imageOK = true
                        visibleOkButton()
                    }
                }
            }
        }

        binding.imgSelectProfile.setOnClickListener {
            firebaseViewModel.getPeople(FirebaseRepository.PeopleOrder.NAME_ASC)
        }

        binding.buttonWrite.setOnClickListener {
            writePost()
        }

        binding.editContent.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.layoutLoadPhoto.setOnClickListener {

            resultLauncher.launch("image/*")
        }

        binding.imgDeletePhoto.setOnClickListener {
            binding.layoutPhoto.visibility = View.GONE
            binding.layoutLoadPhoto.visibility = View.VISIBLE
            //scheduleDTO.isPhoto = false
            photoBitmap = null
        }

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.editTitle.doAfterTextChanged {
            if (binding.editTitle.text.toString().isNullOrEmpty()) {
                binding.textTitleError.text = "제목을 입력해 주세요."
                binding.editTitle.setBackgroundResource(R.drawable.edit_rectangle_red)
                titleOK = false
            } else {
                binding.textTitleError.text = ""
                binding.editTitle.setBackgroundResource(R.drawable.edit_rectangle)
                titleOK = true
            }

            binding.textTitleLen.text = "${binding.editTitle.text.length}/18"

            visibleOkButton()
        }

        binding.editContent.doAfterTextChanged {
            if (binding.editContent.text.toString().isNullOrEmpty()) {
                binding.textContentError.text = "내용을 입력해 주세요."
                binding.editContent.setBackgroundResource(R.drawable.edit_rectangle_red)
                contentOK = false
            } else {
                binding.textContentError.text = ""
                binding.editContent.setBackgroundResource(R.drawable.edit_rectangle)
                contentOK = true
            }

            binding.textContentLen.text = "${binding.editContent.text.length}/500"

            visibleOkButton()
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
        val fragment = FragmentPageCheering()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun isValidPassword(id: String): Boolean {
        val reg = Regex("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9!@#\$%^&*]).{8,30}\$")
        if(!id.matches(reg)) return false

        return true
    }

    private fun  writePost() {
        /*val passWord = binding.editPassword.text.toString().replace(" ","")

        if (binding.editTitle.text.toString().replace(" ", "").isNullOrEmpty()) {
            callToast("제목을 입력해주세요.")
            return
        } else if (binding.editName.text.toString().replace(" ", "").isNullOrEmpty()) {
            callToast("이름을 입력해주세요.")
            return
        } else if (passWord.isNullOrEmpty()) {
            callToast("비밀번호를 입력해주세요.")
            return
        } else if (!isValidPassword(passWord)) {
            callToast("비밀번호는 6자 이상 숫자, 문자, 특수문자 중 2가지가 포함되어야 합니다.")
            return
        } else if (binding.editContent.text.toString().replace(" ", "").isNullOrEmpty()) {
            callToast("내용을 입력해주세요.")
            return
        } else if (imagename.isNullOrEmpty() and imageurl.isNullOrEmpty()) {
            callToast("이미지를 선택해주세요.")
            return
        }*/

        (activity as MainActivity?)?.loading()
        val userDTO = (activity as MainActivity?)?.getUser()!!
        boardDTO.docname = Utility.randomDocumentName()
        boardDTO.peopleDocName = peopleDocName
        boardDTO.image = imagename
        boardDTO.imageUrl = imageurl
        boardDTO.title = binding.editTitle.text.toString()
        boardDTO.content = binding.editContent.text.toString()
        boardDTO.name = userDTO.nickname
        boardDTO.userUid = userDTO.uid
        boardDTO.time = Date()

        val writeCount = sharedPreferences.getIntDate(MySharedPreferences.PREF_KEY_WRITE_CHEERING, 0)
        sharedPreferences.putIntDate(MySharedPreferences.PREF_KEY_WRITE_CHEERING, writeCount.plus(1))

        val adPolicyDTO = (activity as MainActivity?)?.getAdPolicy()!!
        val seasonDTO = (activity as MainActivity?)?.getSeason()!!
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
        firebaseViewModel.addCheeringBoard(boardDTO!!, seasonDTO.seasonNum!!, seasonDTO.getWeek()) {
            firebaseViewModel.addUserTicket(userDTO.uid.toString(), preferencesDTO.cheeringTicketCount!!) {
                if (!QuestDTO("응원하기", "응원글 1회 작성하기", 1, userDTO.questSuccessTimes["2"], userDTO.questGemGetTimes["2"]).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
                    userDTO.questSuccessTimes["2"] = Date()
                    firebaseViewModel.updateUserQuestSuccessTimes(userDTO) {
                        callToast("일일 과제 달성! 보상을 획득하세요!")
                    }
                } else {
                    callToast("투표권이 ${preferencesDTO.cheeringTicketCount}장 추가되었습니다!")
                }
            }

            if (boardDTO.isPhoto) {
                if (photoBitmap != null) { // isPhoto 가 true 이고 photoBitmap 가 null 이라면 스케줄 수정에서 이미지가 변경되지 않았을 경우임. 이때는 이미지가 바뀐게 없어서 스토리지에 저장할 필요 없음.
                    firebaseStorageViewModel.setCheeringBoardImage(seasonDTO.seasonNum!!, boardDTO.docname.toString(), photoBitmap!!) {
                        (activity as MainActivity?)?.loadingEnd()
                        adsInterstitialManager = AdsInterstitialManager(requireActivity(), adPolicyDTO)
                        adsInterstitialManager?.callInterstitial { }
                        finishFragment()
                    }
                } else {
                    (activity as MainActivity?)?.loadingEnd()
                    adsInterstitialManager = AdsInterstitialManager(requireActivity(), adPolicyDTO)
                    adsInterstitialManager?.callInterstitial { }
                    finishFragment()
                }
            } else {
                (activity as MainActivity?)?.loadingEnd()
                adsInterstitialManager = AdsInterstitialManager(requireActivity(), adPolicyDTO)
                adsInterstitialManager?.callInterstitial { }
                finishFragment()
            }
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    // 유효성 체크
    private fun visibleOkButton() {
        binding.buttonWrite.isEnabled = imageOK && titleOK && contentOK
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentBoardWrite.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentBoardWrite().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}