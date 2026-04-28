package com.example.apphotel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apphotel.Model.ReservationRequest
import com.example.apphotel.Model.respository.RoomsRepository
import com.example.apphotel.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReservationViewModel(private val repository: RoomsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private fun getUserIdFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT).toString(Charsets.UTF_8)
            val regex = "\"id\":\"([^\"]+)\"".toRegex()
            val match = regex.find(payload)
            match?.groups?.get(1)?.value
        } catch (e: Exception) { null }
    }

    fun createReservation(token: String, roomIds: List<String>, checkIn: String, checkOut: String, guests: Int, finalPrice:Double, onSuccess: () -> Unit) {
        val userId = getUserIdFromToken(token) ?: ""

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = ReservationRequest(userId, roomIds, checkIn, checkOut, guests,finalPrice)

            // LLAMADA A TRAVÉS DEL REPOSITORIO
            val result = repository.createReservation(token, request)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    private fun isUserVip(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return false
            val payload = android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT).toString(Charsets.UTF_8)

            // Buscamos la clave "vipStatus":true en el JSON del token
            payload.contains("\"vipStatus\":true")
        } catch (e: Exception) {
            false
        }
    }

    //Función para saber si mostrar la etiqueta "VIP" en la UI
    fun shouldShowVipLabel(token: String): Boolean = isUserVip(token)
}

data class BookingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)