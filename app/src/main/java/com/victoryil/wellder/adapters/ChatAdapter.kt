package com.victoryil.wellder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.ChatItemNewBinding
import com.victoryil.wellder.instances.Preferences
import com.victoryil.wellder.pojo.Message
import com.victoryil.wellder.utils.formattedTimeWithParams
import com.victoryil.wellder.utils.goneUnless

class ChatAdapter : ListAdapter<Message, ChatAdapter.ViewHolder?>(MessageItemDiffCallback) {
    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    var onMsgClick: ((text: String) -> Unit)? = null
    var onPhotoClick: ((text: String) -> Unit)? = null

    inner class ViewHolder(
        private val isR: Boolean = true,
        private val item: ChatItemNewBinding
    ) : RecyclerView.ViewHolder(item.root) {
        fun bind(message: Message) {
            item.chatL.goneUnless(!isR)
            item.chatR.goneUnless(isR)

            val date = message.sendTime.toDate()
            val timeFormatted = formattedTimeWithParams(date)
            if (isR) {
                item.lblMessageR.textSize = Preferences.getFontSize().toFloat()
                item.lblMessageR.text = message.message
                item.lblMessageR.goneUnless(!message.isPhoto)
                item.chatR.goneUnless(!message.isPhoto)
                item.cardViewR.goneUnless(message.isPhoto)
                item.lblTimeR.goneUnless(!message.isPhoto)
                item.lblTimeR2.goneUnless(message.isPhoto)
                item.icCheckR.goneUnless(!message.isPhoto)
                item.lblTimeR.text = timeFormatted
                item.lblTimeR2.text = timeFormatted
                val drawable =
                    if (message.isSeen) R.drawable.ic_double_check_blue else R.drawable.ic_check_blue
                Picasso.get().load(drawable).placeholder(drawable).into(item.icCheckR)
                if (message.isPhoto) {
                    Picasso.get().load(message.url).into(item.imgR)
                }
                item.lblMessageR.setOnClickListener {
                    onMsgClick?.invoke(message.message)
                }
                item.imgR.setOnClickListener { onPhotoClick?.invoke(message.url) }

            } else {
                item.lblMessageL.textSize = Preferences.getFontSize().toFloat()
                item.lblMessageL.text = message.message
                item.cardViewL.goneUnless(message.isPhoto)
                item.chatL.goneUnless(!message.isPhoto)
                item.lblMessageL.goneUnless(!message.isPhoto)
                item.icCheckL.goneUnless(!message.isPhoto)
                item.lblTimeL.goneUnless(!message.isPhoto)
                item.lblTimeL2.goneUnless(message.isPhoto)
                item.lblTimeL.text = timeFormatted
                item.lblTimeL2.text = timeFormatted
                item.lblMessageL.setOnClickListener { onMsgClick?.invoke(message.message) }
                val drawable =
                    if (message.isSeen) R.drawable.ic_double_check_white else R.drawable.ic_check_white
                Picasso.get().load(drawable).placeholder(drawable).into(item.icCheckL)
                if (message.isPhoto) {
                    Picasso.get().load(message.url).into(item.imgL)
                }
                item.imgL.setOnClickListener { onPhotoClick?.invoke(message.url) }
            }
        }

    }

    override fun onCurrentListChanged(
        previousList: MutableList<Message>,
        currentList: MutableList<Message>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        notifyDataSetChanged()
        notifyItemInserted(itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            viewType == 1,
            ChatItemNewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemViewType(position: Int): Int =
        if (currentList[position].senderUID == firebaseUser.uid) 1 else 0

    override fun getItemId(position: Int): Long = position.toLong()
}

object MessageItemDiffCallback : DiffUtil.ItemCallback<Message>() {

    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
        oldItem.uid == newItem.uid

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
        oldItem.message == newItem.message
                && oldItem.uid == newItem.uid
}
