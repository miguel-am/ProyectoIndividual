package com.example.apphotel.Model.respository

import com.example.apphotel.Model.AvailableRooms
import com.example.apphotel.Model.Reservation
import com.example.apphotel.Model.ReservationRequest
import com.example.apphotel.Model.Rooms
import com.example.apphotel.Model.remote.ApiClient
import okhttp3.ResponseBody

class RoomsRepository {
    private val api = ApiClient.instance

    suspend fun getAvailableRooms(checkIn: String, checkOut: String, guests: Int): Result<AvailableRooms> {
        return try {
            Result.success(api.getAvailableRooms(checkIn, checkOut, guests))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRooms(): Result<List<Rooms>> {
        return try {
            val response = api.getAllRooms()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoomById(id: String): Result<Rooms> = try {
        Result.success(api.getRoomById(id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createReservation(token: String, request: ReservationRequest): Result<ResponseBody> {
        return try {
            // Ahora ambos coinciden: Api devuelve ResponseBody y Repo devuelve Result<ResponseBody>
            val response = api.createReservation("Bearer $token", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyReservations(token: String): Result<List<Reservation>> {
        return try {
            Result.success(api.getMyReservations("Bearer $token"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelReservation(token: String, id: String): Result<ResponseBody> {
        return try {
            val response = api.cancelReservation("Bearer $token", id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}