package com.ados.mstrotrematch2.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.databinding.NoticeListItemBinding
import com.ados.mstrotrematch2.model.NewsDTO
import com.ados.mstrotrematch2.util.ToggleAnimation
import com.bumptech.glide.Glide

class RecyclerViewAdapterNotice(private val items: List<NewsDTO>) : RecyclerView.Adapter<RecyclerViewAdapterNotice.NoticeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = NoticeListItemBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return NoticeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.initialize()

        items[position].let { item ->
            with(holder) {
                layoutContent.visibility = View.GONE
                buttonExpand.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

                title.text = item.title.toString().replace("\\n","\n")
                time.text = item.time.toString().replace("\\n","\n")
                content.text = item.content.toString().replace("\\n","\n")

                if (!item.imageUrl.isNullOrEmpty()) {
                    Glide.with(imgNotice.context).load(item.imageUrl).optionalFitCenter().into(imgNotice)
                    imgNotice.visibility = View.VISIBLE
                } else {
                    imgNotice.visibility = View.GONE
                }

                // 최신 3개 항목은 오픈
                if (position <= 2) {
                    buttonExpand.performClick()
                }
            }
        }
    }

    inner class NoticeViewHolder(private val viewBinding: NoticeListItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var isExpanded = false
        var imgNotice = viewBinding.imgNotice
        var mainLayout = viewBinding.layoutMain
        val title = viewBinding.textTitle
        val time = viewBinding.textTime
        val content = viewBinding.textContent
        var layoutContent = viewBinding.layoutContent
        var buttonExpand = viewBinding.buttonExpand

        fun initialize() {
            viewBinding.buttonExpand.setOnClickListener {
                //viewBinding.layoutContent.visibility = View.VISIBLE

                isExpanded = toggleLayout(!isExpanded, viewBinding.buttonExpand, viewBinding.layoutContent)
            }

            viewBinding.layoutTitle.setOnClickListener {
                //viewBinding.layoutContent.visibility = View.VISIBLE

                isExpanded = toggleLayout(!isExpanded, viewBinding.buttonExpand, viewBinding.layoutContent)
            }
        }

        private fun toggleLayout(isExpanded: Boolean, view: View, rv: View): Boolean {
            ToggleAnimation.toggleArrow(view, isExpanded)
            if (isExpanded) {
                ToggleAnimation.expandAction(rv)
            } else {
                ToggleAnimation.collapse(rv)
            }
            return isExpanded
        }
    }

}
