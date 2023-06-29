package com.leoxtech.customerapp.Fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Screens.ProfileActivity
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.Screens.SplashScreen
import com.leoxtech.customerapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            startActivity(Intent(context, ProfileActivity::class.java))
        }
    }

    private fun profileInfo() {
        binding.txtName.text = Common.currentUser!!.name
        binding.txtEmail.text = Common.currentUser!!.email
        if (mAuth.currentUser!!.photoUrl != null) {
            Glide.with(this).load(mAuth.currentUser!!.photoUrl).into(binding.imgProfile)
            dialog.dismiss()
        } else {
            binding.imgProfile.setImageResource(R.drawable.avatar)
            dialog.dismiss()
        }
        dialog.dismiss()
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

    private fun signOut() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("LogOut")
            .setMessage("Are you absolutely sure you want to log out? Confirm your decision by clicking 'Yes' to log out or 'Cancel' to continue your current session.")
            .setIcon(R.drawable.baseline_exit_to_app_24)
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { dialog, which ->
                Common.currentUser = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(context, SplashScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()

                dialog.dismiss()
            }
            .show()
    }
}