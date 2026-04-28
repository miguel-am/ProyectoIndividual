package com.example.apphotel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apphotel.Model.Rooms
import com.example.apphotel.Model.remote.ApiClient
import com.example.apphotel.Model.respository.RoomsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoomsUiState(
    val isLoading: Boolean = false,
    val rooms: List<Rooms> = emptyList(),
    val allRooms: List<Rooms> = emptyList(),
    val selectedRoom: Rooms? = null,
    val filters: RoomFilters = RoomFilters(),
    val apiMessage: String? = null,
    val errorMessage: String? = null,
    val filtersError: String? = null,
    val selectedRoomIds: Set<String> = emptySet(),
    val totalCapacitySelected: Int = 0,
    val isBookingInProgress: Boolean = false
)
data class RoomFilters(
    val roomType: String? = null,
    val priceRange: ClosedFloatingPointRange<Float>? = null,
    val services: Set<String> = emptySet()
)
class RoomsViewModel : ViewModel() {
    private val repository = RoomsRepository()
    private val _uiState = MutableStateFlow(RoomsUiState())
    val uiState: StateFlow<RoomsUiState> = _uiState.asStateFlow()

    // Alternar selección de habitación
    fun toggleRoomSelection(room: Rooms) {
        _uiState.update { state ->
            val newSelection = state.selectedRoomIds.toMutableSet()
            var newCapacity = state.totalCapacitySelected

            if (newSelection.contains(room.id)) {
                newSelection.remove(room.id)
                newCapacity -= room.maxOccupancy
            } else {
                newSelection.add(room.id)
                newCapacity += room.maxOccupancy
            }
            state.copy(selectedRoomIds = newSelection, totalCapacitySelected = newCapacity)
        }
    }


    fun loadRoomsAvailable(checkIn: String, checkOut: String, guests: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, apiMessage = null) }

            val result = repository.getAvailableRooms(checkIn, checkOut, guests)
            result.onSuccess { resp ->
                _uiState.update { state ->
                    val newState = state.copy(
                        allRooms = resp.rooms,
                        isLoading = false,
                        apiMessage = resp.message
                    )
                    newState.copy(rooms = applyFiltersToList(newState.allRooms, newState.filters))
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${e.message}") }
            }
        }
    }

    fun resetFiltersUi() {
        _uiState.update { state ->
            state.copy(
                filters = RoomFilters(),          // por defecto (tipo null, priceRange null, adults 1)
                rooms = state.allRooms,           // vuelves a ver todas
                filtersError = null
            )
        }
    }

    fun applyFilters(filters: RoomFilters): Boolean {
        var applied = false
        _uiState.update { state ->
            val filtered = applyFiltersToList(state.allRooms, filters)

            // 1) Validación tipo/precio: si no hay resultados
            if (filtered.isEmpty()) {
                return@update state.copy(
                    filtersError = "No hay habitaciones con ese tipo y/o rango de precio."
                )
            }
            applied = true
            state.copy(
                filters = filters,
                rooms = filtered,
                filtersError = null
            )
        }
        return applied
    }

    private fun applyFiltersToList(list: List<Rooms>, filters: RoomFilters): List<Rooms> {
        return list.asSequence()
            .filter { r -> filters.roomType == null || r.roomType == filters.roomType }
            .filter { r ->
                val range = filters.priceRange ?: return@filter true
                val p = r.pricePerNight.toFloat()
                p >= range.start && p <= range.endInclusive
            }
            .filter { r ->
                if (filters.services.isEmpty()) return@filter true
                val roomServices = (r.services ?: emptyList()).map { it.lowercase() }.toSet()
                filters.services.all { it.lowercase() in roomServices }
                filters.services.any { it.lowercase() in roomServices }
            }
            .toList()
    }


    fun loadRooms(){
        viewModelScope.launch{
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getRooms()
            result.onSuccess { rooms ->
                _uiState.update {it.copy(rooms = rooms, isLoading = false)}
            }.onFailure { e ->
                _uiState.update {it.copy(isLoading = false,errorMessage = "Error: ${e.message}") }
            }
        }
    }


    fun loadRoomById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, selectedRoom = null) }
            repository.getRoomById(id)
                .onSuccess { room -> _uiState.update { it.copy(isLoading = false, selectedRoom = room) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun calculateTotalPrice(checkIn: String, checkOut: String, token: String): Double {
        val state = _uiState.value

        //Calcular el número de noches
        val nights = calculateNights(checkIn, checkOut)

        //Calcular el precio por noche sumando las habitaciones seleccionadas
        val pricePerNight = state.rooms
            .filter { state.selectedRoomIds.contains(it.id) }
            .sumOf { it.pricePerNight }

        //Calcular el total base (sin descuentos)
        val totalBase = pricePerNight * nights

        //Lógica para detectar si el usuario es VIP decodificando el JWT
        val isVip = try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                // Decodificamos el Payload (la segunda parte del token)
                val payload = android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT)
                    .toString(Charsets.UTF_8)

                // Verificamos si el JSON del payload contiene el campo vipStatus como true
                payload.contains("\"vipStatus\":true")
            } else {
                false
            }
        } catch (e: Exception) {
            false // Si hay error en el token, asumimos que no es VIP por seguridad
        }

        // 5. Aplicar descuento del 20% (multiplicar por 0.8) si es VIP
        return if (isVip) {
            totalBase * 0.80
        } else {
            totalBase
        }
    }

    private fun calculateNights(checkIn: String, checkOut: String): Int {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date1 = sdf.parse(checkIn)
            val date2 = sdf.parse(checkOut)
            val diff = date2!!.time - date1!!.time
            (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        } catch (e: Exception) {
            1
        }
    }

    fun clearSelections() {
        _uiState.update { state ->
            state.copy(
                selectedRoomIds = emptySet(),
                totalCapacitySelected = 0,
                errorMessage = null
            )
        }
    }
}