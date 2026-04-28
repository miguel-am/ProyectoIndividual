package com.example.examenprueba1.view.scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.apphotel.R
import com.example.apphotel.navigation.Routes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarViewOn(
    onInfoClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHideClick: () -> Unit
){
    val colors = MaterialTheme.colorScheme
    TopAppBar(

        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.icono_proyecto),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        },
        title = {
            Text("Pere Maria", fontWeight = FontWeight.Bold)
        },
        actions = { //Icones d'acció per a pantalles addicionals
            IconButton(onClick = onInfoClick){ Icon(imageVector = Icons.Default.Info, contentDescription=null) }
            IconButton(onClick = onProfileClick){ Icon(imageVector = Icons.Default.AccountCircle, contentDescription=null) }
            IconButton(onClick = onHideClick){ Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription=null) }
        },
        colors = TopAppBarDefaults.topAppBarColors( //Definició dels colors del contingut del TopAppBar
            containerColor = colors.primary,
            titleContentColor = colors.background,//Icona Principal
            actionIconContentColor = colors.onPrimary //Icones d'acció
        )
    )
}
