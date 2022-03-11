package com.ados.mstrotrematch2.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_vote.view.*
import java.text.DecimalFormat

//class RecyclerViewAdapterVote(val items: List<RankDTO>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
class RecyclerViewAdapterVote(val items: List<RankDTO>, var clickListener: OnVoteItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterVote.VoteViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VoteViewHolder(parent)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapterVote.VoteViewHolder, position: Int) {
        //view에 onClickListner를 달고, 그 안에서 직접 만든 itemClickListener를 연결시킨다
        holder.initalize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                name.text = "${position+1}. ${item.name}"
                count.text = "득표수:${decimalFormat.format(item.count)}"

                var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)
                if (image != null && imageID > 0) {
                    //image?.setImageResource(imageID)


                    Glide.with(image.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .fitCenter()
                        .into(holder.image)
                }
            }
        }
    }

    inner class VoteViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_vote, parent, false)) {
        var image = itemView.img_profile
        val name = itemView.text_name
        val count = itemView.text_count

//        var imageID = context.resources.getIdentifier(colorList.photo, "drawable", context.packageName)

        fun initalize(item: RankDTO, action:OnVoteItemClickListener) {
            itemView.setOnClickListener {
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
    fun onItemClick(item: RankDTO, position: Int)
}

