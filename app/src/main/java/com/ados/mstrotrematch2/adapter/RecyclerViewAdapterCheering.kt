package com.ados.mstrotrematch2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.ListItemCheeringBinding
import com.ados.mstrotrematch2.model.BoardDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat

class RecyclerViewAdapterCheering(private val items: List<BoardDTO>, private var clickListener: OnCheeringItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterCheering.CheeringViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheeringViewHolder {
        val view = ListItemCheeringBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return CheeringViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: CheeringViewHolder, position: Int) {
        holder.initialize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {

                Glide.with(imgLike.context)
                    .asBitmap()
                    .load(R.drawable.like) ///feed in path of the image
                    .optionalFitCenter()
                    .into(imgLike)

                if (item.isBlock) {
                    title.text = "차단되었습니다."
                    content.text = "내가 신고한 글입니다."
                    title.setTextColor(Color.parseColor("#CCCCCC"))
                    content.setTextColor(Color.parseColor("#CCCCCC"))
                } else {
                    title.text = item.title
                    content.text = item.content
                }
                name.text = item.name

                if (item.time != null) {
                    time.text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.time)
                }
                likeCount.text = item.likeCount.toString()

                if (image != null && item.imageUrl.isNullOrEmpty()) {
                    var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)

                    if (imageID <= 0) {
                    } else {
                        //image?.setImageResource(imageID)
                        Glide.with(image.context)
                            .asBitmap()
                            .load(imageID) ///feed in path of the image
                            .optionalFitCenter()
                            .into(holder.image)
                    }
                } else {
                    Glide.with(holder.itemView.context).load(item.imageUrl).apply(
                        RequestOptions().centerCrop()).into(image)
                }

            }
        }
    }

    inner class CheeringViewHolder(private val viewBinding: ListItemCheeringBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var image = viewBinding.imgProfile
        val title = viewBinding.textTitle
        var content = viewBinding.textContent
        val name = viewBinding.textName
        var time = viewBinding.textTime
        val likeCount = viewBinding.textLikeCount
        val imgLike = viewBinding.imgLike

        fun initialize(item: BoardDTO, action: OnCheeringItemClickListener) {
            viewBinding.cheeringItemLayout.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }

            // 좋아요, 싫어요는 상세보기에서 하도록 수정
            /*itemView.img_like.setOnClickListener {
                action.onItemClick_like(item, itemView.text_like_count)
            }
            itemView.text_like_count.setOnClickListener {
                action.onItemClick_like(item, itemView.text_like_count)
            }
            itemView.img_dislike.setOnClickListener {
                action.onItemClick_dislike(item, itemView.text_dislike_count)
            }
            itemView.text_dislike_count.setOnClickListener {
                action.onItemClick_dislike(item, itemView.text_dislike_count)
            }*/
        }
    }

}

interface OnCheeringItemClickListener {
    fun onItemClick(item: BoardDTO, position: Int)
    fun onItemClickLike(item: BoardDTO, like: TextView)
}