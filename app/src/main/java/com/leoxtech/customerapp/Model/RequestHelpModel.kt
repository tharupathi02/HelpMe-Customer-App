package com.leoxtech.customerapp.Model

import android.net.Uri

class RequestHelpModel {
    var customerUid: String? = null
    var customerName: String? = null
    var customerPhone: String? = null
    var customerIssueTitle: String? = null
    var customerIssueDescription: String? = null
    var customerVehicle: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var imageList: ArrayList<String>? = null
    var status: String? = null
    var garageUid: String? = null
    var timeStamp: String? = null

    var garageRatingValue:Float = 0.toFloat()
    var garageRatingCount:Long = 0.toLong()
}