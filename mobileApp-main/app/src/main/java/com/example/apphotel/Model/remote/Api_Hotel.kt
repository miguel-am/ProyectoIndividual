package com.example.apphotel.Model.remote
import com.example.apphotel.Model.AvailableRooms
import com.example.apphotel.Model.LoginRequest
import com.example.apphotel.Model.LoginResponse
import com.example.apphotel.Model.Reservation
import com.example.apphotel.Model.Rooms
import com.example.apphotel.Model.ReservationRequest
import com.example.apphotel.Model.ReviewRequest
import com.example.apphotel.Model.ReviewResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface Api_Hotel {
    @GET("rooms/available")
    suspend fun getAvailableRooms(
        @Query("checkIn") checkIn: String,
        @Query("checkOut") checkOut: String,
        @Query("guests") guests: Int
    ): AvailableRooms

    @GET("rooms")
    suspend fun getAllRooms():List<Rooms>
    @GET("rooms/{id}")
    suspend fun getRoomById(@Path("id") id: String): Rooms


    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("reservations/my-reservations")
    suspend fun getMyReservations(
        @Header("Authorization") token: String
    ): List<Reservation>

    @PATCH("reservations/cancel/{id}")
    suspend fun cancelReservation(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ResponseBody

    @POST("reservations/add")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body body: ReservationRequest
    ): ResponseBody

    @POST("reviews/add")
    suspend fun createReview(
        @Header("Authorization") token: String,
        @Body body: ReviewRequest
    ): ResponseBody

    @GET("reviews/by-reservation/{reservationId}")
    suspend fun getReviewByReservation(
        @Header("Authorization") token: String,
        @Path("reservationId") reservationId: String
    ): ReviewResponse
}