package com.example.apphotel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apphotel.Model.Reservation
import com.example.apphotel.Model.respository.RoomsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyReservationsUiState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class UserReservationViewModel(private val repository: RoomsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState

    fun fetchReservations(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getMyReservations(token)

            result.onSuccess { response ->
                _uiState.update { it.copy(reservations = response, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = "Error al cargar reservas", isLoading = false) }
            }
        }
    }

    fun cancelReservation(token: String, reservationId: String) {
        viewModelScope.launch {
            val result = repository.cancelReservation(token, reservationId)

            result.onSuccess {
                // Modificamos el estado local para que Compose reaccione al instante
                _uiState.update { currentState ->
                    val listaActualizada = currentState.reservations.map { res ->
                        if (res.id == reservationId) {
                            res.copy(status = "cancelada")
                        } else {
                            res
                        }
                    }
                    currentState.copy(reservations = listaActualizada)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = "No se pudo cancelar: ${e.message}") }
            }
        }
    }
}