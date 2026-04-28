package com.example.apphotel.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apphotel.viewModel.ReviewViewModel

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    token: String,
    roomId: String,
    reservationId: String,
    onBack: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Valorar estancia", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Puntuación")

        Row {
            for (i in 1..5) {
                IconButton(onClick = { rating = i }) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= rating) Color.Yellow else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comentario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(

            onClick = {
                viewModel.createReview(
                    token,
                    roomId,
                    reservationId,
                    rating,
                    comment
                ) {
                    onBack()
                }
            },
            enabled = rating > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            println("ROOM: $roomId")
            println("RES: $reservationId")
            println("TOKEN: $token")
            Text("PUBLICAR")
        }

        if (viewModel.isLoading) {
            Text("Enviando...", color = Color.Gray)
        }

        if (viewModel.error != null) {
            Text(
                text = viewModel.error!!,
                color = Color.Red
            )
        }
    }
}