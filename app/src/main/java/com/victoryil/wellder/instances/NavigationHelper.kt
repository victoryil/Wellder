package com.victoryil.wellder.instances

import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.google.firebase.auth.PhoneAuthProvider
import com.victoryil.wellder.R
import com.victoryil.wellder.ui.chat.ChatFragmentDirections
import com.victoryil.wellder.ui.main.MainFragmentDirections
import com.victoryil.wellder.ui.main.search.SearchFragmentDirections
import com.victoryil.wellder.ui.otp.SendOTPFragmentDirections

object NavigationHelper {
    private lateinit var navController: NavController
    private var optionMenu: MenuItem? = null
    private var currentChatPersonUID = ""

    //Init
    fun setNavigationController(navigation: NavController) {
        navController = navigation
    }

    //Getters
    fun getCurrentDestination() = navController.currentDestination
    fun getCurrentChatPersonUID(): String = currentChatPersonUID

    //Setters
    fun setChatPersonUID(uid: String) {
        currentChatPersonUID = uid
    }

    fun setOptionItem(menuItem: MenuItem?) {
        optionMenu = menuItem
    }

    fun setVisibilityOptionItem(visible: Boolean) {
        if (optionMenu != null) optionMenu!!.isVisible = visible
    }

    //Navigate Mode
    private fun navigator(destination: Int) = navController.navigate(destination)
    private fun navigator(destination: NavDirections) = navController.navigate(destination)

    //Navigations
    fun navigateToMain() = navigator(R.id.mainFragmentDestination)
    fun navigateToSendOTP() = navigator(R.id.sendOTPDestination)
    fun navigateToRegister() = navigator(R.id.registerDestination)
    fun navigateToSearch() = navigator(R.id.searchDestination)
    fun navigateToSettings() = navigator(R.id.settingsFragDestination)
    fun navigateToProfileSettings() = navigator(R.id.profileSettingsDestination)
    fun navigateToAccessibilitySettings() = navigator(R.id.accessibilitySettingDestination)
    fun navigateToAddFriend() = navigator(R.id.addFriendDestination)
    fun navigateToBlockUsers() = navigator(R.id.blockUserDestination)
    fun navigateToQR() = navigator(R.id.QRScannerDestination)

    //Navigation with ARGS
    fun navigateToChats(uid: String) =
        navigator(MainFragmentDirections.actionMain2chat(uid))

    fun navigateSearchToChats(uid: String) =
        navigator(SearchFragmentDirections.search2Chat(uid))

    fun navigateToVerify(verificationId: String) =
        navigator(SendOTPFragmentDirections.actionSendToVerify(verificationId))

    fun navigateToImage(url: String, name: String) =
        navigator(
            ChatFragmentDirections.actionChat2ImageViewer()
                .setUrl(url).setName(name)
        )

    fun goBack() {
        navController.navigateUp()
    }


}