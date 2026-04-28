package com.example.apphotel.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.apphotel.data.SessionManager
import com.example.apphotel.navigation.Routes
import com.example.apphotel.ui.theme.Correct
import com.example.apphotel.viewModel.ReservationViewModel
import com.example.apphotel.viewModel.RoomsViewModel

@Composable
fun ConfirmReservationScreen(
    viewModel: RoomsViewModel,
    reservationViewModel: ReservationViewModel,
    navigationController: NavHostController,
    checkIn: String,
    checkOut: String,
    guests: Int,
) {
    val state by viewModel.uiState.collectAsState()
    val reservationState by reservationViewModel.uiState.collectAsState()
    val token = SessionManager.userToken ?: ""
    val selectedRooms = state.rooms.filter { state.selectedRoomIds.contains(it.id) }
    val total = viewModel.calculateTotalPrice(checkIn, checkOut,token)
    val esVip = reservationViewModel.shouldShowVipLabel(token)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Resumen de tu Reserva", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Card con detalles de fechas
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Fechas: $checkIn al $checkOut", fontWeight = FontWeight.SemiBold)
                Text("Huéspedes: $guests personas")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Habitaciones seleccionadas:", fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(selectedRooms) { room ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• Habitación ${room.numRoom} (${room.roomType})", color = Correct)
                    Text("${room.pricePerNight}€", color = Correct)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (esVip) {
                Text("Descuento VIP aplicado (-20%)", color = Color(0xFF2E7D32), fontSize = 12.sp)
            }
            Text("TOTAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("$total €", style = MaterialTheme.typography.headlineSmall, color = Correct, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de confirmar final
        Button(
            onClick = {
                // Pasamos los IDs de las habitaciones seleccionadas desde el RoomsViewModel
                val roomIds = state.selectedRoomIds.toList()

                reservationViewModel.createReservation(
                    token = token,
                    roomIds = roomIds,
                    checkIn = checkIn,
                    checkOut = checkOut,
                    guests = guests,
                    finalPrice = total
                ) {
                    navigationController.navigate(Routes.Reservations.route) {
                        popUpTo(Routes.Home.route) { inclusive = false }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            // Usamos el estado de carga del reservationViewModel
            enabled = !reservationState.isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            if (reservationState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("CONFIRMAR Y PAGAR EN HOTEL")
            }
        }

        // Mostrar error si existe
        reservationState.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
