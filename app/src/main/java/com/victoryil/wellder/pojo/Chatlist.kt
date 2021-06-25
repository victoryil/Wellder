package com.victoryil.wellder.pojo

import com.google.firebase.Timestamp

data class Chatlist(
    val uuid: String,
    val uid1: String,
    val uid2: String,
    val time: Timestamp,
    val uid1Open: Boolean,
    val uid2Open: Boolean
)
