package com.leoxtech.customerapp.Screens

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        clickListeners()

        dialogBox()

        profileInfo()
    }

    private fun profileInfo() {
        dialog.show()
        if (Common.currentUser != null) {
            binding.txtFullName.text = Common.currentUser!!.name
            if (Common.currentUser!!.photoURL != null) {
                Glide.with(this).load(Common.currentUser!!.photoURL).into(binding.imgProfile)
                dialog.dismiss()
            } else {
                binding.imgProfile.setImageResource(R.drawable.avatar)
                dialog.dismiss()
            }
            binding.txtEmail.editText?.setText(Common.currentUser!!.email)
            binding.txtContactNumber.editText?.setText(Common.currentUser!!.phone)
            binding.txtAddress.editText?.setText(Common.currentUser!!.address)
            binding.txtIDNumber.editText?.setText(Common.currentUser!!.idNumber)
        }
        dialog.dismiss()

    }

    private fun clickListeners() {
        binding.cardBack.setOnClickListener {
            finish()
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