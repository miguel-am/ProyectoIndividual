package com.example.apphotel.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apphotel.Model.ReviewRequest
import com.example.apphotel.Model.ReviewResponse
import com.example.apphotel.Model.respository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException

data class ReviewUiState(
    val isLoading: Boolean = false,
    val review: ReviewResponse? = null,
    val error: String? = null
)

class ReviewViewModel(private val repository: ReviewRepository) : ViewModel() {

    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state

    var error by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    fun createReview(
        token: String,
        roomId: String,
        reservationId: String,
        rating: Int,
        comment: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            error = null

            val request = ReviewRequest(roomId, reservationId, rating, comment)

            val result = repository.createReview(token, request)

            result.onSuccess {
                isLoading = false
                onSuccess()
            }

            result.onFailure { e ->
                isLoading = false

                if (e is HttpException && e.code() == 409) {
                    // Ya existe -> volver atrás y que vea "VER REVIEW"
                    onSuccess()
                    return@onFailure
                }

                if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    error = "HTTP ${e.code()} - $errorBody"
                } else if (e is IOException) {
                    error = "Sin conexión: ${e.message}"
                } else {
                    error = "Error: ${e.message}"
                }
            }
        }
    }
    fun load(token: String, reservationId: String) {
        viewModelScope.launch {
            _state.value = ReviewUiState(isLoading = true, review = null, error = null)

            val result = repository.getReviewByReservation(token, reservationId)

            result.onSuccess { r ->
                _state.value = ReviewUiState(isLoading = false, review = r, error = null)
            }

            result.onFailure { e ->
                var msg = "Error desconocido"
                if (e is HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    msg = "HTTP ${e.code()} - ${body ?: e.message()}"
                } else if (e is IOException) {
                    msg = "Sin conexión: ${e.message}"
                } else {
                    msg = "Error: ${e.message}"
                }

                _state.value = ReviewUiState(isLoading = false, review = null, error = msg)
            }
        }
    }
    fun hasReview(token: String, reservationId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.getReviewByReservation(token, reservationId)

            result.onSuccess {
                println("HAS_REVIEW OK reservationId=$reservationId")
                onResult(true)
            }

            result.onFailure { e ->
                if (e is HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    println("HAS_REVIEW HTTP ${e.code()} reservationId=$reservationId body=$body")

                    // 404 = no existe (si tu backend lo usa así)
                    if (e.code() == 404) {
                        onResult(false)
                    } else {
                        // cualquier otro error (401/500/404 por ruta inexistente)
                        onResult(false)
                    }
                } else {
                    println("HAS_REVIEW ERROR reservationId=$reservationId msg=${e.message}")
                    onResult(false)
                }
            }
        }
    }

}