package com.victoryil.wellder.utils
import androidx.fragment.app.Fragment

interface FragmentNameInterface {
    fun getFName(): String
}

fun Fragment.getName(): String = if (this is FragmentNameInterface) this.getFName() else "undefined"
