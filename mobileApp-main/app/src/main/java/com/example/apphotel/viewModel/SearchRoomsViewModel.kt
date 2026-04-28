package com.example.apphotel.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apphotel.Model.Rooms
import com.example.apphotel.Model.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class SearchUiState(
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToRooms: Boolean = false
)

class SearchRoomsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    // Controla la visibilidad del picker nativo: true=In, false=Out, null=Cerrado
    var showDatePickerEvent by mutableStateOf<Boolean?>(null)
        private set

    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun onDateClicked(isCheckIn: Boolean) {
        showDatePickerEvent = isCheckIn
    }

    fun onDatePickerDismissed() {
        showDatePickerEvent = null
    }

    fun onDateSelected(year: Int, month: Int, day: Int, isCheckIn: Boolean) {
        val calendar = Calendar.getInstance().apply {
            set(year, month, day)
        }
        val dateStr = formatter.format(calendar.time)

        _uiState.update {
            if (isCheckIn) it.copy(checkInDate = dateStr, error = null)
            else it.copy(checkOutDate = dateStr, error = null)
        }
        onDatePickerDismissed()
    }

    fun onSearchClicked(guests: Int) {
        val s = _uiState.value

        if (s.checkInDate.isBlank() || s.checkOutDate.isBlank()) {
            _uiState.update { it.copy(error = "Las fechas son obligatorias") }
            return
        }

        // Validación de coherencia de fechas
        if (s.checkInDate >= s.checkOutDate) {
            _uiState.update { it.copy(error = "La fecha de entrada debe ser anterior a la salida") }
            return
        }

        // Activamos el evento de navegación que escucha el LaunchedEffect en Home
        _uiState.update { it.copy(error = null, navigateToRooms = true) }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToRooms = false) }
    }
}