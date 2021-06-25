package com.victoryil.wellder.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.victoryil.wellder.R

class AccessibilitySetting : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.accesibility, rootKey)
    }
}