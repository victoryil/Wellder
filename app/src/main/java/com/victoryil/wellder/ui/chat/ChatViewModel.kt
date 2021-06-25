package com.victoryil.wellder.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.pojo.Message
import com.victoryil.wellder.utils.debug
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.Map
import kotlin.collections.arrayListOf
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.sortedWith

class ChatViewModel : ViewModel() {
    private var messagesList: ArrayList<Message> = arrayListOf()

    private var _messages: MutableLiveData<ArrayList<Message>> = MutableLiveData(arrayListOf())
    val messages: LiveData<ArrayList<Message>>
        get() = _messages

    var uBlocked: Boolean = false

    fun getData(meId: String, otherId: String) {
        retrieveMessage(meId, otherId)
        checkBlock(meId, otherId)
    }

    fun sendMessage(message: String, sender: String, receiver: String, imageUri: String) {
        val reference = Firebase.firestore
        val messageMap = HashMap<String, Any?>()
        messageMap["message"] = message
        messageMap["sender"] = sender
        messageMap["receiver"] = receiver
        messageMap["isSeen"] = false
        messageMap["isPhoto"] = imageUri.isNotBlank()
        messageMap["url"] = imageUri
        messageMap["time"] = Timestamp.now()
        reference.collection("messages").add(messageMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    registerChat(sender, receiver)
                }
            }
    }

    fun setRead(meId: String, otherId: String) {
        Firebase.firestore.collection("messages")
            .whereEqualTo("sender", otherId)
            .whereEqualTo("receiver", meId)
            .whereEqualTo("isSeen", false)
            .get().addOnSuccessListener {
                for (document in it.documents) {
                    FirebaseHelper.setMessageRead(document.id)
                }
            }
    }

    private fun retrieveMessage(sender: String, receiver: String) {
        Firebase.firestore.collection("messages")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                messagesList.clear()
                if (value != null && !value.isEmpty) {
                    for (document in value.documents) {
                        if (checkDataIntegrity(document.data, sender, receiver)) {
                            messagesList.add(createMessage(document))
                        }
                    }
                    val msg = arrayListOf<Message>()
                    messagesList.sortedWith(compareByDescending { it.sendTime.toDate() }).forEach {
                        msg.add(it)
                    }
                    updateList(msg)
                }

            }
    }

    private fun updateList(msg: ArrayList<Message>) {
        _messages.value = msg
    }

    private fun checkDataIntegrity(
        document: Map<String, Any>?,
        sender: String,
        receiver: String
    ): Boolean {
        if (document == null
            && document?.get("sender") == null
            && document?.get("receiver") == null
        ) {
            return false
        }
        val send = document["sender"] as String
        val rec = document["receiver"] as String
        if (send == sender && rec == receiver) return true
        if (rec == sender && send == receiver) return true
        return false
    }

    private fun createMessage(document: DocumentSnapshot) = Message(
        document.id,
        document.data!!["message"] as String,
        document.data!!["isPhoto"] as Boolean,
        document.data!!["url"] as String,
        document.data!!["isSeen"] as Boolean,
        document.data!!["sender"] as String,
        document.data!!["receiver"] as String,
        document.data!!["time"] as Timestamp,
    )

    private fun registerChat(sender: String, receiver: String) {

        val chatl = FirebaseHelper.chatList.value?.find { chatlist ->
            (chatlist.uid1 == sender && chatlist.uid2 == receiver) ||
                    (chatlist.uid2 == sender && chatlist.uid1 == receiver)
        }

        val chat = HashMap<String, Any?>()
        if (chatl == null) {
            chat["user1"] = sender
            chat["user2"] = receiver
            chat["time"] = Timestamp.now()
            chat["user1Open"] = true
            chat["user2Open"] = true
            Firebase
                .firestore
                .collection("chatslist").add(chat)
        } else {
            chat["time"] = Timestamp.now()
            chat["user1Open"] = true
            chat["user2Open"] = true
            Firebase
                .firestore
                .collection("chatslist")
                .document(chatl.uuid).update(chat)
        }
    }

    private fun checkBlock(meId: String, otherId: String) {
        Firebase.firestore
            .collection("users")
            .document(otherId)
            .collection("blocked")
            .whereEqualTo("uid", meId).addSnapshotListener { value, error ->
                value?.documents?.forEach { documentSnapshot ->
                    uBlocked = documentSnapshot.exists()
                }
            }
    }
}