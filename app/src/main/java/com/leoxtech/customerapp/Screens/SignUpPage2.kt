package com.leoxtech.customerapp.Screens

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.UserModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivitySignUpPage2Binding

class SignUpPage2 : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpPage2Binding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var dialog: AlertDialog

    private var uID = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPage2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        initialUserGoogleData()

        clickListeners()

        dialogBox()

    }

    private fun clickListeners() {
        binding.txtSkipForm.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            signUpForm()
        }

    }

    private fun signUpForm() {
        val firstName = binding.txtFirstName.editText?.text.toString()
        val lastName = binding.txtLastName.editText?.text.toString()
        val address = binding.txtAddress.editText?.text.toString()
        val contactNumber = binding.txtContactNumber.editText?.text.toString()
        val idNumber = binding.txtIDNumber.editText?.text.toString()

        if (firstName.isEmpty()) {
            binding.txtFirstName.error = "First name is required"
            return
        } else if (lastName.isEmpty()) {
            binding.txtLastName.error = "Last name is required"
            return
        } else if (address.isEmpty()) {
            binding.txtAddress.error = "Address is required"
            return
        } else if (contactNumber.isEmpty()) {
            binding.txtContactNumber.error = "Contact number is required"
            return
        } else if (idNumber.isEmpty()) {
            binding.txtIDNumber.error = "ID number is required"
            return
        } else {
            dialog.show()
            val userModel = UserModel()
            userModel.uid = uID
            userModel.name = binding.txtFirstName.editText?.text.toString() + " " + binding.txtLastName.editText?.text.toString()
            userModel.firstName = binding.txtFirstName.editText?.text.toString()
            userModel.lastName = binding.txtLastName.editText?.text.toString()
            userModel.email = binding.txtEmail.editText?.text.toString()
            userModel.address = binding.txtAddress.editText?.text.toString()
            userModel.phone = binding.txtContactNumber.editText?.text.toString()
            userModel.idNumber = binding.txtIDNumber.editText?.text.toString()
            userModel.photoURL = mAuth.currentUser?.photoUrl.toString()

            userRef.child(uID).setValue(userModel).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Snackbar.make(binding.root, "Congratulation! Registration Completed...", Snackbar.LENGTH_SHORT).show()
                    Common.currentUser = userModel
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    dialog.dismiss()
                } else {
                    Snackbar.make(binding.root, "Registration Failed... ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

        }

    }

    private fun initialUserGoogleData() {
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        uID = mAuth.currentUser?.uid.toString()
        binding.txtEmail.editText?.setText(mAuth.currentUser?.email)
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