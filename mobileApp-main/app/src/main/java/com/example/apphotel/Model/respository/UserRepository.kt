package com.example.apphotel.Model.respository

import com.example.apphotel.Model.LoginRequest
import com.example.apphotel.Model.LoginResponse
import com.example.apphotel.Model.remote.ApiClient
import okio.IOException
import retrofit2.HttpException

class AuthRepository {

    private val api = ApiClient.instance

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val res = api.login(LoginRequest(email, password))
            Result.success(res)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}