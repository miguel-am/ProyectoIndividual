package com.example.examenprueba1.view.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.apphotel.navigation.NavItems
import com.example.apphotel.navigation.Routes

@Composable
fun NavigationBarState(
    navController: NavHostController
){
    val items = listOf<NavItems>(
        NavItems("Home", Icons.Default.Home, Routes.Home.route),
        NavItems("Reservas", Icons.Default.ShoppingCart, Routes.Reservations.route)
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: items[0].route

    fun onItemSelected(route: String){
        navController.navigate(route){
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }}
    NavigationBarView(
        items = items,
        currentRoute = currentRoute,
        onItemSelected = { route -> onItemSelected(route) }
    )
}
@Composable
fun NavigationBarView(
    items:List<NavItems>,
    currentRoute:String,
    onItemSelected:(String)-> Unit
){
    val colors = MaterialTheme.colorScheme
    NavigationBar(
        contentColor=colors.onPrimary,
        containerColor= colors.primary
    ){
        for(item in items){
            NavigationBarItem(
                icon = {Icon(imageVector = item.icon, contentDescription = item.name)},
                label = { Text(item.name) },
                selected = currentRoute == item.route,
                onClick = {onItemSelected(item.route)},
                alwaysShowLabel = false,
                colors = NavigationBarItemColors(
                    selectedIconColor = colors.onPrimary,
                    selectedTextColor = colors.onPrimary,
                    selectedIndicatorColor = colors.secondary, // azul oscuro
                    unselectedIconColor = colors.onPrimary.copy(alpha = 0.6f),
                    unselectedTextColor = colors.onPrimary.copy(alpha = 0.6f),
                    disabledIconColor = colors.onPrimary.copy(alpha = 0.3f),
                    disabledTextColor = colors.onPrimary.copy(alpha = 0.3f)
                )
            )
        }
    }
}
