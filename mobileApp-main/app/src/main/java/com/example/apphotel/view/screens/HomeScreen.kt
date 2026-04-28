package com.example.apphotel.view.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.apphotel.navigation.Routes
import com.example.apphotel.viewModel.RoomsViewModel
import com.example.apphotel.viewModel.SearchRoomsViewModel
import java.util.Calendar

@Composable
fun Home(
    viewModel: SearchRoomsViewModel,
    roomsViewModel: RoomsViewModel,
    navigationController: NavHostController
) {
    val colors = MaterialTheme.colorScheme
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var guests by remember { mutableStateOf(1) }

    // Manejo de Navegación
    LaunchedEffect(state.navigateToRooms) {
        if (state.navigateToRooms) {
            navigationController.navigate(
                Routes.Rooms.createRoute(
                    checkIn = state.checkInDate,
                    checkOut = state.checkOutDate,
                    guests = guests
                )
            )
            viewModel.onNavigationHandled()
        }
    }

    // Lanzador de picker
    viewModel.showDatePickerEvent?.let { isCheckIn ->
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                viewModel.onDateSelected(year, month, day, isCheckIn)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { viewModel.onDatePickerDismissed() }
            show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("Encuentra tu \nhabitación perfecta!", fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(40.dp))

        //Input Fecha Inicio
        DateTextField(
            label = "Fecha Entrada",
            value = state.checkInDate,
            onClick = { viewModel.onDateClicked(true) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Input Fecha Final
        DateTextField(
            label = "Fecha Salida",
            value = state.checkOutDate,
            onClick = { viewModel.onDateClicked(false) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Personas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color =MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxWidth())
        CounterRow(
            title = "Adultos",
            subtitle = "Desde 13 años",
            value = guests,
            onMinus = { if (guests > 1) guests-- },
            onPlus = { guests++ }
        )

        Spacer(modifier = Modifier.height(32.dp))

        //Botón Buscar
        Button(
            onClick = {
                // 1. Limpiamos las selecciones anteriores del RoomsViewModel
                roomsViewModel.clearSelections()

                // 2. Ejecutamos la búsqueda en el SearchRoomsViewModel (para manejar navegación y fechas)
                viewModel.onSearchClicked(guests)

                // Nota: No navegues manualmente aquí con "rooms_list" porque ya tienes
                // un LaunchedEffect arriba que escucha 'state.navigateToRooms' y lo hace de forma segura.
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = !state.isLoading,
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary)
        ) {
            Text(if (state.isLoading) "Buscando..." else "Buscar")
        }

        //Mensaje de Error
        state.error?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun DateTextField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label,color = MaterialTheme.colorScheme.secondary) },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.secondary,
                disabledLabelColor = MaterialTheme.colorScheme.secondary,
                disabledBorderColor = MaterialTheme.colorScheme.secondary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calendario",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        )
    }
}

@Composable
fun CounterRow(
    title: String,
    subtitle: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color =MaterialTheme.colorScheme.secondary )
            Text(subtitle, style = MaterialTheme.typography.bodySmall,color =MaterialTheme.colorScheme.secondary)
        }
        IconButton(onClick = onMinus, enabled = value > 1) { Text("-",fontSize = 14.sp) }
        Text(value.toString(),color =MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 10.dp))
        IconButton(onClick = onPlus) { Text("+", fontSize = 14.sp) }
    }
}