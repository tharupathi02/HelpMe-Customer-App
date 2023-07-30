package com.leoxtech.customerapp.Screens

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.customerapp.Adapter.ImageLargeAdapter
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.Review
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityBookingDetailsBinding
import pl.droidsonroids.gif.GifImageView

class BookingDetails : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailsBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialogLoading: AlertDialog

    val bookingRequestImages = ArrayList<String>()
    private var bookingId: String? = null
    private var customerId: String? = null
    private var garageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        bookingId = intent.getStringExtra("key")
        getBookingDetails(bookingId!!)

        clickListeners()

    }

    private fun getBookingDetails(bookingKey: String) {
        dialogLoading.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (bookingSnapshot in snapshot.children) {
                        if (bookingSnapshot.child("key").value.toString() == bookingKey) {
                            binding.txtBookingTitle.text = bookingSnapshot.child("issueTitle").value.toString()
                            binding.txtBookingDescription.text = bookingSnapshot.child("issueDescription").value.toString()
                            binding.txtBookingDate.text = bookingSnapshot.child("bookingDate").value.toString()
                            binding.txtBookingTime.text = bookingSnapshot.child("bookingTime").value.toString()
                            binding.txtVehicleModel.text = bookingSnapshot.child("vehicleModel").value.toString()
                            binding.txtVehicleNumber.text = bookingSnapshot.child("vehicleNumber").value.toString()

                            if (bookingSnapshot.child("bookingType").value.toString() == "Home Service") {
                                binding.layoutBookingType.visibility = View.VISIBLE
                                binding.layoutCustomerAddress.visibility = View.VISIBLE
                                binding.txtBookingType.text = bookingSnapshot.child("bookingType").value.toString()
                                binding.txtCustomerAddress.text = bookingSnapshot.child("customerAddress").value.toString()
                            } else if (bookingSnapshot.child("bookingType").value.toString() == "Garage Visit") {
                                binding.layoutBookingType.visibility = View.GONE
                                binding.layoutCustomerAddress.visibility = View.GONE
                            }

                            for (imageSnapshot in bookingSnapshot.child("imageList").children) {
                                bookingRequestImages.add(imageSnapshot.value.toString())
                            }
                            binding.recyclerBookingImages.adapter = ImageLargeAdapter(this@BookingDetails, bookingRequestImages)
                            binding.recyclerBookingImages.layoutManager = LinearLayoutManager(this@BookingDetails, LinearLayoutManager.HORIZONTAL, false)
                            binding.recyclerBookingImages.setHasFixedSize(true)

                            if (bookingSnapshot.child("bookingStatus").value.toString() == "Rejected" || bookingSnapshot.child("bookingStatus").value.toString() == "Accepted") {
                                binding.btnCancel.visibility = View.GONE
                                binding.btnReview.visibility = View.GONE
                            } else if (bookingSnapshot.child("bookingStatus").value.toString() == "Completed") {
                                binding.btnCancel.visibility = View.GONE
                                binding.btnReview.visibility = View.VISIBLE
                            }

                            if (bookingSnapshot.child("garageReview").child("0").child("ratingCount").value.toString() == "0"){
                                binding.btnReview.visibility = View.VISIBLE
                            } else {
                                binding.btnReview.visibility = View.GONE
                            }

                            customerId = bookingSnapshot.child("customerId").value.toString()
                            garageId = bookingSnapshot.child("garageId").value.toString()
                            dialogLoading.dismiss()
                            getCustomerDetails(bookingSnapshot.child("customerId").value.toString())
                        }
                    }

                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(this@BookingDetails, "No Bookings Details Found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialogLoading.dismiss()
                Toast.makeText(this@BookingDetails, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCustomerDetails(customerId: String) {
        dbRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (customerSnapshot in snapshot.children) {
                        if (customerSnapshot.child("uid").value.toString() == customerId) {
                            binding.txtCustomerName.text = customerSnapshot.child("name").value.toString()
                            binding.txtMobileNumber.text = customerSnapshot.child("phone").value.toString()
                        }
                    }

                } else {
                    Toast.makeText(this@BookingDetails, "No Customer Details Found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookingDetails, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clickListeners() {
        binding.cardBack.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            dbRef = FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF)
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (bookingSnapshot in snapshot.children) {
                            if (bookingSnapshot.child("key").value.toString() == bookingId) {
                                bookingSnapshot.child("bookingStatus").ref.setValue("Canceled")
                                dialogLoading.dismiss()
                                finish()
                            }
                        }

                    } else {
                        dialogLoading.dismiss()
                        Toast.makeText(this@BookingDetails, "Accepting Failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dialogLoading.dismiss()
                    Toast.makeText(this@BookingDetails, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.btnReview.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Rate the Garage")
            val view = LayoutInflater.from(this).inflate(R.layout.rating_dialog, null)
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
                FirebaseDatabase.getInstance().getReference(Common.GARAGE_REF).child(garageId!!).child("garageReview")
                    .child("0")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                dialogLoading.show()
                                val currentRatingValue = snapshot.child("ratingValue").value.toString().toFloat()
                                val currentRatingCount = snapshot.child("ratingCount").value.toString().toInt()

                                val reviewSum = Review()
                                reviewSum.ratingValue = currentRatingValue + ratingGarage
                                reviewSum.ratingCount = currentRatingCount + 1
                                reviewSum.time = System.currentTimeMillis().toString()

                                val review = Review()
                                review.garageId = garageId
                                review.customerId = customerId
                                reviewSum.customerName = Common.currentUser!!.name!!
                                reviewSum.customerAvatar = Common.currentUser!!.photoURL!!
                                review.ratingValue = ratingGarage
                                review.ratingCount = 1
                                review.comment = txtComment.editText!!.text.toString()
                                reviewSum.time = System.currentTimeMillis().toString()

                                FirebaseDatabase.getInstance().getReference(Common.GARAGE_REF).child(garageId!!)
                                    .child("garageReview").child("0").setValue(reviewSum)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF).child(bookingId!!)
                                                .child("garageReview").child("0").setValue(review)
                                                .addOnCompleteListener {
                                                    FirebaseDatabase.getInstance().getReference(Common.REVIEW_REF).child(garageId!! + "-" + System.currentTimeMillis())
                                                        .child("garageReview").setValue(review)
                                                        .addOnCompleteListener {
                                                            dialogLoading.dismiss()
                                                            ContextCompat.startActivity(this@BookingDetails, Intent(this@BookingDetails, ActivityDoneScreen::class.java)
                                                                    .putExtra("titleText", "Review Added Successfully !")
                                                                    .putExtra("subTitleText", "Thank you for your review. Your review is very important to us to improve our service.\n\nWe hope to see you again ! Have a nice day ! ")
                                                                    .putExtra("buttonText", "Go Back")
                                                                    .putExtra("activity", "GoBack"), null)
                                                        }
                                                }
                                        } else {
                                            dialogLoading.dismiss()
                                            Toast.makeText(this@BookingDetails, "Review failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@BookingDetails, error.message, Toast.LENGTH_SHORT).show()
                        }

                    })
            }

            builder.setView(view)
            val ratingDialog = builder.create()
            ratingDialog.show()

        }

    }

    private fun dialogBox() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setView(R.layout.progress_dialog)
        }.create().also {
            dialogLoading = it
            dialogLoading.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}