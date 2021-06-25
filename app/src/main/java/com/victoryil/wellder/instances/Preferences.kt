package com.victoryil.wellder.instances

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.victoryil.wellder.utils.PreferencesKeys

object Preferences {
    private lateinit var sharedPreferences: SharedPreferences
    fun loadPreferences(activity: Activity) {
        sharedPreferences =
            activity.getSharedPreferences("com.victoryil.wellder_preferences", Context.MODE_PRIVATE)
    }

    fun getTheme() = sharedPreferences.getString(PreferencesKeys.THEME.name, "classic") == "modern"
    fun getTts() = sharedPreferences.getBoolean(PreferencesKeys.TTS.name, true)
    fun getFontSize() = sharedPreferences.getInt(PreferencesKeys.FONTSIZE.name, 18)
}