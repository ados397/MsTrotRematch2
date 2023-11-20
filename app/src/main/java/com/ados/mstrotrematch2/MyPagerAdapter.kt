package com.ados.mstrotrematch2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.mstrotrematch2.page.*

class MyPagerAdapter(private val fragmentPageMusic: FragmentPageMusic, fa: FragmentActivity) : FragmentStateAdapter(fa) {
    //private val NUM_PAGES = 5
    private val NUM_PAGES = 6 // 팬클럽 적용

    override fun getItemCount(): Int  = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            /*0 -> {
                FragmentPageRankMain()
            }
            1 -> {
                FragmentPageVoteMain()
            }
            2 -> {
                fragmentPageMusic
            }
            3 -> {
                FragmentPageCheeringMain()
            }
            else -> {
                FragmentPageAccount()
            }*/
            0 -> {
                FragmentPageRankMain()
            }
            1 -> {
                FragmentPageVoteMain()
            }
            2 -> {
                FragmentPageFanClubMain() // 팬클럽 적용
            }
            3 -> {
                fragmentPageMusic
            }
            4 -> {
                FragmentPageCheeringMain()
            }
            else -> {
                FragmentPageAccount()
            }
        }
    }
}