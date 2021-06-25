package com.victoryil.wellder.adapters

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.victoryil.wellder.instances.MyUser
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.UserSearchItemBinding
import com.victoryil.wellder.pojo.User
import kotlin.math.roundToLong

class SearchAdapter : ListAdapter<User, SearchAdapter.ViewHolder?>(ItemDiffCallback) {
    private var me: User? = null
    var onClickListen: ((user: String) -> Unit)? = null

    init {
        me = MyUser.getMyUser()
    }

    inner class ViewHolder(private val item: UserSearchItemBinding) :
        RecyclerView.ViewHolder(item.root) {
        fun bind(user: User) {
            setItem(user)
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
            item.root.setOnClickListener { onClickListen?.invoke(user.UID) }
        }

        private fun setItem(user: User) {
            if (MyUser.getMyUser() != null) {
                val loc1 = Location("")
                loc1.longitude = MyUser.getMyUser()!!.geo.longitude
                loc1.latitude = MyUser.getMyUser()!!.geo.latitude
                val loc2 = Location("")
                loc2.longitude = user.geo.longitude
                loc2.latitude = user.geo.latitude
                val d2m = loc1.distanceTo(loc2)
                if (d2m / 1000 >= 1.0) {
                    item.lblMeters.text = "${(d2m / 1000).roundToLong()} km"
                } else item.lblMeters.text = "${d2m.roundToLong()} m"
            }
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
            UserSearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

}

object ItemDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.UID == newItem.UID

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
        oldItem.isOnline == newItem.isOnline &&
                oldItem.UID == newItem.UID
}
