package com.victoryil.wellder.ui.settings

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.ProfileSettingsBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.MyUser
import com.victoryil.wellder.instances.NavigationHelper

class ProfileSettings : Fragment(R.layout.profile_settings) {
    private val binding: ProfileSettingsBinding by viewBinding()
    private var imageURI: Uri? = null
    private var storageRef: StorageReference? = Firebase.storage.reference.child("users/profile")
    private var user: HashMap<String, Any> = HashMap()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        val qrgEncoder = QRGEncoder(MyUser.getMyUser()?.tag, QRGContents.Type.TEXT, 350)
        qrgEncoder.bitmap
        binding.qrStetting.setImageBitmap(qrgEncoder.bitmap)
        binding.outlinedTextField.editText?.setText(MyUser.getMyUser()?.name)
        if (MyUser.getMyUser()!!.profileImg.isNotBlank()) {
            Picasso.get().load(MyUser.getMyUser()?.profileImg).placeholder(R.drawable.ic_user)
                .into(binding.profileImg)
        } else {
            binding.profileImg.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_user
                )
            )
        }

        binding.fabEditPhoto.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    256,
                    256
                )
                .start { resultCode, data ->
                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            //Image Uri will not be null for RESULT_OK
                            imageURI = data?.data
                            binding.profileImg.setImageURI(imageURI)
                            Picasso.get().load(imageURI)
                                .placeholder(R.drawable.ic_user)
                                .into(binding.profileImg)
                            //You can get File object from intent
                            ImagePicker.getFile(data)

                            //You can also get File Path from intent
                            ImagePicker.getFilePath(data)
                        }
                        ImagePicker.RESULT_ERROR -> {
                            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> {
                            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

        }
        binding.btnSaveProfileSettings.setOnClickListener {
            registerData()
        }
    }

    private fun registerData() {
        val name = binding.outlinedTextField.editText?.text.toString()
        if (name.isNotBlank() && name != MyUser.getMyUser()?.name || imageURI != null) {
            binding.btnSaveProfileSettings.startAnimation()
            user["name"] = name
            if (imageURI != null) {
                val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageURI!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        val url = downloadUrl.toString()
                        user["profileImg"] = url
                        update()
                    }
                }
            } else update()
        }
    }

    private fun update() {
        Firebase.firestore.collection("users")
            .document(MyUser.getMyUser()!!.UID)
            .update(user)
        FirebaseHelper.profileChangesEmitter()
        NavigationHelper.goBack()
    }
}