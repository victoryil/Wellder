package com.victoryil.wellder.ui.main.chats

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.victoryil.wellder.R
import com.victoryil.wellder.adapters.UserAdapter
import com.victoryil.wellder.databinding.ChatsFragmentBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.utils.FragmentNameInterface
import com.victoryil.wellder.utils.debug
import com.victoryil.wellder.utils.goneUnless
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator

class ChatsFragment : Fragment(R.layout.chats_fragment), FragmentNameInterface {
    private val binding: ChatsFragmentBinding by viewBinding()

    private lateinit var listadapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupAll()
    }

    private fun setupAll() {
        listadapter = UserAdapter(requireContext()).apply {
            onClickListen = { NavigationHelper.navigateToChats(it) }
            onLongClickListen =
                { uid ->
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("¿Deseas eliminar este chat?")
                        .setNegativeButton("Cancel") { _, _ ->
                        }
                        .setPositiveButton("Eliminar") { _,_ ->
                            FirebaseHelper.deleteChat(uid)
                        }
                        .show()
                }
        }
        setViewmodel()
    }

    private fun setViewmodel() {
        FirebaseHelper.chatList.observe(viewLifecycleOwner, {
            it.sortByDescending { chatlist -> chatlist.time }
            val lista = FirebaseHelper.filterMyChats(it)
            binding.lblEmptyChatsText.goneUnless(lista.size <= 0)
            binding.imgEmptyChats.goneUnless(lista.size <= 0)
            updateList(lista)
        })
        FirebaseHelper.userList.observe(viewLifecycleOwner, {
            val lista = FirebaseHelper.chatList.value?.let { chats -> FirebaseHelper.filterMyChats(chats) } ?: arrayListOf()
            binding.lblEmptyChatsText.goneUnless(lista.size <= 0)
            binding.imgEmptyChats.goneUnless(lista.size <= 0)
            updateList(lista)
        })

        setupRecycler()
    }

    private fun setupRecycler() {
        binding.lstChat.run {
            setHasFixedSize(true)
            adapter = listadapter
            itemAnimator = SlideInLeftAnimator()
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun updateList(users: ArrayList<User>) {
        listadapter.submitList(users)
    }

    override fun getFName(): String = "Mensajes"
}

