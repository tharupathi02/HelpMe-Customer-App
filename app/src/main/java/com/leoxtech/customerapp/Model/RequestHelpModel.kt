package com.leoxtech.customerapp.Model

import android.net.Uri

class RequestHelpModel {
    var key: String? = null
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

    var garageReviewValue: Float? = 0.0.toFloat()
    var garageReviewComment: String? = null

}