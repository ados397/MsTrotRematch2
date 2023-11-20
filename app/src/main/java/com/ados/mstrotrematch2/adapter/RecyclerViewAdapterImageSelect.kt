package com.ados.mstrotrematch2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.databinding.ListItemImageSelectBinding
import com.ados.mstrotrematch2.model.RankExDTO
import com.bumptech.glide.Glide

class RecyclerViewAdapterImageSelect(private val items: List<RankExDTO>, var clickListener: OnImageSelectItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterImageSelect.ImageSelectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSelectViewHolder {
        val view = ListItemImageSelectBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return ImageSelectViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ImageSelectViewHolder, position: Int) {
        holder.initialize(items[position],clickListener)

        items[position].let { item ->
            with(holder) {

                var imageID = itemView.context.resources.getIdentifier(item.rankDTO?.image, "drawable", itemView.context.packageName)
                if (image != null && imageID > 0) {
                    Glide.with(image.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .optionalFitCenter()
                        .centerInside()
                        .into(holder.image)
                }
                name.text = "${position+1}. ${item.rankDTO?.name}"
            }
        }
    }

    inner class ImageSelectViewHolder(private val viewBinding: ListItemImageSelectBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var image = viewBinding.imgProfile
        var name = viewBinding.textName

        fun initialize(item: RankExDTO, action: OnImageSelectItemClickListener) {
            viewBinding.voteItemLayout.setOnClickListener {
                action.onItemClick(item)
            }
        }
    }
}

interface OnImageSelectItemClickListener {
    fun onItemClick(item: RankExDTO)
}

