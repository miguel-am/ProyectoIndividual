package com.example.apphotel.Model

data class ReviewRequest(
    val roomId: String,
    val reservationId: String,
    val rating: Int,
    val comment: String?
)
data class ReviewResponse(
    val _id: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)