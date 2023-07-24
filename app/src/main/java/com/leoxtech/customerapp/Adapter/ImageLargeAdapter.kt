package com.leoxtech.customerapp.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leoxtech.customerapp.R

class ImageLargeAdapter(internal var context: Context, private var imageList: ArrayList<String>) :
    RecyclerView.Adapter<ImageLargeAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_image_view_large, parent, false)
        return ImageViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(context).load(Uri.parse(imageList[position])).into(holder.imageView)

        holder.imageView.setOnClickListener {
            //startActivity(context, Intent(context, ImageViewer::class.java).putExtra("image", imageList[position]), null)
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