package com.leoxtech.customerapp.Screens

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.customerapp.Adapter.MyBookingsAdapter
import com.leoxtech.customerapp.Adapter.UrgentRequestAdapter
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.Booking
import com.leoxtech.customerapp.Model.RequestHelpModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityMyBookingsBinding

class MyBookings : AppCompatActivity() {

    private lateinit var binding: ActivityMyBookingsBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var bookingArrayList: ArrayList<Booking>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        bookingArrayList = arrayListOf<Booking>()
        getBookings()

        binding.cardBack.setOnClickListener {
            finish()
        }

    }

    private fun getBookings() {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    bookingArrayList.clear()
                    for (bookingSnapshot in snapshot.children) {
                        if (bookingSnapshot.child("customerId").value.toString() == Common.currentUser!!.uid) {
                            val booking = bookingSnapshot.getValue(Booking::class.java)
                            bookingArrayList.add(booking!!)
                        }
                    }
                    bookingArrayList.reverse()
                    if (bookingArrayList.isNotEmpty()) {
                        binding.recyclerMyBookings.adapter = MyBookingsAdapter(this@MyBookings, bookingArrayList!!)
                        binding.recyclerMyBookings.layoutManager = LinearLayoutManager(this@MyBookings, LinearLayoutManager.VERTICAL, false)
                        binding.txtNotFound.visibility = View.GONE
                        dialog.dismiss()
                    } else {
                        dialog.dismiss()
                        binding.txtNotFound.visibility = View.VISIBLE
                    }
                } else {
                    dialog.dismiss()
                    Toast.makeText(this@MyBookings, "No Bookings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Toast.makeText(this@MyBookings, error.message, Toast.LENGTH_SHORT).show()
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