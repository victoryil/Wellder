package com.victoryil.wellder.pojo

import com.google.firebase.Timestamp

data class Message(
    val uid: String,
    val message: String,
    val isPhoto: Boolean,
    val url: String,
    val isSeen: Boolean,
    val senderUID: String,
    val receiverUID: String,
    val sendTime: Timestamp
)
