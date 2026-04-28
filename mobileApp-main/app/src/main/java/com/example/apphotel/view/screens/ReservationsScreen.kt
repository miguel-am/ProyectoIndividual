package com.example.apphotel.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.apphotel.Model.Reservation
import com.example.apphotel.Model.respository.ReviewRepository
import com.example.apphotel.navigation.Routes
import com.example.apphotel.ui.theme.Correct
import com.example.apphotel.viewModel.ReviewViewModel
import com.example.apphotel.viewModel.UserReservationViewModel

@Composable
fun ReservationsScreen(viewModel: UserReservationViewModel, token: String,
                       navController: NavHostController
) {
    val state by viewModel.uiState.collectAsState()
    val reviewVM = remember { ReviewViewModel(ReviewRepository()) }
    // Estados para el diálogo de confirmación
    var showDialog by remember { mutableStateOf(false) }
    var selectedReservationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchReservations(token)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar cancelación") },
            text = { Text("¿Estás seguro de que deseas cancelar esta reserva? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedReservationId?.let { viewModel.cancelReservation(token, it) }
                    showDialog = false
                }) {
                    Text("ACEPTAR", color = Correct, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCELAR", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.error != null) {
            Text(text = state.error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.reservations) { res ->
                    ReservationItem(
                        res = res,
                        token = token,
                        reviewViewModel = reviewVM,
                        onCancelClick = { selectedReservationId = res.id
                            showDialog = true },
                        onReviewClick = { roomId, reservationId ->
                            navController.navigate(Routes.Review.createRoute(roomId, reservationId))
                        },
                        onViewReviewClick = { reservationId ->
                            navController.navigate(Routes.ViewReview.createRoute(reservationId))
                        })
                }
            }
        }
    }
}

@Composable
fun ReservationItem(
    res: Reservation,
    token: String,
    reviewViewModel: ReviewViewModel,
    onCancelClick: () -> Unit,
    onReviewClick: (String, String) -> Unit,
    onViewReviewClick: (String) -> Unit
) {
    var alreadyReviewed by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }

    // Solo chequeamos si está terminada
    LaunchedEffect(res.status, res.id) {
        if (res.status == "terminada") {
            reviewViewModel.hasReview(token, res.id) { exists ->
                alreadyReviewed = exists
            }
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Reserva #${res.id.takeLast(5)}", fontWeight = FontWeight.Bold)
                Surface(
                    color = if (res.status == "confirmada") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = res.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (res.status == "confirmada") Correct else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Entrada: ${res.checkIn.take(10)}")
            Text("Salida: ${res.checkOut.take(10)}")

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Habitaciones:", fontWeight = FontWeight.SemiBold)
            res.roomIds.forEach { room ->
                Text("• Habitación ${room.numRoom} (${room.roomType})")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (res.status == "confirmada") {
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CANCELAR RESERVA", color = Color.White)
                }
            }


            if (res.status == "terminada") {

                // ✅ Si NO hay review -> mostrar VALORAR
                if (!alreadyReviewed) {
                    Button(
                        onClick = {
                            val roomId = res.roomIds.first().id
                            onReviewClick(roomId, res.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("VALORAR ESTANCIA")
                    }
                }

                // ✅ Si YA hay review -> mostrar VER REVIEW (botón pequeño)
                if (alreadyReviewed) {
                    TextButton(
                        onClick = { onViewReviewClick(res.id) },
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("VER REVIEW", fontSize = 12.sp)
                    }
                }
            }

            Text(
                text = "${res.totalPrice} €",
                modifier = Modifier.align(Alignment.End),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (res.status == "confirmada") Correct else MaterialTheme.colorScheme.error
            )
        }
    }

}