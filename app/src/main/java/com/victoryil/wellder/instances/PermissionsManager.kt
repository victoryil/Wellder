package com.victoryil.wellder.instances

import android.app.Activity
import com.victoryil.wellder.utils.ManagePermissions

object PermissionsManager {
    private lateinit var managePermissions: ManagePermissions
    fun setManager(activity: Activity, list: List<String>) {
        managePermissions = ManagePermissions(activity,list,AppManager.getCodes("PermissionsRequestCode"))
    }
    fun getManager() = managePermissions
}