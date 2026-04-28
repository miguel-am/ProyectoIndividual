package com.example.examenprueba1.view.scaffold
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.apphotel.navigation.Navigation
import com.example.apphotel.navigation.Routes
import com.example.apphotel.ui.theme.AppHotelTheme


@Composable
fun MyApp() {
    var darkTheme by remember { mutableStateOf(false) }


    val navController = rememberNavController()
    var barsVisible by remember { mutableStateOf(true) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scaffoldRoutes = setOf(
        Routes.Home.route,
        Routes.Rooms.route,
        Routes.Reservations.route,
        Routes.Profile.route,
        Routes.InfoRoom.route,
        Routes.Filters.route,
        Routes.Info.route
    )

    val shouldShowScaffold = currentRoute in scaffoldRoutes

    AppHotelTheme(darkTheme = darkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

            topBar = {
                if (shouldShowScaffold && barsVisible) {
                    TopAppBarViewOn(
                        onInfoClick = { navController.navigate(Routes.Info.route) },
                        onProfileClick = { navController.navigate(Routes.Profile.route) },
                        onHideClick = { barsVisible = false }
                    )
                }
            },

            bottomBar = {
                if (shouldShowScaffold && barsVisible) {
                    NavigationBarState(navController)
                }
            },

            floatingActionButton = {
                if (shouldShowScaffold && !barsVisible) {
                    FloatingActionButton(onClick = { barsVisible = true},
                        containerColor = MaterialTheme.colorScheme.primary
                    ){
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    }
                }
            }
        ) { innerPadding ->
            Navigation(
                navigationController = navController,
                modifier = Modifier.padding(innerPadding),
                darkTheme = darkTheme,
                onToggleTheme = { darkTheme = !darkTheme }
            )
        }
    }
}