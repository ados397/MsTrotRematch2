package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.DonationNewsDTO
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.donation_certificate_dialog.*
import java.text.DecimalFormat
import java.util.regex.Matcher
import java.util.regex.Pattern


class DonationCertificateDialog(context: Context, val item: RankDTO, val rank: Int, val donationNewsDTO : ArrayList<DonationNewsDTO>) : Dialog(context), View.OnClickListener {

    private val layout = R.layout.donation_certificate_dialog
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        text_title.text = "[ ${item.name} 가수님 기부증서 ]"

        if (!item.donationUrl.isNullOrEmpty()) {
            Glide.with(img_donation_certificate.context)
                .load(item.donationUrl)
                .apply(
                    RequestOptions().fitCenter()
                ).into(img_donation_certificate)

            var donation = item.count?.div(1000000)?.times(10)
            var donationTotal = donation
            var crwonDonation = ""
            if (rank == 1) {
                donationTotal = donationTotal?.plus(30)
                crwonDonation = "- 금 왕관 30만원 추가 적립"
            } else if (rank == 2) {
                donationTotal = donationTotal?.plus(20)
                crwonDonation = "- 은 왕관 20만원 추가 적립"
            } else if (rank == 3) {
                donationTotal = donationTotal?.plus(10)
                crwonDonation = "- 동 왕관 10만원 추가 적립"
            }

            text_desc1.text = "${item.name} 가수님의 소중한 한표한표가 모여 소아암.난치병 어린이들을 위한 ${donationTotal}만원의 소중한 기부금으로 전달 되었습니다."

            text_desc2.text = "- 총 득표수 : ${decimalFormat.format(item.count)} 표\n" +
                    "- 총 적립 금액 : ${donationTotal}만원\n" +
                    "- 100만 표마다 10만원씩 총 ${donation}만원 적립\n" +
                    "${crwonDonation}"

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
            text_desc3.text = newsStr
            for (i in patterns.indices) {
                Linkify.addLinks(text_desc3, patterns[i], "", null, transforms[i])
            }
        } else {
            text_desc1.text = "${item.name} 가수님의 기부증서가 곧 업로드 될 예정입니다."
            text_desc2.text = ""
            text_desc3.text = ""
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