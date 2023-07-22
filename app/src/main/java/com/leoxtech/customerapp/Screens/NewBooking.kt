package com.leoxtech.customerapp.Screens

import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
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
import com.leoxtech.customerapp.Model.Booking
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityNewBookingBinding
import kotlin.time.Duration

class NewBooking : AppCompatActivity() {

    private lateinit var binding: ActivityNewBookingBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var imageList: ArrayList<Uri>
    private var imageUri: Uri? = null
    private var garageId: String? = null
    private var bookingType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        imageList = ArrayList()

        dialogBox()

        clickListeners()


    }

    private fun clickListeners() {
        binding.btnSelectGarage.setOnClickListener {
            selectGarageDialog()
        }

        binding.btnSelectDate.setOnClickListener {
            selectDate()
        }

        binding.btnSelectTime.setOnClickListener {
            selectTime()
        }

        binding.btnAttachImage.setOnClickListener {
            selectImage()
        }

        binding.btnBookNow.setOnClickListener {
            bookNow()
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.radioBtnHomeRepair.id) {
                binding.txtCustomerAddress.visibility = View.VISIBLE
                binding.txtCustomerAddress.text = Common.currentUser!!.address
            } else if (checkedId == binding.radioBtnOnSite.id) {
                binding.txtCustomerAddress.visibility = View.GONE
            }
        }

        binding.cardBack.setOnClickListener {
            finish()
        }

    }

    private fun bookNow() {
        if (binding.txtIssueTitle.editText!!.text.toString().isEmpty()) {
            binding.txtIssueTitle.error = "Please enter issue title"
            binding.txtIssueTitle.requestFocus()
            return
        } else if (binding.txtIssueDescription.editText!!.text.toString().isEmpty()) {
            binding.txtIssueTitle.error = null
            binding.txtIssueDescription.error = "Please enter issue title"
            binding.txtIssueDescription.requestFocus()
            return
        } else if (binding.txtVehicleModel.editText!!.text.toString().isEmpty()) {
            binding.txtIssueDescription.error = null
            binding.txtVehicleModel.error = "Please enter vehicle model"
            binding.txtVehicleModel.requestFocus()
            return
        } else if (binding.txtVehicleNumber.editText!!.text.toString().isEmpty()) {
            binding.txtVehicleModel.error = null
            binding.txtVehicleNumber.error = "Please enter vehicle number"
            binding.txtVehicleNumber.requestFocus()
            return
        } else if (binding.txtDate.text.toString().isEmpty()) {
            binding.txtVehicleNumber.error = null
            binding.txtDate.error = "Please select date"
            binding.txtDate.requestFocus()
            return
        } else if (binding.txtTime.text.toString().isEmpty()) {
            binding.txtDate.error = null
            binding.txtTime.error = "Please select time"
            binding.txtTime.requestFocus()
            return
        } else if (binding.txtGarageName.text.toString().isEmpty()) {
            binding.txtTime.error = null
            binding.txtGarageName.error = "Please select garage"
            binding.txtGarageName.requestFocus()
            return
        } else if (imageList.isEmpty()) {
            binding.txtGarageName.error = null
            binding.txtGarageName.error = "Please attach at least one image"
            binding.txtGarageName.requestFocus()
            return
        } else if (binding.radioGroup.checkedRadioButtonId == -1) {
            binding.txtGarageName.error = null
            binding.txtGarageName.error = "Please select booking type"
            binding.txtGarageName.requestFocus()
            return
        } else {
            binding.txtGarageName.error = null
            binding.txtGarageName.error = null
            when (binding.radioGroup.checkedRadioButtonId) {
                binding.radioBtnOnSite.id -> {
                    bookingType = "Garage Visit"
                }
                binding.radioBtnHomeRepair.id -> {
                    bookingType = "Home Service"
                }
            }
            saveBooking()
        }
    }

    private fun saveBooking() {
        dialog.show()
        val firebaseImageList = ArrayList<String>()
        val storageReference = FirebaseStorage.getInstance().reference.child("BookingImages/" + Common.currentUser!!.uid + "-" + System.currentTimeMillis())
        for (i in 0 until imageList.size) {
            val image = imageList[i]
            val imageRef = storageReference.child("BookingImages-" + System.currentTimeMillis())
            imageRef.putFile(image).addOnCompleteListener() { _ ->
                imageRef.downloadUrl.addOnSuccessListener {uri->
                    firebaseImageList.add(uri.toString())
                    if (firebaseImageList.size == imageList.size) {
                        dialog.dismiss()
                        val bookingModel = Booking()
                        val keyRef = Common.currentUser!!.uid + System.currentTimeMillis()

                        bookingModel.key = keyRef
                        bookingModel.customerId = Common.currentUser!!.uid
                        bookingModel.garageId = garageId
                        bookingModel.issueTitle = binding.txtIssueTitle.editText!!.text.toString()
                        bookingModel.issueDescription = binding.txtIssueDescription.editText!!.text.toString()
                        bookingModel.vehicleModel = binding.txtVehicleModel.editText!!.text.toString()
                        bookingModel.vehicleNumber = binding.txtVehicleNumber.editText!!.text.toString()
                        bookingModel.bookingDate = binding.txtDate.text.toString()
                        bookingModel.bookingTime = binding.txtTime.text.toString()
                        bookingModel.imageList = firebaseImageList
                        bookingModel.bookingStatus = "Pending"
                        bookingModel.bookingType = bookingType

                        if (bookingType == "Garage Visit") {
                            bookingModel.customerAddress = ""
                        } else {
                            bookingModel.customerAddress = Common.currentUser!!.address
                        }

                        dbRef = FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF)
                        dbRef.child(keyRef).setValue(bookingModel).addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                dialog.dismiss()
                                startActivity(Intent(this, ActivityDoneScreen::class.java)
                                    .putExtra("titleText", "Booking Request Sent")
                                    .putExtra("subTitleText", "Please wait for garage to accept your booking request or they will contact you soon for further details about your issue and booking.")
                                    .putExtra("buttonText", "Go back to Home")
                                    .putExtra("activity", "HomeActivity"), null)
                            } else {
                                Snackbar.make(binding.root, "Failed to send booking request", Snackbar.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        }

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

    private fun selectImage() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    private fun selectTime() {
        val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(10)
                .setTitleText("Select Time of Booking")
                .build()
        timePicker.show(supportFragmentManager, "Time Picker")

        timePicker.addOnPositiveButtonClickListener {
            binding.txtTime.text = "${if (timePicker.hour > 12) timePicker.hour - 12 else timePicker.hour}:${timePicker.minute} ${if (timePicker.hour > 12) "PM" else "AM"}"
        }
    }

    private fun selectDate() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Booking")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()
        datePicker.show(supportFragmentManager, "Date Picker")

        datePicker.addOnPositiveButtonClickListener {
            binding.txtDate.text = datePicker.headerText
        }
    }

    private fun selectGarageDialog() {
        dialog.show()
        dbRef = Firebase.database.reference.child(Common.GARAGE_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val garageNameSpinner = ArrayList<String>()
                        garageNameSpinner.add("Select Garage")
                        garageNameSpinner.add(data.child("companyName").value.toString())

                        val builder = MaterialAlertDialogBuilder(this@NewBooking)
                        val view = LayoutInflater.from(this@NewBooking).inflate(R.layout.select_garage_dialog, null)
                        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
                        val txtGarageName = view.findViewById<TextView>(R.id.txtGarageName)
                        val spinnerGarage = view.findViewById<Spinner>(R.id.spinnerGarage)
                        val txtWorkingHours = view.findViewById<TextView>(R.id.txtWorkingHours)
                        val txtWorkingVehicles = view.findViewById<TextView>(R.id.txtWorkingVehicles)

                        val adapterSpinner = ArrayAdapter(this@NewBooking, android.R.layout.simple_spinner_dropdown_item, garageNameSpinner)
                        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerGarage.adapter = adapterSpinner

                        spinnerGarage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                if (position == 0) {
                                    txtGarageName.text = ""
                                    txtWorkingHours.text = ""
                                    txtWorkingVehicles.text = ""
                                    ratingBar.rating = 0.0f
                                } else {
                                    txtGarageName.text = data.child("companyName").value.toString()
                                    garageId = data.child("uid").value.toString()
                                    txtWorkingHours.text = data.child("workingHours").value.toString()
                                    txtWorkingVehicles.text = data.child("workingVehicleTypes").value.toString()
                                    ratingBar.rating = data.child("garageReview").child("0").child("ratingValue").value.toString().toFloat() / data.child("garageReview").child("0").child("ratingCount").value.toString().toFloat()
                                    ratingBar.rating = Common.ratingCalculate(data.child("garageReview").child("0").child("ratingValue").value.toString().toFloat(), data.child("garageReview").child("0").child("ratingCount").value.toString().toFloat())
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                txtGarageName.text = ""
                                txtWorkingHours.text = ""
                                txtWorkingVehicles.text = ""
                                ratingBar.rating = 0.0f
                            }
                        }

                        builder.setPositiveButton("Select") { dialog, which ->
                            if (spinnerGarage.selectedItemPosition != 0) {
                                binding.txtGarageName.text = spinnerGarage.selectedItem.toString()
                                binding.ratingBar.rating = ratingBar.rating
                            } else {
                                Toast.makeText(this@NewBooking, "Please select a garage", Toast.LENGTH_SHORT).show()
                            }
                        }

                        builder.setNegativeButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }

                        builder.setView(view)
                        builder.setCancelable(false)
                        val selectGarageDialog = builder.create()
                        selectGarageDialog.show()
                        dialog.dismiss()

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)
        if (resultCode == RESULT_OK) {
            if (imageData!!.data != null) {
                imageList.add(imageData.data!!)
                binding.recyclerViewImages.adapter = ImageAdapter(this, imageList)
                binding.recyclerViewImages.layoutManager = LinearLayoutManager(this@NewBooking, LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerViewImages.setHasFixedSize(true)
            } else {
                Snackbar.make(binding.root, "No image selected", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }

    }
}