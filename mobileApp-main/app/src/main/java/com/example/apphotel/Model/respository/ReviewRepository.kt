package com.example.apphotel.Model.respository

import com.example.apphotel.Model.ReviewRequest
import com.example.apphotel.Model.ReviewResponse
import com.example.apphotel.Model.remote.ApiClient

class ReviewRepository {
    private val api = ApiClient.instance

    suspend fun createReview(token: String, request: ReviewRequest): Result<Unit> {
        return try {
            api.createReview("Bearer $token", request)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getReviewByReservation(token: String, reservationId: String): Result<ReviewResponse> {
        return try {
            val res = api.getReviewByReservation("Bearer $token", reservationId)
            Result.success(res)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}