package com.victoryil.wellder.instances

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.victoryil.wellder.R
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.utils.RSSPullService

object AppManager {
    private var alreadyInit = false
    private val hideDestination = arrayListOf(
        R.id.QRScannerDestination,
        R.id.registerDestination,
        R.id.verifyOTPDestination,
        R.id.sendOTPDestination
    )

    private val exitDestination = arrayListOf(
        R.id.mainFragmentDestination,
        R.id.registerDestination,
        R.id.verifyOTPDestination,
        R.id.sendOTPDestination
    )
    private val CODES: HashMap<String, Int> = hashMapOf(
        Pair("RECORD_REQUEST_CODE", 101),
        Pair("PermissionsRequestCode", 123),
        Pair("CHANNEL_ID", 123),
    )

    fun runApp(user: User?, activity: Activity) {
        if (alreadyInit) {
            return
        }
        if (user != null) { //Si existe setea el usario, arranca el serivicio de notificaciones y navega a la pantalla inicial
            MyUser.setMyUser(user)
            ServiceManager.startService(activity, RSSPullService())
            NavigationHelper.navigateToMain()
            alreadyInit = true
            FirebaseHelper.getMyFriends()
            FirebaseHelper.getBlocked()
        } else NavigationHelper.navigateToRegister()
    }

    fun checkHide(): Boolean = hideDestination.contains(NavigationHelper.getCurrentDestination()?.id)
    fun checkReturn(): Boolean = exitDestination.contains(NavigationHelper.getCurrentDestination()?.id)

    fun getCodes(code: String): Int = CODES[code]!!

}