package com.ados.mstrotrematch2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.parseAsHtml
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.databinding.ListItemMusicBinding
import com.ados.mstrotrematch2.model.ItemList
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class RecyclerViewAdapterMusic(private val items: List<ItemList>, private var clickListener: OnMusicItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterMusic.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = ListItemMusicBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        //holder.initialize(items.get(position),clickListener)
        holder.initialize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                title.text = "${item.snippet.title?.parseAsHtml()}"

                Glide.with(holder.itemView.context).load(item.snippet.thumbnails.medium.url).apply(
                    RequestOptions().centerCrop()).into(image)
            }
        }
    }

    inner class MusicViewHolder(private val viewBinding: ListItemMusicBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var image = viewBinding.imgThumbnail
        val title = viewBinding.textTitle

        fun initialize(item: ItemList, action: OnMusicItemClickListener) {
            viewBinding.musicItemLayout.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }

    }

}

interface OnMusicItemClickListener {
    fun onItemClick(item: ItemList, position: Int)
}