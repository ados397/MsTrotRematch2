package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.ados.mstrotrematch2.databinding.DonationCertificateDialogBinding
import com.ados.mstrotrematch2.model.DonationNewsDTO
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.DecimalFormat
import java.util.regex.Matcher
import java.util.regex.Pattern


class DonationCertificateDialog(context: Context, val item: RankDTO, val rank: Int, val donationNewsDTO : ArrayList<DonationNewsDTO>) : Dialog(context), View.OnClickListener {

    lateinit var binding: DonationCertificateDialogBinding
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DonationCertificateDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        binding.textTitle.text = "[ ${item.name} 가수님 기부증서 ]"

        if (!item.donationUrl.isNullOrEmpty()) {
            Glide.with(binding.imgDonationCertificate.context)
                .load(item.donationUrl)
                .apply(
                    RequestOptions().optionalFitCenter()
                ).into(binding.imgDonationCertificate)

            var donation = item.count?.div(1000000)?.times(10)
            var donationTotal = donation
            var crownDonation = ""
            when (rank) {
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

            binding.textDesc1.text = "${item.name} 가수님의 소중한 한표한표가 모여 소아암.난치병 어린이들을 위한 ${donationTotal}만원의 소중한 기부금으로 전달 되었습니다."

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

        binding.imgDonationCertificate.setOnClickListener {
            val dialog = ImageViewDialog(context)
            dialog.imageUrl = item.donationUrl
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.show()
        }
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