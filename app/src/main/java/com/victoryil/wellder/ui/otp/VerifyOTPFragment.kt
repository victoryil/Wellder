package com.victoryil.wellder.ui.otp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.VerifyOtpFragmentBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.MyUser
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.utils.hideKeyboard
import java.util.concurrent.TimeUnit

class VerifyOTPFragment : Fragment(R.layout.verify_otp_fragment) {

    private val binding: VerifyOtpFragmentBinding by viewBinding()

    val mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
        }

        override fun onVerificationFailed(p0: FirebaseException) {
        }

        override fun onCodeSent(
            verficationId: String,
            p1: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verficationId, p1)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setups()
    }

    private val args: VerifyOTPFragmentArgs by navArgs()

    private fun setups() {
        setHasOptionsMenu(true)
        listeners()
    }

    private fun listeners() {
        codes()
        binding.lblResendClickable.setOnClickListener {
            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(FirebaseHelper.phone)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(mCallback)
                .setForceResendingToken(FirebaseHelper.token)
                .setActivity(requireActivity())// OnVerificationStateChangedCallbacks
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
        binding.btnSend.setOnClickListener { verifyCode() }
    }

    private fun codes() {
        binding.edPN1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotBlank()) binding.edPN2.requestFocus()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.edPN2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotBlank()) binding.edPN3.requestFocus()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.edPN3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotBlank()) binding.edPN4.requestFocus()
            }

            override fun afterTextChanged(s: Editable?) {}

        })
        binding.edPN4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotBlank()) binding.edPN5.requestFocus()
            }

            override fun afterTextChanged(s: Editable?) {}

        })
        binding.edPN5.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotBlank()) binding.edPN6.requestFocus()
            }

            override fun afterTextChanged(s: Editable?) {}

        })
        binding.edPN6.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotBlank()) {
                    binding.edPN6.hideKeyboard()
                    binding.root.clearFocus()
                    verifyCode()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun verifyCode() {
        var code = ""
        code += binding.edPN1.text
        code += binding.edPN2.text
        code += binding.edPN3.text
        code += binding.edPN4.text
        code += binding.edPN5.text
        code += binding.edPN6.text
        if (code.length == 6) {

            val phoneCredential: PhoneAuthCredential =
                PhoneAuthProvider.getCredential(args.verficationId, code)
            val currentUser = FirebaseAuth.getInstance()

            currentUser.signInWithCredential(phoneCredential)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        FirebaseHelper.firebaseInitMyUser()
                        if (MyUser.getMyUser() != null) {
                            NavigationHelper.navigateToMain()
                        } else NavigationHelper.navigateToRegister()
                    }
                }
        }
    }
}