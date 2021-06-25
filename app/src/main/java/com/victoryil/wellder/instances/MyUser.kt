package com.victoryil.wellder.instances

import com.victoryil.wellder.pojo.User

//Usa patron singelton
object MyUser {
    private var user: User? = null

    fun setMyUser(user: User) {
        this.user = user
    }
    fun getMyUser() = user
    fun resetUser() {
        user = null
    }
}