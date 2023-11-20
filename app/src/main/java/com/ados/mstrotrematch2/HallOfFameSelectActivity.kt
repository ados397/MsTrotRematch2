package com.ados.mstrotrematch2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ados.mstrotrematch2.databinding.ActivityHallOfFameSelectBinding
import com.ados.mstrotrematch2.model.AdPolicyDTO
import com.ados.mstrotrematch2.model.SeasonDTO
import com.ados.mstrotrematch2.util.AdsBannerManager

class HallOfFameSelectActivity : AppCompatActivity() {
    private var _binding: ActivityHallOfFameSelectBinding? = null
    private val binding get() = _binding!!

    lateinit var adPolicyDTO: AdPolicyDTO
    lateinit var seasonDTO: SeasonDTO
    private var adsBannerManager : AdsBannerManager? = null // AD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHallOfFameSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adPolicyDTO = intent.getParcelableExtra("adPolicy")!!
        seasonDTO = intent.getParcelableExtra("season")!!
        adsBannerManager = AdsBannerManager(this, lifecycle, adPolicyDTO, binding.adViewAdmob, binding.xmladview, binding.adViewKakao)
        adsBannerManager?.callBanner {

        }

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonNew.setOnClickListener {
            startHallOfFameActivity(HallOfFameActivity.HallOfFameType.MR_NEW)
        }

        binding.buttonMr1.setOnClickListener {
            startHallOfFameActivity(HallOfFameActivity.HallOfFameType.MR_OLD)
        }
    }

    private fun startHallOfFameActivity(hallOfFameType: HallOfFameActivity.HallOfFameType) {
        var intent = Intent(this, HallOfFameActivity::class.java)
        intent.putExtra("adPolicy", adPolicyDTO)
        intent.putExtra("season", seasonDTO)
        intent.putExtra("hallOfFameType", hallOfFameType)
        startActivity(intent)
    }

}