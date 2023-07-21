package com.leoxtech.customerapp.Screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.leoxtech.customerapp.Adapter.ImageAdapter
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.RequestHelpModel
import com.leoxtech.customerapp.Model.Review
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityRequestHelpBinding
import java.io.IOException
import java.util.Locale

class RequestHelp : AppCompatActivity() {

    private lateinit var binding: ActivityRequestHelpBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var garageUserId: String? = null

    private val PICK_IMAGE = 1
    private lateinit var imageList: ArrayList<Uri>
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        imageList = ArrayList()

        binding.txtGarageName.text = Common.selectedGarage!!.name
        binding.ratingBar.rating = Common.selectedGarage!!.garageReview!!.get(0)!!.ratingValue!!.toFloat()
        garageUserId = Common.selectedGarage!!.uid

        dialogBox()

        clickListeners()

        getUserCurrentLocation()

    }

    @SuppressLint("MissingPermission")
    private fun getUserCurrentLocation() {
        dialog.show()
        initLocation()

        fusedLocationProviderClient!!.lastLocation
            .addOnFailureListener { e ->
                binding.txtLocation.text = "Location not found"
                Snackbar.make(binding.root, e.message!!, Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .addOnCompleteListener { task ->
                latitude = task.result!!.latitude
                longitude = task.result!!.longitude

                val singleAddress = getAddressFromLatLng(task.result!!.latitude, task.result!!.longitude)

                binding.txtLocation.text = singleAddress.toString()
                dialog.dismiss()
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

    private fun clickListeners() {
        binding.btnAttachImage.setOnClickListener {
            ImagePicker.with(this@RequestHelp).crop().compress(1024).maxResultSize(1080, 1080).start()
        }

        binding.cardBack.setOnClickListener {
            finish()
        }

        binding.btnCheckLocation.setOnClickListener {
            startActivity(Intent(this, CurrentLocationView::class.java))
        }

        binding.btnRequestHelp.setOnClickListener {
            if (binding.txtIssueTitle.editText!!.text.toString().isEmpty()) {
                binding.txtIssueTitle.error = "Please enter issue title"
                binding.txtIssueTitle.requestFocus()
            } else if (binding.txtIssueDescription.editText!!.text.toString().isEmpty()) {
                binding.txtIssueDescription.error = "Please enter issue description"
                binding.txtIssueDescription.requestFocus()
            } else if (binding.txtVehicleModel.editText!!.text.toString().isEmpty()) {
                binding.txtVehicleModel.error = "Please enter vehicle model"
                binding.txtVehicleModel.requestFocus()
            } else if (imageList.isEmpty()) {
                Snackbar.make(binding.root, "Please attach images", Snackbar.LENGTH_SHORT).show()
            } else if (binding.txtLocation.text.toString().isEmpty()) {
                Snackbar.make(binding.root, "Please check your location", Snackbar.LENGTH_SHORT).show()
            } else {
                requestHelp()
            }
        }
    }

    private fun requestHelp() {
        dialog.show()
        val firebaseImageList = ArrayList<String>()

        val storageReference = FirebaseStorage.getInstance().reference.child("HelpImages/" + garageUserId + "-" + System.currentTimeMillis())
        for (i in 0 until imageList.size) {
            val image = imageList[i]
            val imageRef = storageReference.child("HelpImages-" + System.currentTimeMillis())
            imageRef.putFile(image).addOnCompleteListener() { _ ->
                imageRef.downloadUrl.addOnSuccessListener {uri->
                    firebaseImageList.add(uri.toString())
                    if (firebaseImageList.size == imageList.size) {
                        dialog.dismiss()
                        saveRequestToDb(firebaseImageList)
                    }
                }
            }.addOnProgressListener {taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                val progressText = dialog.findViewById<TextView>(R.id.txtProgress)
                progressText.visibility = View.VISIBLE
                progressText.text = "Updating... " + progress.toInt() + "%"
            }
        }

    }

    private fun saveRequestToDb(firebaseImageList: ArrayList<String>) {
        dialog.show()
        val requestHelpModel = RequestHelpModel()
        val review = Review()
        val keyRef = garageUserId + System.currentTimeMillis()
        requestHelpModel.garageUid = garageUserId
        requestHelpModel.customerUid = Common.currentUser!!.uid
        requestHelpModel.customerName = Common.currentUser!!.firstName + " " + Common.currentUser!!.lastName
        requestHelpModel.customerPhone = Common.currentUser!!.phone
        requestHelpModel.latitude = latitude
        requestHelpModel.longitude = longitude
        requestHelpModel.status = "Urgent"
        requestHelpModel.imageList = firebaseImageList
        requestHelpModel.customerIssueTitle = binding.txtIssueTitle.editText!!.text.toString()
        requestHelpModel.customerIssueDescription = binding.txtIssueDescription.editText!!.text.toString()
        requestHelpModel.customerVehicle = binding.txtVehicleModel.editText!!.text.toString()
        requestHelpModel.timeStamp = System.currentTimeMillis().toString()
        requestHelpModel.key = keyRef

        review.customerId = ""
        review.garageId = garageUserId
        review.ratingValue = 0.0.toFloat()
        review.ratingCount = 0
        review.comment = ""

        requestHelpModel.garageReview = listOf(review)

        dbRef = FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF)
        dbRef.child(keyRef).setValue(requestHelpModel).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                dialog.dismiss()
                startActivity(Intent(this, ActivityDoneScreen::class.java)
                    .putExtra("titleText", "Request sent Successfully.")
                    .putExtra("subTitleText", "Please wait for garage to accept your request or they will contact you soon for further details about your issue and location details.")
                    .putExtra("buttonText", "Go back to Home")
                    .putExtra("activity", "HomeActivity"), null)
            } else {
                Snackbar.make(binding.root, "Failed to send request. Please try again later.", Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun pickImagesFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)
        if (resultCode == RESULT_OK) {
            if (imageData!!.data != null) {
                imageList.add(imageData.data!!)
                binding.recyclerViewImages.adapter = ImageAdapter(this, imageList)
                binding.recyclerViewImages.layoutManager = LinearLayoutManager(this@RequestHelp, LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerViewImages.setHasFixedSize(true)
            } else {
                Snackbar.make(binding.root, "No image selected", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show()
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
