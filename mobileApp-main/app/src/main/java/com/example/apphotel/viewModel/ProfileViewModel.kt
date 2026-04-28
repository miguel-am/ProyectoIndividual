package com.example.apphotel.viewModel

import androidx.lifecycle.ViewModel
import com.example.apphotel.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val isDarkTheme: Boolean = false,
    val loggedOut: Boolean = false
)

class ProfileViewModel : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui

    fun toggleTheme() {
        _ui.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun logout() {
        SessionManager.clearSession()
        _ui.update { it.copy(loggedOut = true) }
    }
}