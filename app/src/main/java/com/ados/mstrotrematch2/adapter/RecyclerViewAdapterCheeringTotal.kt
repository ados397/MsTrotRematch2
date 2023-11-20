package com.ados.mstrotrematch2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.ListItemCheeringTotalBinding
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class RecyclerViewAdapterCheeringTotal(private val items: List<RankDTO>) : RecyclerView.Adapter<RecyclerViewAdapterCheeringTotal.CheeringTotalViewHolder>() {
    var context: Context? = null
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheeringTotalViewHolder {
        context = parent.context
        val view = ListItemCheeringTotalBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return CheeringTotalViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: CheeringTotalViewHolder, position: Int) {
        items[position].let { item ->
            with(holder) {
                Glide.with(imgLike.context)
                    .asBitmap()
                    .load(R.drawable.like) ///feed in path of the image
                    .optionalFitCenter()
                    .into(imgLike)

                textRank.text = "${position+1}위"
                textName.text = item.name
                textTotalCount.text = "총점 : ${decimalFormat.format(item.cheeringCountTotal)}"
                textBoardCount.text = "응원글 : ${decimalFormat.format(item.cheeringCount)}"
                textLikeCount.text = decimalFormat.format(item.likeCount)

                val imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)

                if (imageID > 0) {
                    Glide.with(imageProfile.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .optionalFitCenter()
                        .into(holder.imageProfile)
                }

                if (position+1 <= 7) { // 7위 까지는 붉은색 표시
                    layoutRank.setBackgroundColor(ContextCompat.getColor(context!!, R.color.display_board_10))
                } else {
                    layoutRank.setBackgroundColor(ContextCompat.getColor(context!!, R.color.purple2))
                }
            }
        }
    }

    inner class CheeringTotalViewHolder(private val viewBinding: ListItemCheeringTotalBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        val layoutRank = viewBinding.layoutRank
        val imageProfile = viewBinding.imgProfile
        val textRank = viewBinding.textRank
        val textName = viewBinding.textName
        val textTotalCount = viewBinding.textTotalCount
        val textBoardCount = viewBinding.textBoardCount
        val textLikeCount = viewBinding.textLikeCount
        val imgLike = viewBinding.imgLike
    }

}