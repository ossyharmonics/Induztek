package com.induztek.protocolos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.repository.data.AppDatabase
import com.induztek.protocolos.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ESTADO DE LA PANTALLA DE LOGIN.
 *
 * un único data class inmutable que la
 * pantalla observa. Cada cambio se hace con copy(), y la UI se redibuja sola.
 *
 * Campos:
 *   - email/password: lo que el usuario escribe.
 *   - isLoading: muestra el spinner del botón.
 *   - error: mensaje rojo si las credenciales fallan.
 *   - loggedUser: usuario autenticado. Si es distinto de null, AppNavigation
 *     deja entrar a Home y el drawer puede mostrar su nombre y correo.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedUser: User? = null
)

/**
 * VIEWMODEL DE LOGIN (capa ViewModel de MVVM).
 *
 * ¿Cómo se comunica?
 *   LoginScreen escribe -> llama onEmailChange/onPasswordChange/login()
 *   LoginViewModel       -> valida, consulta UserDao y actualiza LoginUiState
 *   UserDao              -> ejecuta el SELECT en SQLite
 *   AppNavigation        -> observa loggedUser y navega a Home
 *
 * Se usa AndroidViewModel porque necesita el Application para obtener la
 * base de datos con AppDatabase.getDatabase(application),
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Acceso directo al DAO,
    // Aquí no hay repositorio: el profesor mantiene el ejemplo simple.
    private val userDao = AppDatabase.getDatabase(application).userDao()

    // Estado privado mutable: solo este ViewModel puede cambiarlo.
    private val _uiState = MutableStateFlow(LoginUiState())

    // Estado público de solo lectura: la pantalla lo observa y no lo modifica.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Cada tecla del email actualiza el estado y limpia el error anterior.
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    // Cada tecla de la contraseña actualiza el estado y limpia el error anterior.
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    /**
     * Intenta iniciar sesión.
     *
     * Flujo:
     *   1) Valida que ambos campos tengan contenido.
     *   2) Marca isLoading para bloquear el botón y mostrar el spinner.
     *   3) Ejecuta UserDao.login() en Dispatchers.IO (consulta a SQLite).
     *   4) Si encuentra usuario, guarda loggedUser y limpia la contraseña.
     *      Si no, publica "Credenciales inválidas".
     */
    fun login() {
        val state = _uiState.value

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Ingrese email y contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Pequeña pausa para que el usuario alcance a ver el indicador de carga.
            delay(600)

            val user = withContext(Dispatchers.IO) {
                userDao.login(state.email.trim(), state.password)
            }

            _uiState.update {
                if (user != null) {
                    it.copy(isLoading = false, loggedUser = user, password = "")
                } else {
                    it.copy(isLoading = false, error = "Credenciales inválidas")
                }
            }
        }
    }

    /**
     * Cierra la sesión en memoria.
     * AppNavigation escucha loggedUser == null y navega de vuelta al Login.
     */
    fun logout() {
        _uiState.value = LoginUiState()
    }
}
