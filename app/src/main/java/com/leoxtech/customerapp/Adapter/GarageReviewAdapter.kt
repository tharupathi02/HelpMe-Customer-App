package com.leoxtech.customerapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.Review
import com.leoxtech.customerapp.R

class GarageReviewAdapter (internal var context: Context, private var garageReview: List<Review>) : RecyclerView.Adapter<GarageReviewAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GarageReviewAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.review_item, parent, false))
    }

    override fun onBindViewHolder(holder: GarageReviewAdapter.MyViewHolder, position: Int) {
        holder.txtCustomerName!!.text = garageReview.get(position).customerName
        holder.txtCustomerMessage!!.text = garageReview.get(position).comment
        holder.txtReviewDateTime!!.text = Common.convertTimeStampToDate(garageReview.get(position).time!!.toLong())
        Glide.with(context).load(garageReview.get(position).customerAvatar).into(holder.imgCustomerAvatar!!)
        holder.ratingBar!!.rating = garageReview.get(position).ratingValue!!.toFloat()
    }

    override fun getItemCount(): Int {
        return garageReview.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgCustomerAvatar: ImageView? = null
        var txtCustomerName: TextView? = null
        var txtCustomerMessage: TextView? = null
        var txtReviewDateTime: TextView? = null
        var ratingBar: RatingBar? = null

        init {
            imgCustomerAvatar = itemView.findViewById(R.id.imgCustomerAvatar) as ImageView
            txtCustomerName = itemView.findViewById(R.id.txtCustomerName) as TextView
            txtCustomerMessage = itemView.findViewById(R.id.txtCustomerMessage) as TextView
            txtReviewDateTime = itemView.findViewById(R.id.txtReviewDateTime) as TextView
            ratingBar = itemView.findViewById(R.id.ratingBar) as RatingBar
        }
    }
}