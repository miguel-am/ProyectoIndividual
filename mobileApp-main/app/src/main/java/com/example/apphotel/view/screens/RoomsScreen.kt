package com.example.apphotel.view.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.apphotel.Model.Rooms
import com.example.apphotel.Model.remote.ApiConfig
import com.example.apphotel.data.SessionManager
import com.example.apphotel.navigation.Routes
import com.example.apphotel.ui.theme.Correct
import com.example.apphotel.viewModel.RoomsViewModel


@Composable
fun RoomView(
    listRoomsViewModel: RoomsViewModel,
    navigationController: NavHostController,
    checkIn: String,
    checkOut: String,
    guests: Int
) {
    val listRoomsState by listRoomsViewModel.uiState.collectAsState()

    val token = SessionManager.userToken ?: ""

    LaunchedEffect(checkIn, checkOut, guests) {
        if (checkIn.isNotBlank() && checkOut.isNotBlank() && guests >= 1) {
            listRoomsViewModel.loadRoomsAvailable(checkIn, checkOut, guests)
        }
    }

    // Navegación automática tras éxito
    LaunchedEffect(listRoomsState.isBookingInProgress) {
        if (listRoomsState.isBookingInProgress) {
            navigationController.navigate(Routes.Reservations.route)
        }
    }
    val colors = MaterialTheme.colorScheme
    val okSelected = listRoomsState.totalCapacitySelected >= guests


    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (okSelected) colors.tertiaryContainer else colors.errorContainer
        ) {
            Text(
                text = "Seleccionado: ${listRoomsState.totalCapacitySelected} / $guests personas",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (okSelected) Correct else colors.onErrorContainer
            )
        }

        FilledTonalButton (
            onClick = { navigationController.navigate(Routes.Filters.route) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.onPrimaryContainer
            )
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Filtros")

        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listRoomsState.rooms) { room ->
                val isSelected = listRoomsState.selectedRoomIds.contains(room.id)
                RoomsItem(
                    room = room,
                    isSelected = isSelected,
                    onSelectClick = { listRoomsViewModel.toggleRoomSelection(room) },
                    onInfoClick = { navigationController.navigate(Routes.InfoRoom.createRoute(room.id)) }
                )
            }
        }

        // BOTÓN DE CREAR RESERVA
        Button(
            onClick = {
                navigationController.navigate("confirm_booking/$checkIn/$checkOut/$guests")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
            enabled = okSelected,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.secondary
            )
        ) {
            Text("RESUMEN")
        }
    }
}

@Composable
fun RoomsItem(
    room: Rooms,
    isSelected: Boolean,
    onSelectClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val firstImg = room.image?.firstOrNull()
    val imageUrl = if (firstImg != null) ApiConfig.BASE_URL.trimEnd('/') + firstImg else null

    val border = if (isSelected) BorderStroke(2.dp, colors.tertiary) else null
    val container = if (isSelected) colors.primaryContainer else colors.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .size(250.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onSelectClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = border
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AssistChip(
                onClick = { },
                enabled = false,
                label = { Text("Disponible") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .zIndex(1f),
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = colors.tertiary,
                    disabledLabelColor = colors.background
                )
            )

            Column (
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                // Imagen
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceVariant)
                    )
                }

                Spacer(Modifier.height(30.dp))

                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .weight(1f)
                ){
                    Column (){
                        Text(
                            text = "Habitación ${room.roomType}",
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Nº ${room.numRoom}",
                            textAlign = TextAlign.Center
                        )
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(Icons.Default.Face, contentDescription = "Personas", tint = Color.Gray,
                                modifier = Modifier
                                    .size(12.dp)
                                    )
                            Text(
                                text = " ${room.maxOccupancy} personas",
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Column (
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text(
                            text = "${room.pricePerNight} €/noche",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = onInfoClick) {
                            Icon(Icons.Default.Info, contentDescription = "Info")
                        }
                    }
                }
            }
        }
    }
}
