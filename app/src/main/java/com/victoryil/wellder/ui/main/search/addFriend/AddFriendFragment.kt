package com.victoryil.wellder.ui.main.search.addFriend

import android.Manifest
import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.AddFriendFragmentBinding
import com.victoryil.wellder.instances.*


class AddFriendFragment : Fragment(R.layout.add_friend_fragment) {
    private val binding: AddFriendFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseHelper.getMyFriends()
        setupAll()
    }

    private fun setupAll() {
        requirePermission()
        binding.addTag.text = "#${MyUser.getMyUser()?.tag}"
        val qrgEncoder = QRGEncoder(MyUser.getMyUser()?.tag, QRGContents.Type.TEXT, 350)
        qrgEncoder.bitmap
        binding.qr.setImageBitmap(qrgEncoder.bitmap)
        binding.camera.setOnClickListener {
            if (PermissionsManager.getManager().checkPermissions()) NavigationHelper.navigateToQR()
        }
    }

    private fun requirePermission() {
        val list = listOf(
            Manifest.permission.CAMERA
        )
        PermissionsManager.setManager(requireActivity(), list)
    }
}