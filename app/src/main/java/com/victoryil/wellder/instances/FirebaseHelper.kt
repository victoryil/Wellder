package com.victoryil.wellder.instances

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.victoryil.wellder.pojo.Chatlist
import com.victoryil.wellder.pojo.Message
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.utils.ErrorCodes
import com.victoryil.wellder.utils.debug

object FirebaseHelper {
    private var isReady_: MutableLiveData<Boolean> = MutableLiveData(false)
    val isReady: LiveData<Boolean>
        get() = isReady_

    //Usuarios
    private val userList_: MutableLiveData<ArrayList<User>> = MutableLiveData(arrayListOf())
    val userList: LiveData<ArrayList<User>>
        get() = userList_

    //Chats
    private val chatList_: MutableLiveData<ArrayList<Chatlist>> = MutableLiveData(arrayListOf())
    val chatList: LiveData<ArrayList<Chatlist>>
        get() = chatList_

    //Friends
    private val friendList_: MutableLiveData<ArrayList<User>> = MutableLiveData(arrayListOf())
    val friendList: LiveData<ArrayList<User>>
        get() = friendList_
    private val friendsArr = arrayListOf<User>()

    //Blocks
    private val blockList_: MutableLiveData<ArrayList<User>> = MutableLiveData(arrayListOf())
    val blockList: LiveData<ArrayList<User>>
        get() = blockList_

    var phone = ""
    lateinit var token: PhoneAuthProvider.ForceResendingToken


    //Finds
    fun findUserByUID(uid: String) = userList.value?.find { user -> user.UID == uid }
    fun findUsersByTAG(tag: String): User? = userList.value?.find { user -> user.tag == tag }

    //Gets
    private fun getAllChatList() {
        val arr = arrayListOf<Chatlist>()
        Firebase.firestore.collection("chatslist").addSnapshotListener { value, _ ->
            if (value != null) {
                arr.clear()
                for (document in value.documents) {
                    if (document.data != null
                        && document.data!!["user1"] != null
                        && document.data!!["user2"] != null
                        && document.data!!["time"] != null
                    ) arr.add(instantiateChats(document))

                }
                chatList_.value = arr
            }
        }
    }
    private fun getAllUsers() {
        val arr = arrayListOf<User>()
        Firebase.firestore.collection("users").addSnapshotListener { value, _ ->
            if (value != null) {
                arr.clear()
                for (document in value.documents) {
                    if (document.data!!["name"] != null) {
                        arr.add(firebaseUser2AppUser(document))
                    }
                }
                userList_.value = arr
            }
            isReady_.value = true
        }
    }

    fun getCurrentUID() = FirebaseAuth.getInstance().currentUser?.uid
    fun getMyFriends() {
        Firebase.firestore.collection("users").document(getCurrentUID()!!)
            .collection("friends").addSnapshotListener { value, error ->
                if (value != null) {
                    friendsArr.clear()
                    for (document in value.documents) {
                        findUserByUID(document.data!!["uid"] as String)?.let {
                            friendsArr.add(it)
                        }
                    }
                    friendList_.value = friendsArr
                }
            }
    }
    fun getBlocked() {
        val arr = arrayListOf<User>()
        Firebase.firestore.collection("users").document(getCurrentUID()!!)
            .collection("blocked").addSnapshotListener { value, error ->
                if (value != null) {
                    arr.clear()
                    for (document in value.documents) {

                        findUserByUID(document.data!!["uid"] as String)?.let {
                            arr.add(it)
                        }
                    }
                    blockList_.value = arr
                }
            }
    }
    fun getMyUser() = findUserByUID(getCurrentUID()!!)

    //Set
    fun addFriendByUID(uid: String): ErrorCodes {
        if (friendList.value == null) return ErrorCodes.UNEXPECTED
        if (uid == getCurrentUID()) return ErrorCodes.ARE_ME
        val user = findUserByUID(uid) ?: return ErrorCodes.USER_NOT_FOUND
        if (friendList.value!!.contains(user)) return ErrorCodes.FRIEND_YET
        Firebase.firestore.collection("users")
            .document(getCurrentUID()!!)
            .collection("friends").add(hashMapOf(Pair("uid", uid)))
        return ErrorCodes.SUCCESS
    }
    fun blockByUID(uid: String) {
        Firebase.firestore.collection("users")
            .document(getCurrentUID()!!)
            .collection("blocked").add(hashMapOf(Pair("uid", uid))).addOnSuccessListener {
                if (isFriendsById(uid)) {
                    deleteFriendById(uid)
                }
            }
    }
    fun setStatus(isOnline: Boolean) {
        Firebase.firestore
            .collection("users")
            .document(getCurrentUID()!!).update("isOnline", isOnline)
    }
    fun setMessageRead(id: String) {
        Firebase.firestore.collection("messages").document(id).update("isSeen", true)
    }
    fun updateProfile() {
        if (getCurrentUID() != null) {
            val user = findUserByUID(getCurrentUID()!!)
            if (user != null) {
                MyUser.setMyUser(user)
            }
        }
    }
    fun setMyUser() {
        if (getCurrentUID() != null) {
            val user = findUserByUID(getCurrentUID()!!)
            if (user != null) {
                MyUser.setMyUser(user)
            }
        }
    }

    //Deletes
    private fun deleteFriendById(uid: String) {
        val ref = Firebase.firestore.collection("users")
            .document(getCurrentUID()!!)
            .collection("friends")

        ref.whereEqualTo("uid", uid).get().addOnSuccessListener {
            for (document in it.documents) {
                ref.document(document.id).delete()
            }
        }
    }

    fun unblockByUID(uid: String) {
        val ref = Firebase.firestore.collection("users")
            .document(getCurrentUID()!!)
            .collection("blocked")

        ref.whereEqualTo("uid", uid).get().addOnSuccessListener {
            for (document in it.documents) {
                ref.document(document.id).delete()
            }
        }
    }
    fun deleteChat(uid: String) {
        val chat: Chatlist? =
            chatList.value?.find { chatlist -> checkChatList(chatlist, uid) }
        if (chat == null) {
            return
        }
        val field = if (getCurrentUID() == chat.uid1) "user1Open" else "user2Open"
        val ref = Firebase.firestore
            .collection("chatslist")
        ref.whereEqualTo("user1", chat.uid1)
            .whereEqualTo("user2", chat.uid2)
            .get().addOnSuccessListener {
                for (document in it.documents) {
                    ref.document(document.id).update(field, false)
                }
            }
    }


    // Filters
    private fun isFriendsById(uid: String): Boolean {
        return friendList.value?.any { user -> user.UID == uid } ?: false
    }

    fun filterMyChats(chatlist: ArrayList<Chatlist>): ArrayList<User> {
        val myChats: ArrayList<User> = arrayListOf()
        for (openChat in chatlist) {
            if (openChat.uid1 == getCurrentUID() && openChat.uid1Open) {
                val user = findUserByUID(openChat.uid2)
                if (user != null) myChats.add(user)
            }
            if (openChat.uid2 == getCurrentUID() && openChat.uid2Open) {
                val user = findUserByUID(openChat.uid1)
                if (user != null) myChats.add(user)
            }
        }
        return myChats
    }
    fun filterNotFriends(users: ArrayList<User>): ArrayList<User> {
        val arr: ArrayList<User> = arrayListOf()
        if (friendList.value != null) {
            arr.clear()
            for (user in users) {
                val any = friendList.value!!.any { u -> u.UID == user.UID }
                if (!any && user.UID != getCurrentUID()) arr.add(user)
            }
        }
        return arr
    }
    fun filterNotFriends(): ArrayList<User> {
        val arr: ArrayList<User> = arrayListOf()
        val users: ArrayList<User> = userList.value ?: arrayListOf()
        if (friendList.value != null) {
            arr.clear()
            for (user in users) {
                val any = friendList.value!!.any { u -> u.UID == user.UID }

                if (!any && user.UID != getCurrentUID()) arr.add(user)
            }
        }
        return arr
    }
    fun filterNotBlockByUID(users: ArrayList<User>): ArrayList<User> {
        val arr: ArrayList<User> = arrayListOf()
        if (friendList.value != null) {
            arr.clear()
            for (user in users) {
                val any = blockList.value!!.any { u -> u.UID == user.UID }

                if (!any && user.UID != getCurrentUID()) arr.add(user)
            }
        }
        return arr
    }

    //Functionality
    fun logout() {
        MyUser.resetUser()
        isReady_.value = false
        FirebaseAuth.getInstance().signOut()
        NavigationHelper.navigateToSendOTP()
    }

    //Instantiate
    private fun instantiateChats(document: DocumentSnapshot): Chatlist = Chatlist(
        document.id,
        document.data!!["user1"] as String,
        document.data!!["user2"] as String,
        document.data!!["time"] as Timestamp,
        document.data!!["user1Open"] as Boolean,
        document.data!!["user2Open"] as Boolean,
    )
    private fun setFirebaseSettings() {
        Firebase.firestore.firestoreSettings = firestoreSettings { isPersistenceEnabled = true }
    }

    fun instantiateMessage(document: DocumentSnapshot) = Message(
        document.id,
        document.data!!["message"] as String,
        document.data!!["isPhoto"] as Boolean,
        document.data!!["url"] as String,
        document.data!!["isSeen"] as Boolean,
        document.data!!["sender"] as String,
        document.data!!["receiver"] as String,
        document.data!!["time"] as Timestamp,
    )
    fun firebaseUser2AppUser(document: DocumentSnapshot): User = User(
        document.id,
        name = document.data!!["name"] as String,
        phone = document.data!!["phone"] as String,
        isOnline = document.data!!["isOnline"] as Boolean,
        tag = document.data!!["TAG"] as String,
        profileImg = document.data!!["profileImg"] as String,
        geo = document.data!!["geo"] as GeoPoint
    )
    fun firebaseInitMyUser() {
        if (getCurrentUID() != null) {
            val user = findUserByUID(getCurrentUID()!!)
            if (user != null) {
                MyUser.setMyUser(user)
            }
        }
    }
    fun loadFirebase() {
        setFirebaseSettings()
        getAllUsers()
        getAllChatList()
    }
    fun profileChangesEmitter() {
        chatList_.value = chatList_.value
        friendList_.value = friendList_.value
        updateProfile()
    }

    //Checks
    private fun checkChatList(chatlist: Chatlist, uid: String): Boolean =
        chatlist.uid1 == getCurrentUID() && chatlist.uid2 == uid
                || chatlist.uid2 == getCurrentUID() && chatlist.uid1 == uid

    fun checkBlockByUID(uid: String): Boolean =
        blockList.value?.any { user -> user.UID == uid } ?: false
    fun checkFriendByUID(uid: String): Boolean =
        friendList.value?.any { user -> user.UID == uid } ?: false
}