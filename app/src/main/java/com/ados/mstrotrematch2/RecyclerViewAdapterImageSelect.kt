package com.ados.mstrotrematch2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.mstrotrematch2.model.RankDTO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_rank.view.*

class RecyclerViewAdapterImageSelect(val items: List<RankDTO>, var clickListener: OnImageSelectItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterImageSelect.ImageSelectViewHolder>() {

    /*var images = arrayListOf<String>(
        "profile01",
        "profile02",
        "profile03",
        "profile04",
        "profile05",
        "profile06",
        "profile07",
        "profile08",
        "profile09",
        "profile10",
        "profile11",
        "profile12",
        "profile13",
        "profile14",
        "profile15",
        "profile16",
        "profile17",
        "profile18",
        "profile19",
        "profile20",
        "profile21",
        "profile22",
        "profile23",
        "profile24",
        "profile25",
        "profile26",
        "profile27",
        "profile28",
        "profile29",
        "profile30",
        "profile31",
        "profile32",
        "profile33",
        "profile34",
        "profile35",
        "profile36",
        "profile37",
        "profile38",
        "profile39",
        "profile40",
        "profile41",
        "profile42",
        "profile43",
        "profile44",
        "profile45",
        "profile46",
        "profile47"
        )

    var names = arrayListOf<String>(
        "강태관",
        "강화",
        "고재근",
        "구자명",
        "김경민",
        "김수찬",
        "김인석",
        "김재혁",
        "김중연",
        "김태수",
        "김호중",
        "김희재",
        "나무",
        "나태주",
        "남승민",
        "노지훈",
        "류지광",
        "미스터붐박스",
        "박경래",
        "삼식이",
        "신성",
        "신인선",
        "안성훈",
        "양지원",
        "영기",
        "영탁",
        "오샘",
        "옥진욱",
        "유호",
        "이대원",
        "이도진",
        "이재식",
        "이찬원",
        "임도형",
        "임영웅",
        "장민호",
        "정동원",
        "정호",
        "천명훈",
        "최대성",
        "최윤히",
        "최정훈",
        "추혁진",
        "한강",
        "허민영",
        "홍잠언",
        "황윤성"
    )*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageSelectViewHolder(parent)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapterImageSelect.ImageSelectViewHolder, position: Int) {
        //view에 onClickListner를 달고, 그 안에서 직접 만든 itemClickListener를 연결시킨다
        //holder.initalize(images.get(position),clickListener)
        holder.initalize(items.get(position),clickListener)

        items[position].let { item ->
        //images[position].let { item ->
        //images.getResourceId(position, -1).let {item ->
            println("position : $position, ${item.toString()}")

            with(holder) {
                //name.text = "${position+1}. ${item.name}"
                //count.text = "득표수:${decimalFormat.format(item.count)}"

                var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)

                //println("이미지이름 : ${itemView.context.resources.getText(item)}")
                if (image != null && imageID > 0) {
                    //image?.setImageResource(imageID)

                    Glide.with(image.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .fitCenter()
                        .into(holder.image)
                }
                //name.text = names[position]
                name.text = item.name
            }
        }
    }

    inner class ImageSelectViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_image_select, parent, false)) {
        var image = itemView.img_profile
        var name = itemView.text_name

        fun initalize(item: RankDTO, action:OnImageSelectItemClickListener) {
            itemView.setOnClickListener {
                action.onItemClick(item)
            }
        }
    }
}

interface OnImageSelectItemClickListener {
    fun onItemClick(item: RankDTO)
}

