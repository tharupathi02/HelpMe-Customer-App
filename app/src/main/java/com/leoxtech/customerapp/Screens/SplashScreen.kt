package com.leoxtech.customerapp.Screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.customerapp.Common.Common
import com.leoxtech.customerapp.Model.UserModel
import com.leoxtech.customerapp.R
import com.leoxtech.holify.Common.CheckConnectionLiveData


class SplashScreen : AppCompatActivity() {

    private lateinit var cld: CheckConnectionLiveData
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userRef: DatabaseReference

    private lateinit var fusedLocation: FusedLocationProviderClient
    var isPermissionGranted: Boolean = false
    private val LOCATION_REQUEST_CODE = 100

    private lateinit var requestLauncher: ActivityResultLauncher<String>
    var isNotificationPermissionGranted: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        mAuth = FirebaseAuth.getInstance()

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        requestLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // If the permission is granted, display the toast.
                    isNotificationPermissionGranted = true
                    checkLocationPermission()
                } else {
                    // If the permission is not granted, display the toast.
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        notificationPermission()

    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            } else {
                goToNext()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", applicationContext.packageName, null)
                intent.data = uri
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), "Please allow storage access permission", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Allow") {
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        val uri = Uri.fromParts("package", applicationContext.packageName, null)
                        intent.data = uri
                    }.show()
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            }
        }
    }

    private fun goToDashboard() {
        Handler().postDelayed({
            val user = mAuth.currentUser
            if (user != null) {
                checkUserFromFirebase(user!!)
            } else {
                startActivity(Intent(this, SignIn::class.java))
                finish()
            }
        }, 2000)
    }

    private fun checkUserFromFirebase(user: FirebaseUser) {
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        userRef!!.child(user!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    goToHomeActivity(userModel)
                } else {
                    startActivity(Intent(this@SplashScreen, SignIn::class.java))
                    finish()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@SplashScreen, "" + p0.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun goToHomeActivity(userModel: UserModel?) {
        Common.currentUser = userModel!!
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notificationPermission() {
        // Check if the notification permission is granted or not.
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If the notification permission is not granted, request for the same.
            requestLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // If the notification permission is already granted, display the toast.
            isNotificationPermissionGranted = true
            checkLocationPermission()
        }
    }

    private fun goToNext() {
        cld = CheckConnectionLiveData(application)
        cld.observe(this) {
            if (it) {
                goToDashboard()
            } else {
                Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST_CODE)
        } else {
            isPermissionGranted = true
            requestStoragePermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        isPermissionGranted = false
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = true
                    requestStoragePermission()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    requestStoragePermission()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}