package com.victoryil.wellder.ui.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.victoryil.wellder.R
import com.victoryil.wellder.utils.debug

open class ModalBottomSheet : BottomSheetDialogFragment() {
    var uid: String = ""
    var onAddUserListen: (() -> Unit)? = null
    var onEnterChatListen: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflate = inflater.inflate(R.layout.bottom_sheet, container, false)
        val root1 = inflate.findViewById<MaterialCardView>(R.id.btnSheetAdd)
        val root2 = inflate.findViewById<MaterialCardView>(R.id.btnSheetSend)

        root1.setOnClickListener { onAddUserListen?.invoke() }
        root2.setOnClickListener { onEnterChatListen?.invoke() }

        return inflate
    }
}