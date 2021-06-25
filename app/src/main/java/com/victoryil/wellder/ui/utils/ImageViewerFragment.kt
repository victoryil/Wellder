package com.victoryil.wellder.ui.utils

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import com.victoryil.wellder.R
import com.victoryil.wellder.databinding.FragmentImageViewerBinding
import com.victoryil.wellder.utils.debug
import com.victoryil.wellder.utils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewerFragment : Fragment(R.layout.fragment_image_viewer) {
    private  val binding: FragmentImageViewerBinding by viewBinding()
    private val args: ImageViewerFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            val img = Picasso.get().load(args.url).get()
            binding.photoView.setImageBitmap(img)
        }
        setToolbar()
    }
    private fun setToolbar() {
        (activity as AppCompatActivity).supportActionBar?.run { title = args.name }
        requireActivity().invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        menu.removeItem(R.id.ajustes)
        super.onCreateOptionsMenu(menu, inflater)
    }
}