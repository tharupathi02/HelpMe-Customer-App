package com.leoxtech.customerapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.RequestHelpModel
import com.leoxtech.customerapp.R

class UrgentRequestAdapter (internal var context: Context, private var urgentRequestList: List<RequestHelpModel>) : RecyclerView.Adapter<UrgentRequestAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrgentRequestAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.urgent_request_item, parent, false))
    }

    override fun onBindViewHolder(holder: UrgentRequestAdapter.MyViewHolder, position: Int) {
        holder.txtUrgentRequestTitle!!.text = urgentRequestList.get(position).customerIssueTitle
        holder.txtUrgentRequestDescription!!.text = urgentRequestList.get(position).customerIssueDescription
        holder.txtUrgentRequest!!.text = urgentRequestList.get(position).status
        Glide.with(context).load(urgentRequestList.get(position).imageList!!.get(0)).into(holder.imgUrgentRequest!!)

        if (urgentRequestList.get(position).status.equals("Rejected")) {
            holder.btnRequestAgain!!.visibility = View.VISIBLE
            holder.btnCancel!!.visibility = View.VISIBLE
        } else if (urgentRequestList.get(position).status.equals("Accepted")) {
            holder.btnRequestAgain!!.visibility = View.GONE
            holder.btnCancel!!.visibility = View.GONE
        } else {
            holder.btnRequestAgain!!.visibility = View.GONE
            holder.btnCancel!!.visibility = View.VISIBLE
        }

        holder.btnRequestAgain!!.setOnClickListener {
            FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(urgentRequestList.get(position).key!!).child("status").setValue("Urgent")
                .addOnCompleteListener() {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        holder.btnCancel!!.setOnClickListener {
            FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(urgentRequestList.get(position).key!!).removeValue()
                .addOnCompleteListener() {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Request cancelled", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    override fun getItemCount(): Int {
        return urgentRequestList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgUrgentRequest: ImageView? = null
        var txtUrgentRequestTitle: TextView? = null
        var txtUrgentRequestDescription: TextView? = null
        var txtUrgentRequest: TextView? = null
        var btnRequestAgain: Button? = null
        var btnCancel: Button? = null
        var cardUrgentRequest: MaterialCardView? = null

        init {
            imgUrgentRequest = itemView.findViewById(R.id.imgUrgentRequest) as ImageView
            txtUrgentRequestTitle = itemView.findViewById(R.id.txtUrgentRequestTitle) as TextView
            txtUrgentRequestDescription = itemView.findViewById(R.id.txtUrgentRequestDescription) as TextView
            txtUrgentRequest = itemView.findViewById(R.id.txtUrgentRequest) as TextView
            btnRequestAgain = itemView.findViewById(R.id.btnRequestAgain) as Button
            btnCancel = itemView.findViewById(R.id.btnCancel) as Button
            cardUrgentRequest = itemView.findViewById(R.id.cardUrgentRequest) as MaterialCardView

        }
    }
}