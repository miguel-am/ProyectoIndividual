package com.example.apphotel.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.apphotel.viewModel.RoomFilters
import com.example.apphotel.viewModel.RoomsViewModel

@Composable
fun Filters(
    roomsViewModel: RoomsViewModel,
    navController: NavHostController
) {
    val state by roomsViewModel.uiState.collectAsState()
    val scroll = rememberScrollState()

    var selectedType by remember { mutableStateOf(state.filters.roomType) }
    val serviceOptions = listOf("wifi", "parking", "gym", "towels", "smoke", "crib")
    var selectedServices by remember { mutableStateOf(state.filters.services) }
    val minP = 0f
    val maxP = 500f

    var priceRange by remember {
        mutableStateOf(state.filters.priceRange ?: (minP..maxP))
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scroll)
        .padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.error)
            }
            Text("Filtros", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.size(16.dp))

        RoomTypeDropdown(
            value = selectedType,
            onChange = { selectedType = it }
        )

        Spacer(Modifier.size(18.dp))

        Text(
            "Precio: ${priceRange.start.toInt()}€ - ${priceRange.endInclusive.toInt()}€",
            style = MaterialTheme.typography.titleMedium
        )
        RangeSlider(
            value = priceRange,
            onValueChange = { priceRange = it },
            valueRange = (minP..maxP)
        )
        Spacer(Modifier.size(18.dp))

        Text(
            text = "Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.size(8.dp))

        serviceOptions.forEach { key ->
            val checked = selectedServices.contains(key)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { isChecked ->
                        selectedServices = if (isChecked) selectedServices + key else selectedServices - key
                    }
                )
                Text(text = key)
            }
        }

        Spacer(Modifier.size(8.dp))



        state.filtersError?.let {
            Spacer(Modifier.size(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    selectedType = null
                    priceRange = (minP..maxP)
                    selectedServices = emptySet()
                    roomsViewModel.resetFiltersUi()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) { Text("Limpiar") }

            Spacer(Modifier.width(12.dp))

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    val ok = roomsViewModel.applyFilters(
                            RoomFilters(
                                roomType = selectedType,
                                priceRange = priceRange,
                                services = selectedServices
                            )
                    )
                    if (ok) navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) { Text("Aplicar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun RoomTypeDropdown(
    value: String?,
    onChange: (String?) -> Unit
) {
    val options = listOf("single", "double", "triple", "fourfold")
    var expanded by remember { mutableStateOf(false) }
    val label = value ?: "Cualquiera"

    ExposedDropdownMenuBox (expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de habitación") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Cualquiera") },
                onClick = { onChange(null); expanded = false }
            )
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = { onChange(opt); expanded = false }
                )
            }
        }
    }
}