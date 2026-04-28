package com.example.apphotel.data

object SessionManager {
    var userToken: String? = null
    var userRole: String? = null

    fun clearSession() {
        userToken = null
        userRole = null
    }
}