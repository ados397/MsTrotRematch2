package com.ados.mstrotrematch2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.mstrotrematch2.page.*

//class MyPagerAdapterFanClub(fa: FragmentActivity, fanClub: FanClubDTO, member: MemberDTO) : FragmentStateAdapter(fa) {
class MyPagerAdapterFanClub(fm: FragmentManager, life: Lifecycle) : FragmentStateAdapter(fm, life) {
    private val NUM_PAGES = 5

    override fun getItemCount(): Int = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        println("탭 : position ${position}")

        return when (position) {
            0 -> {
                FragmentFanClubInfo()
            }
            1 -> {
                //FragmentFanClubMember()
                FragmentFanClubInfo()
            }
            2 -> {
                //FragmentFanClubRank()
                FragmentFanClubInfo()
            }
            3 -> {
                //FragmentFanClubManagement()
                FragmentFanClubInfo()
            }
            4 -> {
                //FragmentPageSchedule.newInstance("fanClub", "")
                FragmentFanClubInfo()
            }
            else -> {
                //FragmentFanClubManagement()
                FragmentFanClubInfo()
            }
        }
    }
}