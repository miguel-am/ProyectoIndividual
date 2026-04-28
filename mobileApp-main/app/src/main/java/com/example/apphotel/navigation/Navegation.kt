package com.example.apphotel.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.apphotel.data.SessionManager
import androidx.navigation.navArgument
import com.example.apphotel.Model.Rooms
import com.example.apphotel.Model.respository.ReviewRepository
import com.example.apphotel.Model.respository.RoomsRepository
import com.example.apphotel.view.screens.ConfirmReservationScreen
import com.example.apphotel.view.screens.Filters
import com.example.apphotel.view.screens.Home
import com.example.apphotel.view.screens.Info
import com.example.apphotel.view.screens.InfoRoom
import com.example.apphotel.view.screens.Login
import com.example.apphotel.view.screens.ProfileView
import com.example.apphotel.view.screens.ReservationsScreen
import com.example.apphotel.view.screens.ReviewScreen
import com.example.apphotel.view.screens.RoomView
import com.example.apphotel.view.screens.ViewReviewScreen
import com.example.apphotel.viewModel.LoginViewModel
import com.example.apphotel.viewModel.ReservationViewModel
import com.example.apphotel.viewModel.ReviewViewModel
import com.example.apphotel.viewModel.RoomsViewModel
import com.example.apphotel.viewModel.SearchRoomsViewModel
import com.example.apphotel.viewModel.UserReservationViewModel

@Composable
fun Navigation(
    navigationController: NavHostController,
    modifier: Modifier,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit) {

    val repository = remember { RoomsRepository() }
    val repositoryReview = remember { ReviewRepository() }

    val vmR = remember { RoomsViewModel() }
    val svm = remember { SearchRoomsViewModel() }
    val rvm = remember { UserReservationViewModel(repository) }
    val vmRe = remember { ReservationViewModel(repository) }
    NavHost(
        navController = navigationController,
        startDestination = Routes.Login.route,
        modifier = modifier
    ) {
        composable(Routes.Login.route) {
            val vm: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            Login(
                vm,
                onLoggedIn = {
                navigationController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Login.route) { inclusive = true }
                }
                })
        }
        composable(Routes.Home.route) {
            Home(svm,
                vmR,
                navigationController)
        }

        composable(
            route = Routes.Rooms.route,
            arguments = listOf(
                navArgument("checkIn") { type = NavType.StringType },
                navArgument("checkOut") { type = NavType.StringType },
                navArgument("guests") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val checkIn = backStackEntry.arguments?.getString("checkIn").orEmpty()
            val checkOut = backStackEntry.arguments?.getString("checkOut").orEmpty()
            val guests = backStackEntry.arguments?.getInt("guests") ?: 1
            RoomView(
                vmR,
                navigationController = navigationController,
                checkIn = checkIn,
                checkOut = checkOut,
                guests = guests
            )
        }
        composable(
            route = "confirm_booking/{checkIn}/{checkOut}/{guests}",
            arguments = listOf(
                navArgument("checkIn") { type = NavType.StringType },
                navArgument("checkOut") { type = NavType.StringType },
                navArgument("guests") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val checkIn = backStackEntry.arguments?.getString("checkIn").orEmpty()
            val checkOut = backStackEntry.arguments?.getString("checkOut").orEmpty()
            val guests = backStackEntry.arguments?.getInt("guests") ?: 1

            ConfirmReservationScreen(
                viewModel = vmR,
                reservationViewModel = vmRe,
                navigationController = navigationController,
                checkIn = checkIn,
                checkOut = checkOut,
                guests = guests
            )
        }
        composable(Routes.Reservations.route) {
            val token = SessionManager.userToken ?: ""
            ReservationsScreen(viewModel = rvm, token = token,navigationController)
        }
        composable(Routes.Filters.route) {
            Filters(vmR,navigationController )
        }

        composable(
            route = Routes.InfoRoom.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            InfoRoom(id = id, vm = vmR)
        }


        composable(Routes.Info.route){
            Info()
        }
        composable(Routes.Profile.route){
            ProfileView(
                onToggleTheme = onToggleTheme,
                navigationController = navigationController
            )
        }
        composable(
            route = Routes.Review.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("reservationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
            val token = SessionManager.userToken ?: ""

            val reviewViewModel = remember { ReviewViewModel(repositoryReview) }

            ReviewScreen(
                viewModel = reviewViewModel,
                token = token,
                roomId = roomId,
                reservationId = reservationId,
                onBack = { navigationController.popBackStack() }
            )
        }
        composable(
            route = Routes.ViewReview.route,
            arguments = listOf(navArgument("reservationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
            val token = SessionManager.userToken ?: ""

            val reviewViewModel = remember { ReviewViewModel(repositoryReview) }

            ViewReviewScreen(
                token = token,
                reservationId = reservationId,
                viewModel = reviewViewModel,
                onBack = { navigationController.popBackStack() }
            )
        }
    }
}