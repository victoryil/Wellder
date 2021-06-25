package com.victoryil.wellder.ui.main.friends

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.victoryil.wellder.R
import com.victoryil.wellder.adapters.FriendAdapter
import com.victoryil.wellder.databinding.FriendsFragmentBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.utils.FragmentNameInterface
import com.victoryil.wellder.utils.goneUnless
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import java.util.*

class FriendsFragment : Fragment(R.layout.friends_fragment), FragmentNameInterface {
    override fun getFName(): String = "Amistades"

    private val binding: FriendsFragmentBinding by viewBinding()
    private lateinit var listAdapter: FriendAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAll()
    }

    private fun setupAll() {
        listAdapter = FriendAdapter(requireContext()).apply {
            onClickListen = { NavigationHelper.navigateToChats(it) }
        }
        FirebaseHelper.friendList.observe(viewLifecycleOwner, {
            binding.lblEmptyFriendText.goneUnless(it.size<=0)
            binding.imgEmptyFriends.goneUnless(it.size<=0)
            updateList(it)
        })

        setupRecycler()
    }

    private fun updateList(it: ArrayList<User>) = listAdapter.submitList(it)

    private fun setupRecycler() = binding.lstPeople.run {
        setHasFixedSize(true)
        adapter = listAdapter
        itemAnimator = SlideInLeftAnimator()
        layoutManager = LinearLayoutManager(context)
    }
}