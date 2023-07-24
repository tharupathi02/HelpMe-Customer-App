package com.leoxtech.customerapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.leoxtech.customerapp.Model.Booking
import com.leoxtech.customerapp.Model.RequestHelpModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.Screens.BookingDetails

class MyBookingsAdapter (internal var context: Context, private var bookingList: List<Booking>) : RecyclerView.Adapter<MyBookingsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.booking_item, parent, false))
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txtBookingRequestTitle!!.text = bookingList[position].issueTitle
        holder.txtBookingRequestDescription!!.text = bookingList[position].issueDescription
        holder.txtBookingRequest!!.text = bookingList[position].bookingStatus

        Glide.with(context).load(bookingList[position].imageList!![0]).into(holder.imgBookingRequest!!)

        if (bookingList[position].bookingNote!!.isNotEmpty()) {
            holder.txtBookingNote!!.visibility = View.VISIBLE
            holder.txtBookingNote!!.text = bookingList[position].bookingNote
        }

        holder.cardBookingRequest!!.setOnClickListener {
            ContextCompat.startActivity(context, Intent(context, BookingDetails::class.java).putExtra("key", bookingList.get(position).key), null)
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgBookingRequest: ImageView? = null
        var txtBookingRequestTitle: TextView? = null
        var txtBookingRequestDescription: TextView? = null
        var txtBookingRequest: TextView? = null
        var txtBookingNote: TextView? = null
        var cardBookingRequest: MaterialCardView? = null

        init {
            imgBookingRequest = itemView.findViewById(R.id.imgBookingRequest) as ImageView
            txtBookingRequestTitle = itemView.findViewById(R.id.txtBookingRequestTitle) as TextView
            txtBookingRequestDescription = itemView.findViewById(R.id.txtBookingRequestDescription) as TextView
            txtBookingRequest = itemView.findViewById(R.id.txtBookingRequest) as TextView
            txtBookingNote = itemView.findViewById(R.id.txtBookingNote) as TextView
            cardBookingRequest = itemView.findViewById(R.id.cardBookingRequest) as MaterialCardView
        }
    }

}