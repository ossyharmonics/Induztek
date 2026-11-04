package com.induztek.protocolos.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.repository.data.AppDatabase
import com.induztek.protocolos.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * VIEWMODEL DE USUARIOS (capa ViewModel de MVVM).
 *
 * Se comunica con:
 *   - UserDao: lectura reactiva, creación, edición y borrado.
 *   - UserListScreen: observa users y message.
 *   - UserFormScreen: llama saveUser() y getUser().
 *   - PruebaListScreen / PruebaDetailScreen: usan users para mostrar el
 *     nombre del autor de cada prueba.
 *
 * Igual que el profesor:
 *   - users es un StateFlow respaldado por un Flow de Room.
 *   - message es un StateFlow para errores de negocio (email duplicado,
 *     usuario con pruebas asociadas).
 *   - saveUser valida el nombre con Validaciones.esSoloLetras.
 */
class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    // READ: lista reactiva. WhileSubscribed(5000) deja de escuchar si no hay
    // ninguna pantalla observando, para no consumir recursos de más.
    val users: StateFlow<List<User>> = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensajes que la vista muestra al usuario (por ejemplo, email duplicado).
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    // Lectura puntual para cargar el formulario de edición.
    suspend fun getUser(id: Int): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    /**
     * CREATE / UPDATE.
     *
     * id = 0 -> inserta; id > 0 -> actualiza.
     * onSuccess se ejecuta en el hilo principal para que la pantalla pueda
     * cerrar el formulario solo cuando la operación terminó bien.
     *
     * Se valida el nombre con la misma regla del profesor: solo letras y un
     * espacio entre palabras. Si el email está repetido, Room lanza
     * SQLiteConstraintException y se muestra un mensaje amigable.
     */
    fun saveUser(
        id: Int,
        name: String,
        email: String,
        password: String,
        imageUri: String?,
        tipoUsuarioId: Int,
        onSuccess: () -> Unit
    ) {
        if (!Validaciones.esSoloLetras(name)) {
            _message.value = "El nombre solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val user = User(
                        id = id,
                        name = name,
                        email = email,
                        password = password,
                        profileImageUri = imageUri,
                        tipoUsuarioId = tipoUsuarioId
                    )

                    if (id == 0) userDao.insertUser(user) else userDao.updateUser(user)
                }

                _message.value = null
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "Ya existe un usuario con el email $email"
            }
        }
    }

    /**
     * DELETE.
     *
     * La tabla pruebas tiene FK usuarioId con RESTRICT. Si el usuario tiene
     * pruebas registradas, SQLite lanza SQLiteConstraintException y NO se
     * borra. Se informa con message para que UserListScreen lo muestre.
     */
    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { userDao.deleteUser(user) }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar a ${user.name}: tiene pruebas registradas"
            }
        }
    }
}
