package com.leoxtech.customerapp.Fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.FragmentHomeBinding
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
}