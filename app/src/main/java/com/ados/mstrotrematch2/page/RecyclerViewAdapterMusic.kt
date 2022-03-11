package com.ados.mstrotrematch2.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.ItemList
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.list_item_music.view.*

class RecyclerViewAdapterMusic(val items: List<ItemList>, var clickListener: OnMusicItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterMusic.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MusicViewHolder(parent)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        //holder.initalize(items.get(position),clickListener)
        holder.initalize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                title.text = "${item.snippet.title}"

                Glide.with(holder.itemView.context).load(item.snippet.thumbnails.medium.url).apply(
                    RequestOptions().centerCrop()).into(image)
            }
        }
    }

    inner class MusicViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_music, parent, false)) {
        var image = itemView.img_thumbnail
        val title = itemView.text_title

        fun initalize(item: ItemList, action:OnMusicItemClickListener) {
            itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }

    }

}

interface OnMusicItemClickListener {
    fun onItemClick(item: ItemList, position: Int)
}