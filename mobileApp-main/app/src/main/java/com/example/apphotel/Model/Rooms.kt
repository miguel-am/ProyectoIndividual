package com.example.apphotel.Model

import com.google.gson.annotations.SerializedName

data class Rooms(
    @SerializedName("_id")val id: String,
    val numRoom: Int,
    val numFloor: Int,
    val roomType: String,
    val description: String?,
    val image: List<String>?,
    val pricePerNight: Double,
    val maxOccupancy: Int,
    val availability: String,
    val services: List<String>? = emptyList()
)
