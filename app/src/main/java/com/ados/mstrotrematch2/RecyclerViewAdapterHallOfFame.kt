package com.ados.mstrotrematch2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.model.RankDTO
import kotlinx.android.synthetic.main.list_item_rank.view.*
import java.text.DecimalFormat

class RecyclerViewAdapterHallOfFame(val items: List<RankDTO>) : RecyclerView.Adapter<RecyclerViewAdapterHallOfFame.HallOfFameViewHolder>() {

//        RankDTO("9위", "profile06", "ㅇㅇㅇ", "득표수 : 111명")

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = HallOfFameViewHolder(parent)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: HallOfFameViewHolder, position: Int) {
        items[position].let { item ->
            with(holder) {
                rank.text = "${position+4}위"
                name.text = item.name
                count.text = "득표수:${decimalFormat.format(item.count)}"

                var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)
                if (image != null && imageID > 0)
                    image?.setImageResource(imageID)

                /*Glide.with(holder.itemView.context).load("@drawable.crown_gold").apply(
                    RequestOptions().fitCenter()).into(image)*/
            }
        }
    }

    inner class HallOfFameViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_rank, parent, false)) {
        val rank = itemView.text_rank
        var image = itemView.img_profile
        val name = itemView.text_name
        val count = itemView.text_count
//        var imageID = context.resources.getIdentifier(colorList.photo, "drawable", context.packageName)


    }

}
