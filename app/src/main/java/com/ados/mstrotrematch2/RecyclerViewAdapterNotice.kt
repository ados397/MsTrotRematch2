package com.ados.mstrotrematch2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.model.NewsDTO
import kotlinx.android.synthetic.main.notice_list_item.view.*

class RecyclerViewAdapterNotice(val items: List<NewsDTO>) : RecyclerView.Adapter<RecyclerViewAdapterNotice.NoticeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NoticeViewHolder(parent)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        items[position].let { item ->
            with(holder) {
                title.text = item.title.toString().replace("\\n","\n")
                time.text = item.time.toString().replace("\\n","\n")
                content.text = item.content.toString().replace("\\n","\n")
            }
        }
    }

    inner class NoticeViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.notice_list_item, parent, false)) {
        val title = itemView.text_title
        var time = itemView.text_time
        val content = itemView.text_content
    }

}
