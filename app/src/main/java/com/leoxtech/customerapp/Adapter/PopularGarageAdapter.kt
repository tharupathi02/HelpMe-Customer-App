package com.leoxtech.customerapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.GarageModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.Screens.BookingDetails
import com.leoxtech.customerapp.Screens.GarageView

class PopularGarageAdapter(internal var context: Context, private var popularGarageList: List<GarageModel>) : RecyclerView.Adapter<PopularGarageAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgGarageImage: ImageView? = null
        var txtGarageName: TextView? = null
        var txtGarageDescription: TextView? = null
        var ratingBar: RatingBar? = null
        var cardGarageItem: MaterialCardView? = null

        init {
            imgGarageImage = itemView.findViewById(R.id.imgGarageImage) as ImageView
            txtGarageName = itemView.findViewById(R.id.txtGarageName) as TextView
            txtGarageDescription = itemView.findViewById(R.id.txtGarageDescription) as TextView
            ratingBar = itemView.findViewById(R.id.ratingBar) as RatingBar
            cardGarageItem = itemView.findViewById(R.id.cardGarageItem) as MaterialCardView

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.top_garage_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(popularGarageList.get(position).photoURL).into(holder.imgGarageImage!!)
        holder.txtGarageName!!.setText(popularGarageList.get(position).companyName)
        holder.txtGarageDescription!!.setText(popularGarageList.get(position).description)
        if (popularGarageList.get(position).garageReview!!.isEmpty()) {
            holder.ratingBar!!.rating = Common.ratingCalculate(0.0f, 0.0f)
        } else {
            holder.ratingBar!!.rating = Common.ratingCalculate(popularGarageList.get(position).garageReview!![0].ratingValue.toString().toFloat(), popularGarageList.get(position).garageReview!![0].ratingCount.toString().toFloat())
        }

        holder.cardGarageItem!!.setOnClickListener {
            startActivity(context, Intent(context, GarageView::class.java).putExtra("garageId", popularGarageList.get(position).uid), null)
        }

    }

    override fun getItemCount(): Int {
        return popularGarageList.size
    }
}