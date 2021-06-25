package com.victoryil.wellder.ui.otp

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.SendOtpFragmentBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.utils.hideKeyboard
import java.util.concurrent.TimeUnit

class SendOTPFragment : Fragment(R.layout.send_otp_fragment) {
    private val binding: SendOtpFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setups()
    }

    private fun setups() {
        listeners()
    }

    private fun listeners() {
        binding.btnSend.setOnClickListener {
            binding.btnSend.hideKeyboard()

            if (binding.edPhoneNumber.text.isNotBlank()) {
                binding.btnSend.startAnimation()
                val mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        Toast.makeText(context, p0.message, Toast.LENGTH_SHORT).show()
                        binding.btnSend.revertAnimation {
                            binding.btnSend.text = "Verify"
                        }
                    }

                    override fun onCodeSent(
                        verficationId: String,
                        p1: PhoneAuthProvider.ForceResendingToken
                    ) {
                        FirebaseHelper.token = p1
                        NavigationHelper.navigateToVerify(verficationId)
                        super.onCodeSent(verficationId, p1)
                    }

                }


                val phone = "+${binding.ccp.selectedCountryCode} ${binding.edPhoneNumber.text}"
                FirebaseHelper.phone = phone
                val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber(phone)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setCallbacks(mCallback)
                    .setActivity(requireActivity())// OnVerificationStateChangedCallbacks
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)

            }
        }
    }
}