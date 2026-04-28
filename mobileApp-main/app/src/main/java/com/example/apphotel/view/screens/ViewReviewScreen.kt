package com.example.apphotel.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apphotel.viewModel.ReviewViewModel

@Composable
fun ViewReviewScreen(
    token: String,
    reservationId: String,
    viewModel: ReviewViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(reservationId) {
        viewModel.load(token, reservationId)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier =
        Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tu review", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
        }

        if (state.error != null) {
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
        }

        if (state.review != null) {
            val r = state.review!!
            Text("Puntuación: ${r.rating}/5")
            Spacer(Modifier.height(8.dp))
            Text("Comentario: ${r.comment ?: "-"}")
            Spacer(Modifier.height(8.dp))
            Text("Fecha: ${r.createdAt.take(10)}")
            Spacer(Modifier.height(16.dp))
        }

        Button(onClick = onBack) { Text("VOLVER") }
    }
}