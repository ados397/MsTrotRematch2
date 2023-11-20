package com.ados.mstrotrematch2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.util.Linkify
import android.view.Window
import com.ados.mstrotrematch2.databinding.ActivityDonationCertificateBinding
import com.ados.mstrotrematch2.dialog.ImageViewDialog
import com.ados.mstrotrematch2.model.DonationNewsDTO
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.DecimalFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

class DonationCertificateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDonationCertificateBinding
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private lateinit var donationNewsDTO: ArrayList<DonationNewsDTO>
    private lateinit var item: RankDTO
    private var rank = 0
    var hallOfFameType = HallOfFameActivity.HallOfFameType.MR_OLD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationCertificateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        donationNewsDTO = intent.getParcelableArrayListExtra("donationNews")!!
        item = intent.getParcelableExtra("item")!!
        rank = intent.getIntExtra("rank", 1)
        hallOfFameType = intent.getSerializableExtra("hallOfFameType") as HallOfFameActivity.HallOfFameType

        binding.textTitle.text = "[ ${item.name} 가수님 후원증서 ]"

        if (!item.donationUrl.isNullOrEmpty()) {
        //if (true) {
            Glide.with(binding.imgDonationCertificate.context)
                .load(item.donationUrl)
                .apply(
                    RequestOptions().optionalFitCenter()
                ).into(binding.imgDonationCertificate)

            var donation = item.count?.div(1000000)?.times(10)
            var donationTotal = donation
            var crownDonation = ""
            if (hallOfFameType == HallOfFameActivity.HallOfFameType.MR_OLD) {
                when (rank) { // 기존 리매치만 우승 상금 추가
                    1 -> {
                        donationTotal = donationTotal?.plus(30)
                        crownDonation = "- 금 왕관 30만원 추가 적립"
                    }
                    2 -> {
                        donationTotal = donationTotal?.plus(20)
                        crownDonation = "- 은 왕관 20만원 추가 적립"
                    }
                    3 -> {
                        donationTotal = donationTotal?.plus(10)
                        crownDonation = "- 동 왕관 10만원 추가 적립"
                    }
                }
            }

            binding.textDesc1.text = "${item.name} 가수님의 소중한 한표한표가 모여 소아암, 난치병 어린이들을 위한 ${donationTotal}만원의 소중한 후원금으로 전달 되었습니다."

            binding.textDesc2.text = "- 총 득표수 : ${decimalFormat.format(item.count)} 표\n" +
                    "- 총 적립 금액 : ${donationTotal}만원\n" +
                    "- 100만 표마다 10만원씩 총 ${donation}만원 적립\n" +
                    "${crownDonation}"

            var patterns : ArrayList<Pattern> = arrayListOf()
            var transforms : ArrayList<Linkify.TransformFilter> = arrayListOf()

            var newsStr = "[기부관련 신문기사]"
            for (news in donationNewsDTO) {
                newsStr += "\n${news.order}. ${news.company}"

                val pattern = Pattern.compile(news.company)
                patterns.add(pattern)

                val transform = Linkify.TransformFilter(object : Linkify.TransformFilter, (Matcher, String) -> String {
                    override fun transformUrl(p0: Matcher?, p1: String?): String {
                        return news.url!!
                    }
                    override fun invoke(p1: Matcher, p2: String): String {
                        return news.url!!
                    }
                })
                transforms.add(transform)
            }
            binding.textDesc3.text = newsStr
            for (i in patterns.indices) {
                Linkify.addLinks(binding.textDesc3, patterns[i], "", null, transforms[i])
            }
        } else {
            binding.textDesc1.text = "${item.name} 가수님의 기부증서가 곧 업로드 될 예정입니다."
            binding.textDesc2.text = ""
            binding.textDesc3.text = ""
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.imgDonationCertificate.setOnClickListener {
            val dialog = ImageViewDialog(this)
            dialog.imageUrl = item.donationUrl
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.show()
        }
    }
}