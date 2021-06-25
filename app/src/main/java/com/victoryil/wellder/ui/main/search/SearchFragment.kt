package com.victoryil.wellder.ui.main.search

import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.victoryil.wellder.R
import com.victoryil.wellder.adapters.SearchAdapter
import com.victoryil.wellder.databinding.SearchFragmentBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.ui.utils.ModalBottomSheet
import com.victoryil.wellder.utils.goneUnless
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator

class SearchFragment : Fragment(R.layout.search_fragment) {

    private val binding: SearchFragmentBinding by viewBinding()
    private var modal = ModalBottomSheet().apply {
        onAddUserListen = {
            dismiss()
            FirebaseHelper.addFriendByUID(uid).apply {
                FirebaseHelper.run {
                    updatelist(filterUsers(filterNotFriends(), uid))
                }
            }
        }
        onEnterChatListen = {
            dismiss()
            NavigationHelper.navigateSearchToChats(uid)
        }

    }
    private val listadapter: SearchAdapter = SearchAdapter().apply {
        onClickListen = { uid ->
            modal.uid = uid
            modal.show(childFragmentManager, tag)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAll()
    }

    private fun setupAll() {

        binding.showInfo.setOnClickListener {
            NavigationHelper.navigateToAddFriend()
        }
        FirebaseHelper.userList.observe(viewLifecycleOwner, {
            var filter = filterUsers(it)
            filter.sortWith { o1, o2 -> compareGeoDistance(o1, o2) }
            updatelist(filter)
        })

        setupRecycler()
    }

    private fun filterUsers(list: ArrayList<User>, uid: String? = null): ArrayList<User> {
        var lista = FirebaseHelper.filterNotFriends(list)
        lista = FirebaseHelper.filterNotBlockByUID(lista)
        if (uid != null) lista.removeIf { user -> user.UID === uid }
        binding.lblEmptySearchText.goneUnless(lista.size <= 0)
        binding.imgEmptySearch.goneUnless(lista.size <= 0)
        return lista
    }

    private fun updatelist(it: ArrayList<User>) {
        listadapter.submitList(it)
    }

    private fun setupRecycler() {
        binding.lstPeople.run {
            setHasFixedSize(true)
            adapter = listadapter
            itemAnimator = SlideInLeftAnimator()
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }


    fun compareGeoDistance(u1: User, u2: User): Int {
        var loc0 = Location("")
        loc0.longitude = FirebaseHelper.getMyUser()!!.geo.longitude
        loc0.latitude = FirebaseHelper.getMyUser()!!.geo.latitude
        var loc1 = Location("")
        loc1.longitude = u1.geo.longitude
        loc1.latitude = u1.geo.latitude
        var loc2 = Location("")
        loc2.longitude = u2.geo.longitude
        loc2.latitude = u2.geo.latitude
        var d2m1 = loc0.distanceTo(loc1)
        var d2m2 = loc0.distanceTo(loc2)
        return if (d2m1 >= d2m2) 1 else -1
    }
}