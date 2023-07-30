package com.leoxtech.customerapp.Screens

import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.customerapp.Adapter.GarageReviewAdapter
import com.leoxtech.customerapp.Adapter.PopularGarageAdapter
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.GarageModel
import com.leoxtech.customerapp.Model.Review
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityGarageViewBinding

class GarageView : AppCompatActivity() {

    private lateinit var binding: ActivityGarageViewBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var garageReview: ArrayList<Review>

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var garageContact: String = ""
    private var garageId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGarageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        clickListeners()

        garageReview = arrayListOf<Review>()
        garageId = intent.getStringExtra("garageId").toString()
        getGarageDetails(garageId!!)
    }

    private fun clickListeners() {
        binding.cardBack.setOnClickListener {
            finish()
        }

        binding.cardContactGarage.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL);
            intent.data = Uri.parse("tel:$garageContact")
            startActivity(intent)
        }

        binding.cardNewBooking.setOnClickListener {
            val intent = Intent(this, NewBooking::class.java)
            startActivity(intent)
        }
    }

    private fun getGarageDetails(garageId: String) {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.GARAGE_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (garageSnapshot in snapshot.children) {
                        val garage = garageSnapshot.getValue(GarageModel::class.java)
                        if (garage!!.uid == garageId) {
                            binding.txtGarageName.text = garage.companyName
                            binding.txtGarageDescription.text = garage.description
                            Glide.with(this@GarageView).load(garage.photoURL).into(binding.imgGarage)
                            binding.txtGarageWorkingHours.text = garage.workingHours
                            binding.txtGarageVechicles.text = garage.workingVehicleTypes

                            binding.ratingBar.rating = garage.garageReview!!.get(0).ratingValue!!.toFloat()
                            binding.txtGarageRating.text = String.format("%.2f", Common.ratingCalculate(garage.garageReview!!.get(0).ratingValue!!.toFloat(), garage.garageReview!!.get(0).ratingCount!!.toFloat()))

                            latitude = garage.latitude!!.toDouble()
                            longitude = garage.longitude!!.toDouble()
                            garageContact = garage.phone!!

                            getGarageReview()

                            dialog.dismiss()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_SHORT).show()
            }

        })
    }

    private fun getGarageReview() {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.REVIEW_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    garageReview.clear()
                    for (reviewSnapshot in snapshot.children) {
                        val review = reviewSnapshot.child("garageReview").getValue(Review::class.java)
                        if (review!!.garageId == garageId) {
                            garageReview.add(review)
                        }
                    }

                    if (garageReview.size > 0) {
                        binding.reviewRecycler.adapter = GarageReviewAdapter(this@GarageView, garageReview)
                        binding.reviewRecycler.layoutManager = LinearLayoutManager(this@GarageView, LinearLayoutManager.VERTICAL, false)
                        dialog.dismiss()
                    } else {
                        binding.txtNotFound.visibility = View.VISIBLE
                    }

                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_SHORT).show()
            }

        })
    }


    private fun dialogBox() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setView(R.layout.progress_dialog)
        }.create().also {
            dialog = it
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}