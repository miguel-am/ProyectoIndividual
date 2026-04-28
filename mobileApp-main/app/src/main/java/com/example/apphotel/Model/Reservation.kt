package com.example.apphotel.Model

import com.google.gson.annotations.SerializedName

data class Reservation(
    @SerializedName("_id") val id: String,
    val checkIn: String,
    val checkOut: String,
    val status: String,
    val totalPrice: Double,
    val roomIds: List<Rooms> // Aquí recibimos los objetos Room poblados
)