package com.example.apphotel.Model

data class ReservationRequest(
    val userId: String,
    val roomIds: List<String>,
    val checkIn: String,
    val checkOut: String,
    val numGuests: Int,
    val totalPrice: Double
)