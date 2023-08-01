package com.leoxtech.customerapp.Screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.customerapp.Adapter.PopularGarageAdapter
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.GarageModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityHomeBinding
import java.io.IOException
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    private lateinit var popularArrayList: ArrayList<GarageModel>

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        initLocation()

        profileInfo()

        clickListeners()

        popularArrayList = arrayListOf<GarageModel>()
        getTopGarageData()

    }

    @SuppressLint("MissingPermission")
    private fun clickListeners() {
        binding.cardRequestHelp.setOnClickListener {
            startActivity(Intent(this, SelectGarage::class.java))
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, MyProfile::class.java))
        }

        binding.cardMyEmergency.setOnClickListener {
            startActivity(Intent(this, MyEmergencyActivity::class.java))
        }

        binding.cardLocation.setOnClickListener {
            startActivity(Intent(this, CurrentLocationView::class.java))
        }

        binding.cardMakeBooking.setOnClickListener {
            startActivity(Intent(this, NewBooking::class.java))
        }

        binding.cardMyBooking.setOnClickListener {
            startActivity(Intent(this, MyBookings::class.java))
        }

        binding.cardNearbyGarage.setOnClickListener {
            startActivity(Intent(this, NearbyGarage::class.java))
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                currentLocation = p0!!.lastLocation!!
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)
    }

    private fun getTopGarageData() {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.GARAGE_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                popularArrayList.clear()
                if (snapshot.exists()) {
                    for (popularSnapshot in snapshot.children) {
                        val popular = popularSnapshot.getValue(GarageModel::class.java)
                        popularArrayList.add(popular!!)
                    }

                    //sort by rating value, highest to lowest (descending) using bubble sort
                    for (i in 0 until popularArrayList.size - 1) {
                        for (j in 0 until popularArrayList.size - 1) {
                            if (popularArrayList[j].garageReview!!.get(0).ratingValue!! < popularArrayList[j + 1].garageReview!!.get(0).ratingValue!!.toFloat()) {
                                val temp = popularArrayList[j]
                                popularArrayList[j] = popularArrayList[j + 1]
                                popularArrayList[j + 1] = temp
                            }
                        }
                    }

                    if (popularArrayList.isNotEmpty()) {
                        binding.recyclerviewTopGarages.adapter = PopularGarageAdapter(this@HomeActivity, popularArrayList!!)
                        binding.recyclerviewTopGarages.layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.txtNoGaragesFound.visibility = View.GONE
                        dialog.dismiss()
                    } else {
                        binding.txtNoGaragesFound.visibility = View.VISIBLE
                    }

                }else{
                    binding.txtNoGaragesFound.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }
        })
    }

    @SuppressLint("MissingPermission", "UseRequireInsteadOfGet")
    private fun profileInfo() {
        dialog.show()
        if (Common.currentUser != null) {
            binding.txtCustomerName.text = Common.currentUser!!.name
            if (Common.currentUser!!.photoURL!!.isNotBlank()) {
                binding.imgAvatar.visibility = View.VISIBLE
                Glide.with(this).load(Common.currentUser!!.photoURL).into(binding.imgAvatar)
                dialog.dismiss()
            } else {
                binding.cardAvatar.setCardBackgroundColor(Color.parseColor("#FBC146"))
                binding.imgAvatar.visibility = View.GONE
                binding.txtAvatar.visibility = View.VISIBLE
                binding.txtAvatar.text = Common.currentUser!!.firstName!!.substring(0, 1) + Common.currentUser!!.lastName!!.substring(0, 1)
            }
        }

        dialog.show()
        fusedLocationProviderClient!!.lastLocation
            .addOnFailureListener { e ->
                binding.txtLocation.text = "Location not found"
                Snackbar.make(binding.root, e.message!!, Snackbar.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { task ->
                latitude = task.result!!.latitude
                longitude = task.result!!.longitude

                val geoCoder = Geocoder(this, Locale.getDefault())
                val  result : String?=null
                try {
                    val addressList = geoCoder.getFromLocation(latitude, longitude, 1)
                    if (addressList != null && addressList.size > 0) {
                        val address = addressList[0]
                        val sb = StringBuilder(address.getAddressLine(0))
                        binding.txtLocation.text = sb.toString()
                    } else {
                        binding.txtLocation.text = "Address not found"
                    }
                } catch (e: IOException) {
                    binding.txtLocation.text = e.message!!
                }
            }
        dialog.dismiss()
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): Any {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val  result : String?=null
        try {
            val addressList = geoCoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.size > 0) {
                val address = addressList[0]
                val sb = StringBuilder(address.getAddressLine(0))
                return sb.toString()
            } else {
                return "Address not found"
            }
        } catch (e: IOException) {
            return e.message!!
        }
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