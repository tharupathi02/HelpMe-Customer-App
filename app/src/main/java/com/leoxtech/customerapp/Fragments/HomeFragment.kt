package com.leoxtech.customerapp.Fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
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

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var popularGarageList: ArrayList<GarageModel>

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

        dialog.show()

        profileInfo()

    }

    private fun profileInfo() {
        binding.txtCustomerName.text = Common.currentUser!!.name
        if (mAuth.currentUser!!.photoUrl != null) {
            Glide.with(this).load(mAuth.currentUser!!.photoUrl).into(binding.imgAvatar)
            dialog.dismiss()
        } else {
            binding.imgAvatar.setImageResource(R.drawable.avatar)
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