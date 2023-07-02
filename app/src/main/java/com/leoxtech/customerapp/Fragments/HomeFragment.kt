package com.leoxtech.customerapp.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.leoxtech.customerapp.databinding.ActivityMainBinding
import com.leoxtech.customerapp.databinding.FragmentHomeBinding
import java.io.IOException
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.cardLocation.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    private fun showBottomSheetDialog() {
        dialog.show()
        val locationResult = LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
        val view: View = layoutInflater.inflate(R.layout.dashboard_current_location_view, null)
        val bottomSheetDialog = BottomSheetDialog(context!!)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        if (bottomSheetDialog.isShowing) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
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

                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            try {
                                val addresses = geocoder.getFromLocation(lastKnownLocation.latitude, lastKnownLocation.longitude, 1)
                                val address = addresses!![0].getAddressLine(0)
                                val city = addresses[0].locality
                                val state = addresses[0].adminArea
                                val country = addresses[0].countryName
                                val postalCode = addresses[0].postalCode
                                val knownName = addresses[0].featureName
                                val txtCurrentLocationText = view.findViewById<TextView>(R.id.txtCurrentLocationText)
                                txtCurrentLocationText.text = "Address: $address\nCity: $city\nState: $state\nCountry: $country\nPostal Code: $postalCode\nKnown Name: $knownName"
                                dialog.dismiss()
                            } catch (e: IOException) {
                                Snackbar.make(requireView(), "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }
        } else {
            bottomSheetDialog.dismiss()
            dialog.dismiss()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
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
                    binding.recyclerviewTopGarages.adapter = PopularGarageAdapter(context!!, popularArrayList!!)
                    binding.recyclerviewTopGarages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    dialog.dismiss()

                    if (popularArrayList.size > 0) {
                        binding.txtNoGaragesFound.visibility = View.GONE
                    } else {
                        binding.txtNoGaragesFound.visibility = View.VISIBLE
                    }

                }else{
                    Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun profileInfo() {
        dialog.show()
        if (Common.currentUser != null) {
            binding.txtCustomerName.text = Common.currentUser!!.name
            if (Common.currentUser!!.photoURL != null) {
                Glide.with(this).load(Common.currentUser!!.photoURL).into(binding.imgAvatar)
                dialog.dismiss()
            } else {
                binding.imgAvatar.setImageResource(R.drawable.avatar)
                dialog.dismiss()
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

                val singleAddress = getAddressFromLatLng(task.result!!.latitude, task.result!!.longitude)

                binding.txtLocation.text = singleAddress.toString()
            }
        dialog.dismiss()
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): Any {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
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
        AlertDialog.Builder(context).apply {
            setCancelable(false)
            setView(R.layout.progress_dialog)
        }.create().also {
            dialog = it
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

}