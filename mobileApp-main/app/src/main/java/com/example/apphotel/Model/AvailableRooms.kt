package com.example.apphotel.Model

data class AvailableRooms(
    val code: String,
    val message: String,
    val guests: Int,
    val maxPerRoom: Int,
    val roomsNeeded: Int?,
    val rooms: List<Rooms>
)
