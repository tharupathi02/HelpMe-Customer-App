package com.leoxtech.customerapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leoxtech.customerapp.Model.GarageModel
import com.leoxtech.customerapp.R

class PopularGarageAdapter(internal var context: Context, internal var popularGarageList: List<GarageModel>) : RecyclerView.Adapter<PopularGarageAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgGarageImage: ImageView? = null
        var txtGarageName: TextView? = null
        var txtGarageDescription: TextView? = null
        var ratingBar: RatingBar? = null

        init {
            imgGarageImage = itemView.findViewById(R.id.imgGarageImage) as ImageView
            txtGarageName = itemView.findViewById(R.id.txtGarageName) as TextView
            txtGarageDescription = itemView.findViewById(R.id.txtGarageDescription) as TextView
            ratingBar = itemView.findViewById(R.id.ratingBar) as RatingBar

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.top_garage_item, parent, false))
    }

    override fun getItemCount(): Int {
        return popularGarageList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(popularGarageList[position].garageImage).into(holder.imgGarageImage!!)
        holder.txtGarageName!!.text = popularGarageList[position].garageName
        holder.txtGarageDescription!!.text = popularGarageList[position].garageDescription
    }
}