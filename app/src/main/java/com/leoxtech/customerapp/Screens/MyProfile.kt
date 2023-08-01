package com.leoxtech.customerapp.Screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityMyprofileBinding
import com.leoxtech.customerapp.databinding.ActivityProfileBinding

class MyProfile : AppCompatActivity() {

    private lateinit var binding: ActivityMyprofileBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        dialog.show()

        profileInfo()

        clickListeners()

    }

    private fun clickListeners() {
        binding.cardLogOut.setOnClickListener {
            signOut()
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.cardUpdateProfile.setOnClickListener {
            startActivity(Intent(this, UpdateProfile::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun profileInfo() {
        dialog.show()
        if (Common.currentUser != null) {
            binding.txtName.text = Common.currentUser!!.firstName + " " + Common.currentUser!!.lastName
            binding.txtEmail.text = Common.currentUser!!.email
            if (Common.currentUser!!.photoURL!!.isNotBlank()) {
                binding.imgProfile.visibility = View.VISIBLE
                Glide.with(this).load(Common.currentUser!!.photoURL).into(binding.imgProfile)
                dialog.dismiss()
            } else {
                binding.cardAvatar.setCardBackgroundColor(Color.parseColor("#FBC146"))
                binding.imgProfile.visibility = View.GONE
                binding.txtAvatar.visibility = View.VISIBLE
                binding.txtAvatar.text = Common.currentUser!!.firstName!!.substring(0, 1) + Common.currentUser!!.lastName!!.substring(0, 1)
                dialog.dismiss()
            }
        }
        dialog.dismiss()
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

    private fun signOut() {
        MaterialAlertDialogBuilder(this)
            .setTitle("LogOut")
            .setMessage("Are you absolutely sure you want to log out? Confirm your decision by clicking 'Yes' to log out or 'Cancel' to continue your current session.")
            .setIcon(R.drawable.baseline_exit_to_app_24)
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { dialog, which ->
                Common.currentUser = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, SplashScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

                dialog.dismiss()
            }
            .show()
    }

    override fun onStart() {
        super.onStart()
        profileInfo()
    }

}