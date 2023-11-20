package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.GetItemDialogBinding
import com.ados.mstrotrematch2.model.MailDTO
import java.text.DecimalFormat

class GetItemDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: GetItemDialogBinding

    private val layout = R.layout.get_item_dialog

    var mailDTO: MailDTO? = null
    var mailDTO2: MailDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GetItemDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setInfo()
    }

    fun setInfo() {
        if (mailDTO != null) {
            binding.layoutItem.visibility = View.VISIBLE
            when (mailDTO?.item) {
                MailDTO.Item.NONE -> {
                    binding.imgItem.visibility = View.GONE
                    binding.textItemCount.text = "${decimalFormat.format(mailDTO?.itemCount)}"
                }
                MailDTO.Item.PAID_GEM, MailDTO.Item.FREE_GEM -> {
                    binding.imgItem.visibility = View.VISIBLE
                    binding.imgItem.setImageResource(R.drawable.diamond)
                    binding.textItemCount.text = "${decimalFormat.format(mailDTO?.itemCount)}"
                }
                MailDTO.Item.TICKET -> {
                    binding.imgItem.visibility = View.VISIBLE
                    binding.imgItem.setImageResource(R.drawable.ticket2)
                    binding.textItemCount.text = "${decimalFormat.format(mailDTO?.itemCount)}"
                }
                else -> binding.imgItem.visibility = View.GONE
            }
        } else {
            binding.layoutItem.visibility = View.GONE
        }

        if (mailDTO2 != null) {
            binding.layoutItem2.visibility = View.VISIBLE
            when (mailDTO2?.item) {
                MailDTO.Item.NONE -> {
                    binding.imgItem2.visibility = View.GONE
                    binding.textItemCount2.text = "${decimalFormat.format(mailDTO2?.itemCount)}"
                }
                MailDTO.Item.PAID_GEM, MailDTO.Item.FREE_GEM -> {
                    binding.imgItem2.visibility = View.VISIBLE
                    binding.imgItem2.setImageResource(R.drawable.diamond)
                    binding.textItemCount2.text = "${decimalFormat.format(mailDTO2?.itemCount)}"
                }
                MailDTO.Item.TICKET -> {
                    binding.imgItem2.visibility = View.VISIBLE
                    binding.imgItem2.setImageResource(R.drawable.ticket2)
                    binding.textItemCount2.text = "${decimalFormat.format(mailDTO2?.itemCount)}"
                }
                else -> binding.imgItem2.visibility = View.GONE
            }
        } else {
            binding.layoutItem2.visibility = View.GONE
        }
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }
}