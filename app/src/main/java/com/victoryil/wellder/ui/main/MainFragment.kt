package com.victoryil.wellder.ui.main

import android.os.Bundle
import android.view.*
import android.viewbinding.library.fragment.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.victoryil.wellder.R
import com.victoryil.wellder.adapters.ViewPager2Adapter
import com.victoryil.wellder.databinding.MainFragmentBinding
import com.victoryil.wellder.databinding.ThemeModernBinding
import com.victoryil.wellder.instances.*
import com.victoryil.wellder.ui.main.chats.ChatsFragment
import com.victoryil.wellder.ui.main.friends.FriendsFragment
import com.victoryil.wellder.utils.getName


class MainFragment : Fragment() {
    private val fragmentList: ArrayList<Fragment> =
        arrayListOf(ChatsFragment(), FriendsFragment())

    private val bindingClassic: MainFragmentBinding by viewBinding()
    private val bindingModern: ThemeModernBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        if (Preferences.getTheme()) {
            inflater.inflate(R.layout.theme_modern, container, false)
        } else {
            inflater.inflate(R.layout.main_fragment, container, false)

        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setups()
    }


    private fun setups() {
        setupViewPager()
        if (Preferences.getTheme()) {
            bindingModern.fab.setOnClickListener {
                if (bindingModern.viewPager.currentItem == 0) {
                    bindingModern.viewPager.setCurrentItem(1, true)
                    bindingModern.fab.setImageResource(R.drawable.ic_chat)
                } else {
                    bindingModern.viewPager.setCurrentItem(0, true)
                    bindingModern.fab.setImageResource(R.drawable.ic_people)
                }
            }
            bindingModern.bottomAppBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.settings -> NavigationHelper.navigateToSettings()
                }
                return@setOnMenuItemClickListener true
            }
            bindingModern.bottomAppBar.setNavigationOnClickListener {
                NavigationHelper.navigateToSearch()
            }
        } else {
            bindingClassic.chatsClassicFab.setOnClickListener { NavigationHelper.navigateToSearch() }
            TabLayoutMediator(bindingClassic.tabLayout, bindingClassic.viewPager) { tab, position ->
                tab.text = fragmentList[position].getName()
            }.attach()
        }
    }

    private fun setupViewPager() {
        if (!Preferences.getTheme()) {
            bindingClassic.viewPager.adapter =
                ViewPager2Adapter(requireActivity() as AppCompatActivity, fragmentList)
        } else {
            bindingModern.viewPager.adapter =
                ViewPager2Adapter(requireActivity() as AppCompatActivity, fragmentList)
            bindingModern.viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position == 0) {
                        bindingModern.fab.setImageResource(R.drawable.ic_people)
                    } else {
                        bindingModern.fab.setImageResource(R.drawable.ic_chat)
                    }
                    super.onPageSelected(position)
                }
            })
        }
    }

    override fun onResume() {
        NavigationHelper.setVisibilityOptionItem(true)
        super.onResume()
    }

}