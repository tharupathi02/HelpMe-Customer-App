package com.leoxtech.customerapp.Model

class GarageModel {
    var garageId: String? = null
    var garageName: String? = null
    var garageDescription: String? = null
    var garageWorkingHours: String? = null
    var garageWorkingVehicleTypes: List<GarageWorkingVehicleTypes>? = ArrayList<GarageWorkingVehicleTypes>()
    var garageBookings: List<GarageBookings>? = ArrayList<GarageBookings>()
    var garageImage: String? = null

    var garageRatingValue:Double = 0.toDouble()
    var garageRatingCount:Long = 0.toLong()
}