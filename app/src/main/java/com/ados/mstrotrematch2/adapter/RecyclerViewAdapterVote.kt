package com.ados.mstrotrematch2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.databinding.ListItemVoteBinding
import com.ados.mstrotrematch2.model.RankDTO
import com.ados.mstrotrematch2.model.RankExDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat
import java.text.NumberFormat

//class RecyclerViewAdapterVote(val items: List<RankDTO>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
class RecyclerViewAdapterVote(private val items: List<RankExDTO>, var clickListener: OnVoteItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterVote.VoteViewHolder>() {

    init {
        setHasStableIds(true)
    }

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private fun formatNumber(value: Int): String {
        return when {
            value >= 1E3 -> "${decimalFormat.format((value.toFloat() / 1E3).toInt())}K"
            else -> NumberFormat.getInstance().format(value)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        val view = ListItemVoteBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return VoteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        //view에 onClickListner를 달고, 그 안에서 직접 만든 itemClickListener를 연결시킨다
        holder.initialize(items[position],clickListener)

        items[position].let { item ->
            with(holder) {
                name.text = "${position+1}. ${item.rankDTO?.name}"
                //count.text = "득표수:${formatNumber(item.count!!)}"
                count.text = "${decimalFormat.format(item.rankDTO?.count!!)}"

                var imageID = itemView.context.resources.getIdentifier(item.rankDTO?.image, "drawable", itemView.context.packageName)
                if (image != null && imageID > 0) {
                    //image?.setImageResource(imageID)


                    Glide.with(image.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .optionalFitCenter()
                        .centerInside()
                        .into(holder.image)
                }
            }
        }
    }

    inner class VoteViewHolder(private val viewBinding: ListItemVoteBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var image = viewBinding.imgProfile
        val name = viewBinding.textName
        val count = viewBinding.textCount

//        var imageID = context.resources.getIdentifier(colorList.photo, "drawable", context.packageName)

        fun initialize(item: RankExDTO, action: OnVoteItemClickListener) {
            viewBinding.voteItemLayout.setOnClickListener {
                 action.onItemClick(item, adapterPosition)
            }
        }


    }



    /*override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var width = parent.resources.displayMetrics.widthPixels / 3

        var imageview = ImageView(parent.context)
        imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
        return VoteViewHolder(imageview)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var imageview = (holder as VoteViewHolder).imageview

        var imageID = imageview.context.resources.getIdentifier(items[position].image, "drawable", imageview.context.packageName)
        imageview?.setImageResource(imageID)

    }

    inner class VoteViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

    }*/



}

interface OnVoteItemClickListener {
    fun onItemClick(item: RankExDTO, position: Int)
}

