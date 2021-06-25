package com.victoryil.wellder.utils

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.victoryil.wellder.MainActivity
import com.victoryil.wellder.R
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.ServiceManager
import com.victoryil.wellder.pojo.Message
import io.karn.notify.Notify
import java.util.*
import kotlin.collections.ArrayList

class RSSPullService : Service() {
    private var ids: ArrayList<Int> = arrayListOf()
    private var message: ArrayList<Message> = arrayListOf()
    private var messages: ArrayList<Message> = arrayListOf()
    private lateinit var noti: NotificationManager

    override fun onCreate() {
        noti = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        super.onCreate()
        if (FirebaseAuth.getInstance().currentUser == null) return
        if (FirebaseAuth.getInstance().currentUser.uid.isNotBlank()) {
            val user = FirebaseAuth.getInstance().currentUser
            Firebase.firestore.collection("messages")
                .whereEqualTo("receiver", user.uid)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (value != null && !value.isEmpty) {
                        message.clear()
                        for (dc in value.documentChanges) {
                            when (dc.type) {
                                ADDED -> {
                                    val m = FirebaseHelper.instantiateMessage(dc.document)
                                    if (!m.isSeen) {

                                        message.add(m)
                                    }
                                }
                                MODIFIED -> {
                                    val message = FirebaseHelper.instantiateMessage(dc.document)
                                    if (message.isSeen) {
                                        messages.removeIf { t -> t.uid == message.uid }
                                    }
                                }
                                REMOVED -> messages.removeIf { t ->
                                    t.uid == FirebaseHelper.instantiateMessage(
                                        dc.document
                                    ).uid
                                }
                            }

                        }
                        notifyMe()
                    }
                }
        }
    }

    private fun notifyMe() {
        val arr = messages
        val arrAux = arrayListOf<
                NotificationCompat.MessagingStyle.Message>()
        for (m in message) {
            arr.add(m)
        }
        messages = arr
        for (m in messages) {
            val sender = FirebaseHelper.findUserByUID(m.senderUID)
            val icon =
                IconCompat.createWithContentUri(sender?.profileImg ?: "")
            val notificationCompat = NotificationCompat
                .MessagingStyle
                .Message(
                    m.message,
                    m.sendTime.seconds,
                    androidx.core.app.Person.Builder()
                        .setKey(sender?.UID ?: "unknown")
                        .setName(sender?.name ?: "unknown")
                        .setIcon(icon)
                        .build()
                )
            arrAux.add(notificationCompat)
        }
        noty(arrAux)
    }

    private fun noty(message: ArrayList<NotificationCompat.MessagingStyle.Message>) {
        ServiceManager.getNotificationManager().cancelAll()
        if (message.isEmpty()) return
        Notify.with(this).asMessage {
            messages = message
        }
            .header {
                icon = R.drawable.icon
            }
            .meta {
                clickIntent = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(
                        this@RSSPullService,
                        MainActivity::class.java
                    ),
                    0
                )

            }
            .show()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        for (id in ids) {
            Notify.cancelNotification(applicationContext, id)
        }
        return super.onUnbind(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
