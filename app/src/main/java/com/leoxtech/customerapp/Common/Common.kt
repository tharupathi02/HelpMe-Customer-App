package com.leoxtech.customerapp.Common

import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.leoxtech.customerapp.Model.GarageModel
import com.leoxtech.customerapp.Model.UserModel
import com.leoxtech.customerapp.Screens.SelectGarage

object Common {

    val BOOKING_REF: String = "Bookings"
    var selectedGarage: GarageModel? = null
    const val REQUEST_REF = "Requests"
    const val USER_REFERENCE = "Users"
    const val GARAGE_REF = "Garage Users"
    const val GARAGE_REVIEW_REF = "Garage Reviews"
    const val STORAGE_REF = "userProfileImages/"
    const val REVIEW_REF = "Reviews"
    var currentUser: UserModel? = null

    fun ratingCalculate(ratingValue: Float, ratingCount: Float): Float {
        return ratingValue / ratingCount
    }
}