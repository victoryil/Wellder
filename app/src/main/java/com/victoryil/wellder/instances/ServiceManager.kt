package com.victoryil.wellder.instances

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationRequest

object ServiceManager {
    private lateinit var notificationManager: NotificationManager
    fun loadServices(activity: Activity) {
        notificationManager = initializeNotificationManager(activity)
    }

    fun startService(activity: Activity, _class: Any) {
        activity.startService(Intent(activity, _class::class.java))
    }

    fun getSystemService(activity: Activity, service: String): Any = activity.getSystemService(service)

    // Initialize services
    private fun initializeNotificationManager(activity: Activity) =
        getSystemService(activity, Context.NOTIFICATION_SERVICE) as NotificationManager

    // Get Services Manager
    fun getNotificationManager() = notificationManager
}