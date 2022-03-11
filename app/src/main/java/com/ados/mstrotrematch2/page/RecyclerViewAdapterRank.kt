package com.ados.mstrotrematch2.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_rank.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class RecyclerViewAdapterRank(val items: List<RankDTO>, var clickListener: OnRankItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterRank.RankViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RankViewHolder(parent)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        holder.initalize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                rank.text = "${position+4}위"
                name.text = item.name
                if (!item.subTitle.isNullOrEmpty()) {
                    count.text = item.subTitle
                } else if (item.updateDate != null) {
                    count.text = "${SimpleDateFormat("yyyy-MM-dd").format(item.updateDate)} 달성"
                } else {
                    count.text = "득표수 : ${decimalFormat.format(item.count)}표"
                }

                var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)
                if (image != null && imageID > 0) {
                    //image?.setImageResource(imageID)

                    Glide.with(image.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .fitCenter()
                        .into(holder.image)
                }

                /*Glide.with(holder.itemView.context).load("@drawable.crown_gold").apply(
                    RequestOptions().fitCenter()).into(image)*/
            }
        }
    }

    inner class RankViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_rank, parent, false)) {
        val rank = itemView.text_rank
        var image = itemView.img_profile
        val name = itemView.text_name
        val count = itemView.text_count
//        var imageID = context.resources.getIdentifier(colorList.photo, "drawable", context.packageName)

        fun initalize(item: RankDTO, action: OnRankItemClickListener) {
            itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }

    }

}

interface OnRankItemClickListener {
    fun onItemClick(item: RankDTO, position: Int)
}
