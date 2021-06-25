package com.victoryil.wellder.ui.settings

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.victoryil.wellder.R
import com.victoryil.wellder.adapters.BlockAdapter
import com.victoryil.wellder.databinding.BlockUserSettingsBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.utils.debug
import com.victoryil.wellder.utils.goneUnless
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import java.util.*

class BlockUserSettings : Fragment(R.layout.block_user_settings) {
    private val binding: BlockUserSettingsBinding by viewBinding()
    private val listAdapter: BlockAdapter = BlockAdapter().apply {
        onClickListen = { showModal(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAll()
    }

    private fun setupAll() {
        FirebaseHelper.blockList.observe(viewLifecycleOwner, {
            binding.lblEmptyBlockText.goneUnless(it.size<=0)
            binding.imgEmptyBlock.goneUnless(it.size<=0)
            updateList(it)
        })

        setupRecycler()
    }

    private fun showModal(uid: String) {
        val user = FirebaseHelper.findUserByUID(uid)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${user?.name}#${user?.tag}")
            .setMessage("Are u sure to unblock this user? you can receive message to it")
            .setNegativeButton("Cancel") { _, _ ->
            }
            .setPositiveButton("Desbloquear") { _,_ ->
                FirebaseHelper.unblockByUID(uid)
            }
            .show()
    }

    private fun setupRecycler() = binding.lstPeople.run {
        setHasFixedSize(true)
        adapter = listAdapter
        itemAnimator = SlideInLeftAnimator()
        layoutManager = LinearLayoutManager(context)
    }

    private fun updateList(list: ArrayList<User>) = listAdapter.submitList(list)


}