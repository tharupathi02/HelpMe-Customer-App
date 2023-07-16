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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.RequestHelpModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.Screens.ActivityDoneScreen
import pl.droidsonroids.gif.GifImageView

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
            holder.btnReview!!.visibility = View.GONE
        } else if (urgentRequestList.get(position).status.equals("Accepted")) {
            holder.btnRequestAgain!!.visibility = View.GONE
            holder.btnCancel!!.visibility = View.GONE
            holder.btnReview!!.visibility = View.GONE
        } else if (urgentRequestList.get(position).status.equals("Completed")) {
            holder.btnRequestAgain!!.visibility = View.GONE
            holder.btnCancel!!.visibility = View.GONE
            holder.btnReview!!.visibility = View.VISIBLE
        } else {
            holder.btnRequestAgain!!.visibility = View.GONE
            holder.btnCancel!!.visibility = View.VISIBLE
            holder.btnReview!!.visibility = View.GONE
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

        if (urgentRequestList.get(position).garageReviewValue == 0.0.toFloat() && urgentRequestList.get(position).status.equals("Completed")) {
            holder.btnReview!!.visibility = View.VISIBLE
            holder.btnReview?.setOnClickListener {
                val builder = MaterialAlertDialogBuilder(context)
                builder.setTitle("Rate the Garage")
                val view = LayoutInflater.from(context).inflate(R.layout.rating_dialog, null)
                val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
                val txtComment = view.findViewById<TextInputLayout>(R.id.txtComment)
                val emo_1 = view.findViewById<GifImageView>(R.id.emo_1)
                val emo_2 = view.findViewById<GifImageView>(R.id.emo_2)
                val emo_3 = view.findViewById<GifImageView>(R.id.emo_3)
                val emo_4 = view.findViewById<GifImageView>(R.id.emo_4)
                val emo_5 = view.findViewById<GifImageView>(R.id.emo_5)

                var ratingGarage = 0.0.toFloat()

                ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                    ratingGarage = rating
                    if (rating > 0.0.toFloat() && rating <= 1.0.toFloat()) {
                        emo_1.alpha = 1.0.toFloat()
                        emo_2.alpha = 0.5.toFloat()
                        emo_3.alpha = 0.5.toFloat()
                        emo_4.alpha = 0.5.toFloat()
                        emo_5.alpha = 0.5.toFloat()
                    } else if (rating > 1.0.toFloat() && rating <= 2.0.toFloat()) {
                        emo_1.alpha = 0.5.toFloat()
                        emo_2.alpha = 1.0.toFloat()
                        emo_3.alpha = 0.5.toFloat()
                        emo_4.alpha = 0.5.toFloat()
                        emo_5.alpha = 0.5.toFloat()
                    } else if (rating > 2.0.toFloat() && rating <= 3.0.toFloat()) {
                        emo_1.alpha = 0.5.toFloat()
                        emo_2.alpha = 0.5.toFloat()
                        emo_3.alpha = 1.0.toFloat()
                        emo_4.alpha = 0.5.toFloat()
                        emo_5.alpha = 0.5.toFloat()
                    } else if (rating > 3.0.toFloat() && rating <= 4.0.toFloat()) {
                        emo_1.alpha = 0.5.toFloat()
                        emo_2.alpha = 0.5.toFloat()
                        emo_3.alpha = 0.5.toFloat()
                        emo_4.alpha = 1.0.toFloat()
                        emo_5.alpha = 0.5.toFloat()
                    } else if (rating > 4.0.toFloat() && rating <= 5.0.toFloat()) {
                        emo_1.alpha = 0.5.toFloat()
                        emo_2.alpha = 0.5.toFloat()
                        emo_3.alpha = 0.5.toFloat()
                        emo_4.alpha = 0.5.toFloat()
                        emo_5.alpha = 1.0.toFloat()
                    }
                }

                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.setPositiveButton("Add Review") { dialog, _ ->
                    val review = HashMap<String, Any>()
                    review.put("garageUid", urgentRequestList.get(position).garageUid!!)
                    review.put("customerUid", urgentRequestList.get(position).customerUid!!)
                    review.put("ratingValue", ratingGarage)
                    review.put("comment", txtComment.editText!!.text.toString())

                    FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(urgentRequestList.get(position).key!!)
                        .child("garageReview").setValue(review)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(urgentRequestList.get(position).key!!).child("garageReviewValue").setValue(ratingGarage)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            dialog.dismiss()
                                            startActivity(context, Intent(context, ActivityDoneScreen::class.java)
                                                .putExtra("titleText", "Review Added Successfully !")
                                                .putExtra("subTitleText", "Thank you for your review. Your review is very important to us to improve our service.\n\nWe hope to see you again ! Have a nice day ! ")
                                                .putExtra("buttonText", "Go Back")
                                                .putExtra("activity", "GoBack"), null)
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Review failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                builder.setView(view)

                val ratingDialog = builder.create()
                ratingDialog.show()
            }
        } else {
            holder.btnReview!!.visibility = View.GONE
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
        var btnReview: Button? = null
        var cardUrgentRequest: MaterialCardView? = null

        init {
            imgUrgentRequest = itemView.findViewById(R.id.imgUrgentRequest) as ImageView
            txtUrgentRequestTitle = itemView.findViewById(R.id.txtUrgentRequestTitle) as TextView
            txtUrgentRequestDescription = itemView.findViewById(R.id.txtUrgentRequestDescription) as TextView
            txtUrgentRequest = itemView.findViewById(R.id.txtUrgentRequest) as TextView
            btnRequestAgain = itemView.findViewById(R.id.btnRequestAgain) as Button
            btnCancel = itemView.findViewById(R.id.btnCancel) as Button
            btnReview = itemView.findViewById(R.id.btnReview) as Button
            cardUrgentRequest = itemView.findViewById(R.id.cardUrgentRequest) as MaterialCardView

        }
    }
}