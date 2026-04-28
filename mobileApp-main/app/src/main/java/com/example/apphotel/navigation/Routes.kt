package com.example.apphotel.navigation

sealed class Routes(val route:String) {
    object Login: Routes("login")
    object Home: Routes("home")

    object Rooms: Routes("rooms/{checkIn}/{checkOut}/{guests}") {
        fun createRoute(checkIn: String, checkOut: String,guests: Int) =
            "rooms/$checkIn/$checkOut/$guests"
    }
    object Filters: Routes("filtros")
    object InfoRoom : Routes("infoRoom/{id}") {
        fun createRoute(id: String) = "infoRoom/$id"
    }
    object Profile: Routes("profile")

    object Reservations: Routes("reservations")

    object Info : Routes("info")

    object Review : Routes("review/{roomId}/{reservationId}") {
        fun createRoute(roomId: String, reservationId: String) =
            "review/$roomId/$reservationId"
    }

    object ViewReview : Routes("review/view/{reservationId}") {
        fun createRoute(reservationId: String) = "review/view/$reservationId"
    }
}