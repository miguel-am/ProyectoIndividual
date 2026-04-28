package com.example.apphotel.view.screens

import com.example.apphotel.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Info() {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.icono_proyecto),
            contentDescription = "User icon",
            modifier = Modifier.size(200.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.height(16.dp))

        // Título
        Text(
            text = "Hotel Pere Maria",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground,
            fontSize = 40.sp
        )

        Spacer(Modifier.height(22.dp))

        // 4 cards
        InfoCardRow(
            icon = Icons.Default.LocationOn,
            title = "Dirección",
            value = "Calle Mayor 12"
        )

        Spacer(Modifier.height(12.dp))

        InfoCardRow(
            icon = Icons.Default.Phone,
            title = "Teléfono",
            value = "666 123 456"
        )

        Spacer(Modifier.height(12.dp))

        InfoCardRow(
            icon = Icons.Default.Email,
            title = "Email",
            value = "hotelperemaria@gmail.com"
        )

        Spacer(Modifier.height(12.dp))

        InfoCardRow(
            icon = Icons.Default.DateRange,
            title = "Horario",
            value = "24h"
        )
        Spacer(Modifier.height(50.dp))
    }
}

@Composable
private fun InfoCardRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono dentro de una “pastilla” (queda muy como tu captura)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceVariant
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.tertiary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Textos en columna
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
            }
        }
    }
}
@Composable
@Preview(showSystemUi = true)
fun InfoPreview(){
    Info();
}