package com.leoxtech.customerapp.Screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.GarageModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivitySelectGarageBinding

class SelectGarage : AppCompatActivity() {

    private lateinit var binding: ActivitySelectGarageBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mMap: GoogleMap
    private lateinit var dialog: AlertDialog

    private var locationsArrayList = ArrayList<LatLng>()
    private var locationsUIdArrayList = ArrayList<String>()

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectGarageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
        }

        dialogBox()

        clickListeners()

        getLocationsFromFirebase()
    }

    private fun clickListeners() {
        binding.cardBack.setOnClickListener {
            finish()
        }
    }

    private fun getLocationsFromFirebase() {
        dialog.show()
        dbRef = Firebase.database.reference.child(Common.GARAGE_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                locationsArrayList.clear()
                locationsUIdArrayList.clear()
                mMap.clear()
                if (snapshot.exists()) {
                    for (i in snapshot.children) {
                        val lat = i.child("latitude").value.toString().toDouble()
                        val lng = i.child("longitude").value.toString().toDouble()
                        val uID = i.child("uid").value.toString()
                        val location = LatLng(lat, lng)
                        locationsArrayList.add(location)
                        locationsUIdArrayList.add(uID)
                    }
                    showLocations()
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun showLocations() {
        dialog.show()
        val locationResult = LocationServices.getFusedLocationProviderClient(this).lastLocation
        locationResult.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val lastKnownLocation = task.result
                if (lastKnownLocation != null) {
                    val latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.uiSettings.isZoomControlsEnabled = true
                    mMap.uiSettings.isZoomGesturesEnabled = true
                    mMap.uiSettings.isScrollGesturesEnabled = true
                    mMap.uiSettings.isTiltGesturesEnabled = true
                    mMap.uiSettings.isRotateGesturesEnabled = true
                    mMap.uiSettings.isCompassEnabled = true
                    mMap.uiSettings.isMapToolbarEnabled = true
                    mMap.uiSettings.isRotateGesturesEnabled = true
                    mMap.uiSettings.isTiltGesturesEnabled = true
                    mMap.isIndoorEnabled = true
                    mMap.projection.visibleRegion.latLngBounds
                    mMap.isBuildingsEnabled = true
                    mMap.isTrafficEnabled = true
                    mMap.isMyLocationEnabled = true


                    for (i in locationsArrayList.indices) {
                        mMap.addMarker(MarkerOptions().position(locationsArrayList[i]).icon(bitmapDescriptor(this, R.drawable.location_3d)))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationsArrayList[i]))
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))

                    mMap.setOnMarkerClickListener {
                        getCompanyDetails(locationsUIdArrayList[locationsArrayList.indexOf(it.position)])
                        true
                    }

                    dialog.dismiss()

                }
            }
        }
        dialog.dismiss()
        dialog.dismiss()
    }

    private fun bitmapDescriptor(selectGarage: SelectGarage, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(selectGarage, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getCompanyDetails(uID: String) {
        dialog.show()
        dbRef = Firebase.database.reference.child(Common.GARAGE_REF).child(uID)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val view: View = layoutInflater.inflate(R.layout.garage_details_bottom_sheet, null)
                    val bottomSheetDialog = BottomSheetDialog(this@SelectGarage)
                    bottomSheetDialog.setContentView(view)
                    val txtGarageName = view.findViewById<TextView>(R.id.txtGarageName)
                    val imgGarage = view.findViewById<ImageView>(R.id.imgGarage)
                    val txtGarageDescription = view.findViewById<TextView>(R.id.txtGarageDescription)
                    val txtGarageWorkingHours = view.findViewById<TextView>(R.id.txtGarageWorkingHours)
                    val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
                    val btnRequestHelp = view.findViewById<Button>(R.id.btnRequestHelp)
                    txtGarageName.text = snapshot.child("companyName").value.toString()
                    txtGarageDescription.text = snapshot.child("description").value.toString()
                    txtGarageWorkingHours.text = snapshot.child("workingHours").value.toString()
                    Glide.with(this@SelectGarage).load(snapshot.child("photoURL").value.toString()).into(imgGarage)
                    ratingBar.rating = snapshot.child("garageReview").child("0").child("ratingValue").value.toString().toFloat()
                    btnRequestHelp.setOnClickListener {
                        Common.selectedGarage = snapshot.getValue(GarageModel::class.java)
                        startActivity(Intent(this@SelectGarage, RequestHelp::class.java))
                    }
                    bottomSheetDialog.show()
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
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