package com.victoryil.wellder.ui.main.search.addFriend

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.QRScannerFragmentBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.utils.ErrorCodes
import www.sanju.motiontoast.MotionToast


class QRScannerFragment : Fragment(R.layout.q_r_scanner_fragment) {
    private val binding: QRScannerFragmentBinding by viewBinding()
    private lateinit var codeScanner: CodeScanner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAll()
    }

    private fun setupAll() {
        codeScanner = CodeScanner(requireActivity(), binding.scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                val user = FirebaseHelper.findUsersByTAG(it.text)
                if (user != null) {
                    binding.banner.contentText =
                        "vas a añadir a ${user.name}#${user.tag} ¿estas seguro?"
                    binding.banner.visibility = View.VISIBLE
                    binding.banner.setRightButtonAction {
                        val result = FirebaseHelper.addFriendByUID(user.UID)
                        if (result != ErrorCodes.SUCCESS) MotionToast.darkToast(
                            requireActivity(),
                            result.errorTitle(result),
                            result.errorDescription(result),
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            Typeface.SANS_SERIF
                        ) else {
                            MotionToast.darkToast(
                                requireActivity(),
                                user.name,
                                "Se agrego correctamente",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                Typeface.SANS_SERIF
                            )
                            NavigationHelper.goBack()
                        }
                    }
                    binding.banner.setLeftButtonAction { binding.banner.visibility = View.GONE }
                } else {
                    MotionToast.darkToast(
                        requireActivity(),
                        "User not found",
                        getString(R.string.error_qr),
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        Typeface.SANS_SERIF
                    )
                }

            }
        }
        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

}