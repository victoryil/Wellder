package com.victoryil.wellder.ui.register

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.RegisterFragmentBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.utils.debug
import com.victoryil.wellder.utils.goneUnless
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast
import java.util.*
import kotlin.collections.HashMap

class RegisterFragment : Fragment(R.layout.register_fragment) {
    private val binding: RegisterFragmentBinding by viewBinding()
    private var imageURI: Uri? = null
    private var canRegister = false
    private var storageRef: StorageReference? = null
    private var user: HashMap<String, Any> = HashMap()
    private val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        FusedLocationProviderClient(requireActivity())
    }
    private val locationRequest: LocationRequest by lazy {
        LocationRequest()
    }
    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation: Location = p0.lastLocation
            user["geo"] = GeoPoint(lastLocation.latitude, lastLocation.longitude)
            canRegister = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setups()
    }

    private fun setups() {
        openGps()
        binding.btnsetGps.setOnClickListener { openGps() }

        storageRef = Firebase.storage.reference.child("users/profile")
        binding.btnRegister.isEnabled = false
        binding.edName.setText("")
        binding.edName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.btnRegister.isEnabled = s!!.isNotBlank()
            }

        })
        binding.btnEditPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop(128F, 128F)
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
                            binding.imgProfile.setImageURI(imageURI)
                            Picasso.get().load(imageURI)
                                .placeholder(R.drawable.ic_user)
                                .into(binding.imgProfile)
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
        binding.btnRegister.setOnClickListener { registerData() }
    }

    private fun openGps() {
        binding.btnsetGps.goneUnless(false)
        getLastLocation()
    }


    private fun registerData() {
        user["profileImg"] = ""
        val base62chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var newID = ""
        for (index in 1..6) {
            newID += base62chars.random()
        }
        user["TAG"] = newID
        user["isOnline"] = true
        if (!canRegister) {
            MotionToast.darkToast(
                requireActivity(),
                "Error: GPS",
                getString(R.string.error_gps),
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                Typeface.DEFAULT
            )
            binding.btnsetGps.goneUnless(true)
            return
        }
        binding.btnRegister.startAnimation()
        //Subir foto
        user["phone"] = FirebaseAuth.getInstance().currentUser.phoneNumber ?: ""
        user["name"] = binding.edName.text.toString()
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
                    createUser()
                }
            }
        } else {
            createUser()
        }
    }

    private fun createUser() {
        val db = Firebase.firestore
        db.collection("users").document(currentUser!!.uid)
            .set(user)
            .addOnSuccessListener {
                FirebaseHelper.loadFirebase()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    FirebaseHelper.setMyUser()
                    NavigationHelper.navigateToMain()
                }
            }
    }

    private fun checkPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            52
        )
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    if (task.result == null) {
                        getNewLocation()
                    } else {
                        user["geo"] = GeoPoint(task.result.latitude, task.result.longitude)
                        canRegister = true
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please Enable your location service",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun getNewLocation() {
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        if (!checkPermission()) return
        if (Looper.myLooper() == null) return
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallBack, Looper.myLooper()
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 52) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                debug("concedido")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}