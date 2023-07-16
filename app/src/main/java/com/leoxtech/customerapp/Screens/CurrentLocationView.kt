package com.leoxtech.customerapp.Screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.leoxtech.customerapp.Model.GarageModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityCurrentLocationViewBinding
import java.io.IOException
import java.util.Locale

class CurrentLocationView : AppCompatActivity() {

    private lateinit var binding: ActivityCurrentLocationViewBinding

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
        binding = ActivityCurrentLocationViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        initLocation()

        showCurrentLocation()

        binding.cardBack.setOnClickListener {
            finish()
        }

    }

    @SuppressLint("MissingPermission")
    private fun showCurrentLocation() {
        dialog.show()
        val locationResult = LocationServices.getFusedLocationProviderClient(this).lastLocation
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        val latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        googleMap.isMyLocationEnabled = true
                        googleMap.uiSettings.isMyLocationButtonEnabled = true
                        googleMap.uiSettings.isZoomControlsEnabled = true
                        googleMap.uiSettings.isZoomGesturesEnabled = true
                        googleMap.uiSettings.isScrollGesturesEnabled = true
                        googleMap.uiSettings.isTiltGesturesEnabled = true
                        googleMap.uiSettings.isRotateGesturesEnabled = true
                        googleMap.uiSettings.isCompassEnabled = true
                        googleMap.uiSettings.isMapToolbarEnabled = true

                        val geocoder = Geocoder(this, Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(lastKnownLocation.latitude, lastKnownLocation.longitude, 1)
                            val address = addresses!![0].getAddressLine(0)
                            val city = addresses[0].locality
                            val state = addresses[0].adminArea
                            val country = addresses[0].countryName
                            val postalCode = addresses[0].postalCode
                            val knownName = addresses[0].featureName
                            binding.txtCurrentLocationText.text = "Address: $address\nCity: $city\nState: $state\nCountry: $country\nPostal Code: $postalCode\nKnown Name: $knownName"
                            dialog.dismiss()
                        } catch (e: IOException) {
                            Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                    }
                }
            }
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