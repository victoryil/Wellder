package com.victoryil.wellder.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.UserFriendItemBinding
import com.victoryil.wellder.pojo.User
import com.victoryil.wellder.utils.debug

class FriendAdapter(val context: Context) :
    ListAdapter<User, FriendAdapter.ViewHolder?>(FriendItemDiffCallback) {
    var onClickListen: ((user: String) -> Unit)? = null

    inner class ViewHolder(private val item: UserFriendItemBinding) :
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
            val isOnline = if (user.isOnline) R.color.green else R.color.wellder_card_grey
            item.imgProfile.borderColor = ContextCompat.getColor(context, isOnline);
            item.lblTag.text = "#${user.tag}"
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
            UserFriendItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun submitList(list: MutableList<User>?) {
        super.submitList(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

}

object FriendItemDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
        oldItem.UID == newItem.UID

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
        oldItem.isOnline == newItem.isOnline
                && oldItem.UID == newItem.UID


}

