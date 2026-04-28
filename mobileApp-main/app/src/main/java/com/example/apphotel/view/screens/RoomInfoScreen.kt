package com.example.apphotel.view.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apphotel.Model.Rooms
import com.example.apphotel.Model.remote.ApiConfig
import com.example.apphotel.viewModel.RoomsViewModel

@Composable
fun InfoRoom(
    id: String,
    vm: RoomsViewModel
){
    val state by vm.uiState.collectAsState()

    LaunchedEffect(id) {
        vm.loadRoomById(id)
    }

    if (state.isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        return
    }

    if (state.errorMessage != null) {
        Text(
            text = "Error: ${state.errorMessage}",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    if (state.selectedRoom == null) {
        Text("Cargando habitación...", modifier = Modifier.padding(16.dp))
        return
    }

    InfoRoomItem(room = state.selectedRoom!!)

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoRoomItem(room: Rooms) {
    val images = room.image!!.map { ApiConfig.BASE_URL.trimEnd('/') + it }
    val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })

    Column (Modifier.fillMaxSize()) {

        Box(Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) { page ->
                if (images.isNotEmpty()) {
                    AsyncImage(
                        model = images[page],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                }
            }

            if (images.size > 1) {
                Row (
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) Color.White
                                    else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {

            Text(
                text = "Habitación ${room.numRoom}",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text("Tipo: ${room.roomType}")
            Text("Planta: ${room.numFloor}")
            Text("Capacidad máxima: ${room.maxOccupancy} personas")

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Precio: ${room.pricePerNight} €/noche",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Disponibilidad: ${room.availability}",
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Services",
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(6.dp))

            if (room.services.isNullOrEmpty()) {
                Text("No services")
            } else {
                room.services.forEach { service ->
                    Text("• $service")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = room.description ?: "Sin descripción disponible"
            )
        }
    }
}

