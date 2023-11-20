package com.ados.mstrotrematch2.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.ados.mstrotrematch2.R
import com.ados.mstrotrematch2.databinding.WebViewDialogBinding


class WebViewDialog(context: Context, var url: String) : Dialog(context) {

    lateinit var binding: WebViewDialogBinding
    private val layout = R.layout.document_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WebViewDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
        }

        binding.webView.loadUrl(url)


        binding.buttonDocumentCancel.setOnClickListener {
            dismiss()
        }
    }
}