package com.example.apphotel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apphotel.Model.respository.AuthRepository
import com.example.apphotel.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false
)

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    fun onEmailChange(v: String) = _ui.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _ui.update { it.copy(password = v, error = null) }

    fun login() {
        val email = ui.value.email.trim()
        val password = ui.value.password

        if (email.isBlank() || password.isBlank()) {
            _ui.update { it.copy(error = "Rellena email y contraseña") }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }

            val result = repo.login(email, password)

            result.onSuccess {response ->
                if (response.rol == "Usuario") {
                    SessionManager.userToken = response.token
                    _ui.update { it.copy(loading = false, loggedIn = true) }
                } else {
                    // Si el rol es Admin o Trabajador, denegamos el acceso
                    _ui.update {
                        it.copy(
                            loading = false,
                            error = "Acceso denegado: Esta app es solo para clientes."
                        )
                    }
                    SessionManager.userToken = null
                }
            }.onFailure { err ->
                val msg = when (err) {
                    is HttpException -> if (err.code() == 401) "Credenciales inválidas" else "Error HTTP ${err.code()}"
                    else -> "Error: ${err.message ?: "desconocido"}"
                }
                _ui.update { it.copy(loading = false, error = msg) }
            }
        }
    }
    fun resetLoginState() {
        _ui.update { it.copy(loggedIn = false, loading = false, error = null) }
    }
}