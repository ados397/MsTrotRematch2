package com.ados.mstrotrematch2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.ListItemRankBinding
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

class RecyclerViewAdapterRank(private val items: List<RankDTO>, private var clickListener: OnRankItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterRank.RankViewHolder>() {
    var context: Context? = null
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private fun formatNumber(value: Int): String {
        return when {
            value >= 1E3 -> "${decimalFormat.format((value.toFloat() / 1E3).toInt())}K"
            else -> NumberFormat.getInstance().format(value)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        context = parent.context
        val view = ListItemRankBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return RankViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        holder.initialize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                rank.text = "${position+4}위"
                name.text = item.name
                if (!item.subTitle.isNullOrEmpty()) {
                    count.text = item.subTitle
                } else if (item.updateDate != null) {
                    count.text = "${SimpleDateFormat("yyyy-MM-dd").format(item.updateDate)} 달성"
                } else {
                    count.text = "${decimalFormat.format(item.count)}"
                    //count.text = "득표수 : ${formatNumber(item.count!!)}표"
                }

                var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)
                if (image != null && imageID > 0) {
                    //image?.setImageResource(imageID)

                    Glide.with(image.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .optionalFitCenter()
                        .into(holder.image)
                }

                if (position+4 <= 7) { // 7위 까지는 붉은색 표시
                    layoutRank.setBackgroundColor(ContextCompat.getColor(context!!, R.color.display_board_10))
                } else {
                    layoutRank.setBackgroundColor(ContextCompat.getColor(context!!, R.color.purple2))
                }

                /*Glide.with(holder.itemView.context).load("@drawable.crown_gold").apply(
                    RequestOptions().optionalFitCenter()).into(image)*/
            }
        }
    }

    inner class RankViewHolder(private val viewBinding: ListItemRankBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        val rank = viewBinding.textRank
        var image = viewBinding.imgProfile
        val name = viewBinding.textName
        val count = viewBinding.textCount
        val layoutRank = viewBinding.layoutRank
//        var imageID = context.resources.getIdentifier(colorList.photo, "drawable", context.packageName)

        fun initialize(item: RankDTO, action: OnRankItemClickListener) {
            viewBinding.rankItemLayout.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }

    }

}

interface OnRankItemClickListener {
    fun onItemClick(item: RankDTO, position: Int)
}
