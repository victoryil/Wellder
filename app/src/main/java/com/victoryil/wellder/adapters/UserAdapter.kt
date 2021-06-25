package com.victoryil.wellder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.ChatsItemBinding
import com.victoryil.wellder.instances.FirebaseHelper
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.utils.formattedTimeWithParams
import com.victoryil.wellder.utils.goneUnless

class UserAdapter(val context: Context) :
    ListAdapter<User, UserAdapter.ViewHolder?>(ChatItemDiffCallback) {
    var onClickListen: ((user: String) -> Unit)? = null
    var onLongClickListen: ((user: String) -> Unit)? = null

    inner class ViewHolder(private val item: ChatsItemBinding) :
        RecyclerView.ViewHolder(item.root) {
        fun bind(user: User) {
            item.lblName.text = user.name

            if (user.profileImg.isNotBlank()) {
                Picasso
                    .get()
                    .load(user.profileImg)
                    .placeholder(R.drawable.ic_user)
                    .into(item.imgProfile)
            } else {
                item.imgProfile.setImageResource(R.drawable.ic_user)
            }

            Firebase.firestore.collection("messages")
                .orderBy("time", Query.Direction.DESCENDING).addSnapshotListener { docs, _ ->
                    if (docs != null && !docs.isEmpty) {
                        val msg = docs.documents.filter { item ->
                            (item["sender"] == user.UID
                                    && item["receiver"] == FirebaseHelper.getCurrentUID()) ||
                                    (item["receiver"] == user.UID && item["sender"] == FirebaseHelper.getCurrentUID())
                        }
                        var mensajes = hashMapOf<String, Any>()
                        if (msg.isNotEmpty()) {
                            mensajes = msg.first().data as HashMap<String, Any>
                        }
                        if (mensajes.isNotEmpty()) {
                            item.lblLastMessage.text = mensajes["message"] as String
                            val time = mensajes["time"] as Timestamp
                            val date = time.toDate()
                            item.lblLastTime.text = formattedTimeWithParams(date)
                        }

                        var count = 0

                        msg.forEach { documentSnapshot ->
                            if (!(documentSnapshot.data!!["isSeen"] as Boolean) && documentSnapshot.data!!["receiver"] == FirebaseHelper.getCurrentUID()) {
                                count++
                            }
                        }


                        item.notificationBubble.goneUnless(count != 0)
                        item.lblNotificationCount.goneUnless(count != 0)
                        if (count != 0) {
                            item.rootMain.setPadding(30, 0, 0, 0)
                            item.lblNotificationCount.text = count.toString()
                            item.rootCard.setBackgroundResource(R.drawable.bg_chat_item_notified)
                        } else {
                            item.rootMain.setPadding(0, 0, 0, 0)
                            item.lblNotificationCount.text = count.toString()
                            item.rootCard.setBackgroundResource(R.drawable.bg_chat_item)
                        }
                    }
                }

            val isOnline = if (user.isOnline) R.color.green else R.color.wellder_card_grey
            item.imgProfile.borderColor = ContextCompat.getColor(context, isOnline);
            item.root.setOnLongClickListener {
                onLongClickListen?.invoke(user.UID)
                return@setOnLongClickListener true
            }
            item.root.setOnClickListener { onClickListen?.invoke(user.UID) }
        }


    }

    override fun onCurrentListChanged(
        previousList: MutableList<User>,
        currentList: MutableList<User>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ChatsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

}


object ChatItemDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.UID == newItem.UID

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
        oldItem.isOnline == newItem.isOnline
                && oldItem.UID == newItem.UID
                && oldItem.profileImg == newItem.profileImg
                && oldItem.name == newItem.name
}
