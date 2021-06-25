package com.victoryil.wellder.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.victoryil.wellder.R
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.MyUser
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.utils.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SettingsFragment : PreferenceFragmentCompat() {
    var profile: Preference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        setup()
    }

    private fun setup() {
        profile = findPreference(getString(R.string.profileSettings))
        val accessibilitu: Preference? = findPreference(getString(R.string.accesibilityBtn))
        val blockusers: Preference? = findPreference("BLOCKPEOPLE")
        val logout: Preference? = findPreference("LOGOUT")
        if (profile != null) {
            setIcon()
            setName()
            profile!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    NavigationHelper.navigateToProfileSettings()
                    return@OnPreferenceClickListener true
                }
        }
        if (accessibilitu != null) {
            accessibilitu.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    NavigationHelper.navigateToAccessibilitySettings()
                    return@OnPreferenceClickListener true
                }
        }
        if (blockusers != null) {
            blockusers.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    NavigationHelper.navigateToBlockUsers()
                    return@OnPreferenceClickListener true
                }
        }
        if (logout != null) {
            logout.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Bye bye....")
                        .setMessage(getString(R.string.LOGOUT_MESSAGE))
                        .setNegativeButton(getString(R.string.Cancel)) { _, _ ->
                        }
                        .setPositiveButton(getString(R.string.TITLE_LOGOUT)) { _,_ ->
                            FirebaseHelper.logout()
                        }
                        .show()
                    return@OnPreferenceClickListener true
                }
        }
    }

    private fun setName() {
        profile?.title = FirebaseHelper.getMyUser()?.name
    }

    private fun setIcon() {
        if (context == null){
            return
        }
        if (MyUser.getMyUser()!!.profileImg.isNotBlank()) {
            val imageLoader = ImageLoader(requireContext())
            val request = ImageRequest.Builder(requireContext())
                .data(MyUser.getMyUser()?.profileImg)
                .target { drawable ->
                    //TODO: ROUNDED
                    profile?.icon = drawable
                }
                .build()
            imageLoader.enqueue(request)
        } else {
            profile?.setIcon(R.drawable.ic_user)
        }
    }

    override fun onResume() {
        FirebaseHelper.updateProfile()
        CoroutineScope(Dispatchers.Main).launch {
            delay(300)
            setName()
            setIcon()
        }
        super.onResume()
    }

}