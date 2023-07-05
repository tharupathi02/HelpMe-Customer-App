package com.leoxtech.customerapp.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.leoxtech.customerapp.R

class ImageAdapter(internal var context: Context, private var imageList: ArrayList<Uri>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_image_item, parent, false)
        return ImageViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageURI(imageList[position])

        holder.imageView.setOnLongClickListener {
            imageList.removeAt(position)
            notifyDataSetChanged()
            true
        }

    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.imageView)
        }
    }
}