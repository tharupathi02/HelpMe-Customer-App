package com.leoxtech.customerapp.Screens

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.UserModel
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityUpdateProfileBinding

class UpdateProfile : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    var selectedImageUri: Uri? = null
    private var uID = "";
    private var dbImageURL = "";

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)

        clickListeners()

        dialogBox()

        profileInfo()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun clickListeners() {
        binding.cardBack.setOnClickListener {
            finish()
        }

        binding.btnChooseImage.setOnClickListener {
            pickImage()
        }

        binding.btnSignUp.setOnClickListener {
            updateProfile()
        }

    }

    private fun updateProfile() {
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
            if (selectedImageUri != null) {
                val storageRef = FirebaseStorage.getInstance().reference.child(Common.STORAGE_REF + uID + "-" + System.currentTimeMillis() + ".jpg")
                storageRef.putFile(selectedImageUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val userModel = UserModel()
                            userModel.uid = uID
                            userModel.name = binding.txtFirstName.editText?.text.toString() + " " + binding.txtLastName.editText?.text.toString()
                            userModel.firstName = binding.txtFirstName.editText?.text.toString()
                            userModel.lastName = binding.txtLastName.editText?.text.toString()
                            userModel.email = binding.txtEmail.editText?.text.toString()
                            userModel.address = binding.txtAddress.editText?.text.toString()
                            userModel.phone = binding.txtContactNumber.editText?.text.toString()
                            userModel.idNumber = binding.txtIDNumber.editText?.text.toString()
                            userModel.photoURL = uri.toString()

                            dbRef.child(uID).setValue(userModel).addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    Snackbar.make(binding.root, "Congratulation! Your profile has been updated successfully.", Snackbar.LENGTH_SHORT).show()
                                    Common.currentUser = userModel
                                    if (dbImageURL.isNotEmpty()) {
                                        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(dbImageURL)
                                        storageRef.delete().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                dialog.dismiss()
                                                finish()
                                            } else {
                                                Snackbar.make(binding.root, "Profile update failed. Please try again. Error: ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                                                dialog.dismiss()
                                            }
                                        }
                                    } else {
                                        dialog.dismiss()
                                        finish()
                                    }
                                } else {
                                    Snackbar.make(binding.root, "Profile update failed. Please try again. Error: ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                            }
                        }
                    } else {
                        Snackbar.make(binding.root, "Error: ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }.addOnProgressListener { taskSnapshot ->
                    // Add progress dialog with parentage here for image upload process
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    val progressText = dialog.findViewById<TextView>(R.id.txtProgress)
                    progressText.visibility = View.VISIBLE
                    progressText.text = "Updating... " + progress.toInt() + "%"
                }
            } else {
                val userModel = UserModel()
                userModel.uid = uID
                userModel.name = binding.txtFirstName.editText?.text.toString() + " " + binding.txtLastName.editText?.text.toString()
                userModel.firstName = binding.txtFirstName.editText?.text.toString()
                userModel.lastName = binding.txtLastName.editText?.text.toString()
                userModel.email = binding.txtEmail.editText?.text.toString()
                userModel.address = binding.txtAddress.editText?.text.toString()
                userModel.phone = binding.txtContactNumber.editText?.text.toString()
                userModel.idNumber = binding.txtIDNumber.editText?.text.toString()
                userModel.photoURL = dbImageURL

                dbRef.child(uID).setValue(userModel).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(binding.root, "Congratulation! Your profile has been updated successfully.", Snackbar.LENGTH_SHORT).show()
                        Common.currentUser = userModel
                        dialog.dismiss()
                        finish()
                    } else {
                        Snackbar.make(binding.root, "Profile update failed. Please try again. Error: ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun pickImage() {
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        startActivityForResult(intent, 101)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) {
                // catch any exception here
                if (data!!.data == null) {
                    Snackbar.make(binding.root, "Error: Image draw too large. Please select another image. and try again.", Snackbar.LENGTH_SHORT).setAction("Try Again") {
                        pickImage()
                    }.show()
                } else {
                    selectedImageUri = data!!.data
                    binding.imgProfile.setImageURI(selectedImageUri)
                }
            }
        }
    }

    private fun profileInfo() {
        dialog.show()
        uID = mAuth.currentUser!!.uid
        if (Common.currentUser != null) {
            if (Common.currentUser!!.photoURL != null) {
                Glide.with(this).load(Common.currentUser!!.photoURL).into(binding.imgProfile)
            } else {
                binding.imgProfile.setImageResource(R.drawable.avatar)
            }
            binding.txtFirstName.editText?.setText(Common.currentUser!!.firstName)
            binding.txtLastName.editText?.setText(Common.currentUser!!.lastName)
            binding.txtEmail.editText?.setText(Common.currentUser!!.email)
            binding.txtContactNumber.editText?.setText(Common.currentUser!!.phone)
            binding.txtAddress.editText?.setText(Common.currentUser!!.address)
            binding.txtIDNumber.editText?.setText(Common.currentUser!!.idNumber)
            dbImageURL = Common.currentUser!!.photoURL.toString()
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
}