package com.victoryil.wellder.pojo

import com.google.firebase.firestore.GeoPoint

data class User(
    var UID: String,
    var tag: String,
    var name: String,
    var phone: String,
    var profileImg: String,
    var isOnline: Boolean,
    var geo: GeoPoint
)