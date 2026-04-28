package com.example.apphotel.view.screens

import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.apphotel.R
import com.example.apphotel.navigation.Routes
import com.example.apphotel.viewModel.ProfileViewModel

@Composable
fun ProfileView(
    navigationController: NavController,
    onToggleTheme: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {

    val state by vm.ui.collectAsState()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) {
            navigationController.navigate(Routes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val themeIconId = if (state.isDarkTheme) {
        R.drawable.sundark
    } else {
        R.drawable.sunlight
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )


        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                vm.toggleTheme()
                onToggleTheme()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary)
        ){
            Icon(painter = painterResource(id = themeIconId),
                contentDescription = null,
                modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cambiar tema")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { vm.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Log out")
        }
    }
}