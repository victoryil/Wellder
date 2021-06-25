package com.victoryil.wellder.ui.chat

import android.app.Activity
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.vanniktech.emoji.EmojiPopup
import com.victoryil.wellder.R
import com.victoryil.wellder.adapters.ChatAdapter
import com.victoryil.wellder.databinding.ChatFragmentNewBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.instances.NavigationHelper
import com.victoryil.wellder.instances.Preferences
import com.victoryil.wellder.pojo.Message
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.utils.hideSoftKeyboard
import com.victoryil.wellder.utils.isNetworkAvailable
import com.victoryil.wellder.utils.showKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast
import java.util.*


class ChatFragment : Fragment(R.layout.chat_fragment_new), TextToSpeech.OnInitListener {

    private val binding: ChatFragmentNewBinding by viewBinding()
    private val listadapter: ChatAdapter = ChatAdapter().apply {
        onMsgClick = {
            if (Preferences.getTts()) speak(it)
        }
        onPhotoClick = {
            val available = isNetworkAvailable(requireActivity())
            if (available) {
                CoroutineScope(Dispatchers.Default).launch {
                    delay(200) //Como todavia no ha cargado la foto peta asi que con esto lo arregla <3
                }
                NavigationHelper.navigateToImage(it, user.name)
            } else {
                Toast.makeText(requireContext(), "internet not avaliable", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    private val viewModel: ChatViewModel by viewModels()
    private var imageURI: Uri? = null
    private var imageFirebase: String = ""
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var tts: TextToSpeech
    private lateinit var user: User
    private lateinit var name: String
    private var haveText = false
    private var isFirst = true
    private var isDestroy = false
    private var isBlock = false
    private val emojiPopup: EmojiPopup by lazy {
        EmojiPopup.Builder.fromRootView(binding.root).setOnEmojiPopupDismissListener {
            if (!isDestroy) {
                binding.btnEmoji.setImageResource(R.drawable.ic_emoji)
            }
        }.build(binding.edMessage)
    }
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var mnuBlock: MenuItem
    private lateinit var mnuAdd: MenuItem
    private lateinit var mnuUnblock: MenuItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupAll()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.chat, menu)
        mnuAdd = menu.findItem(R.id.mnuAddFriend)
        mnuBlock = menu.findItem(R.id.mnuBlock)
        mnuUnblock = menu.findItem(R.id.mnuUnBlock)
        isBlock = FirebaseHelper.checkBlockByUID(args.uid)
        if (isBlock) {
            mnuBlock.isVisible = false
            mnuAdd.isVisible = false
        } else {
            mnuUnblock.isVisible = false
        }
        if (FirebaseHelper.checkFriendByUID(args.uid)) {
            mnuAdd.isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun setupAll() {
        tts = TextToSpeech(requireActivity(), this)
        tts.setSpeechRate(1f)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        getUserData()
        setupViewmodel()
        setupRecycler()
        listener()
        //FirebaseHelper.setRead(args.uid)
    }

    private fun sendPhoto() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .start { resultCode, data ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        imageURI = data?.data
                        //Toast.makeText(context, "Correcto", Toast.LENGTH_LONG).show()
                        val fileRef =
                            Firebase.storage.reference.child("users/messages/${firebaseUser.uid}/${args.uid}/${System.currentTimeMillis()}.jpg")
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
                                imageFirebase = url
                                sendMessage(true)
                            }
                        }
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        // Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun setupViewmodel() {
        viewModel.getData(firebaseUser.uid, args.uid)
        viewModel.messages.observe(viewLifecycleOwner, {
            if (NavigationHelper.getCurrentDestination()?.id == R.id.chatFragmentDestination) {
                viewModel.setRead(FirebaseHelper.getCurrentUID() ?: "", args.uid)
            }
            updateList(it)
        })
    }

    private fun listener() {
        binding.btnCamera.setOnClickListener { sendPhoto() }
        binding.btnSend.setOnClickListener {
            if (viewModel.uBlocked) {
                Toast.makeText(requireContext(), "Estas bloqueado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isBlock) {
                MotionToast.darkToast(
                    requireActivity(),
                    "Invalid Action",
                    "You must unblock user to send message ore photo",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    Typeface.MONOSPACE
                )
            } else {
                if (haveText) sendMessage(false)
            }
        }
        binding.btnEmoji.setOnClickListener { emojiControls() }
        binding.edMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                haveText = s.toString().isNotEmpty()
            }

        })
        binding.edMessage.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) isFirst = false
        }
    }

    private fun emojiControls() {
        if (isFirst) {
            binding.edMessage.requestFocus()
            binding.root.showKeyboard()
            isFirst = false
        } else {
            val drawable = if (emojiPopup.isShowing) R.drawable.ic_emoji else R.drawable.ic_keyboard
            emojiPopup.toggle()
            binding.btnEmoji.setImageResource(drawable)
        }
    }

    private fun sendMessage(isPhoto: Boolean) {
        var message: String
        var imageUri = ""
        if (isPhoto) {
            imageUri = imageFirebase
            message = "Image"
        } else {
            message = binding.edMessage.text.toString()
        }
        message = message.trim()
        if (message.isBlank() && imageUri.isBlank()) return
        viewModel.sendMessage(message, firebaseUser.uid, args.uid, imageUri)
        binding.edMessage.setText("")
    }

    private fun getUserData() {
        NavigationHelper.setChatPersonUID(args.uid)
        Firebase.firestore.collection("users")
            .document(args.uid).get().addOnSuccessListener { documents ->
                if (documents.exists()) {
                    user = FirebaseHelper.firebaseUser2AppUser(documents)
                    setToolbar()
                }
            }
    }

    private fun updateList(message: ArrayList<Message>) {
        listadapter.submitList(message)
    }

    private fun setupRecycler() {
        binding.lstChat.run {
            setHasFixedSize(true)
            adapter = listadapter
            layoutManager = LinearLayoutManager(context).apply { reverseLayout = true }
        }
    }

    private fun setToolbar() {
        name = user.name
        (activity as AppCompatActivity).supportActionBar?.run { title = user.name }
        requireActivity().invalidateOptionsMenu()
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }


    override fun onInit(status: Int) {
        if (status != TextToSpeech.ERROR) {
            val result = tts.setLanguage(Locale.getDefault())
            /*if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            }*/
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        isDestroy = true
        super.onDestroy()
    }

    override fun onResume() {
        isFirst = true
        super.onResume()
    }

    override fun onPause() {
        binding.root.hideSoftKeyboard()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnuBlock -> {
                mnuUnblock.isVisible = true
                mnuAdd.isVisible = false
                item.isVisible = false
                isBlock = true
                FirebaseHelper.blockByUID(args.uid)
            }
            R.id.mnuUnBlock -> {
                mnuAdd.isVisible = true
                mnuBlock.isVisible = true
                item.isVisible = false
                isBlock = false
                FirebaseHelper.unblockByUID(args.uid)
            }
            R.id.mnuAddFriend -> {
                item.isVisible = false
                FirebaseHelper.addFriendByUID(NavigationHelper.getCurrentChatPersonUID())
            }
        }
        return super.onOptionsItemSelected(item)
    }

}