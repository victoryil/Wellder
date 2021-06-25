package com.victoryil.wellder.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.BlockUserItemBinding
import com.victoryil.wellder.pojo.User

class BlockAdapter : ListAdapter<User, BlockAdapter.ViewHolder?>(BlockedDiffCallback) {
    var onClickListen: ((user: String) -> Unit)? = null
    inner class ViewHolder(private val item: BlockUserItemBinding) :
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
            BlockUserItemBinding.inflate(
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

object BlockedDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.UID == newItem.UID

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
        oldItem.isOnline == newItem.isOnline &&
                oldItem.UID == newItem.UID
}
