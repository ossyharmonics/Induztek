package com.induztek.protocolos.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.repository.data.AppDatabase
import com.induztek.protocolos.repository.models.TipoUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * VIEWMODEL DE TIPOS DE USUARIO (capa ViewModel de MVVM).
 *
 * La tabla tipo_usuarios nace con dos registros (Administrador,
 * Técnico) inyectados por AppDatabase.seedCallback. Esta pantalla permite
 * administrarlos igual que el profesor: crear, editar y eliminar.
 *
 * Se comunica con:
 *   - TipoUsuarioDao para persistir.
 *   - TipoUsuarioScreen para mostrar la lista y el formulario.
 *   - UserFormScreen para llenar el dropdown de tipo de usuario.
 */
class TipoUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).tipoUsuarioDao()

    // READ: catálogo reactivo.
    val tipos: StateFlow<List<TipoUsuario>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensajes de error de negocio (duplicado, largo inválido, FK en uso).
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    /**
     * CREATE (id = 0) o UPDATE (id > 0).
     * Valida largo máximo y que solo tenga letras/espacios, igual que el profesor.
     */
    fun save(id: Int, nombre: String, onSuccess: () -> Unit = {}) {
        val valor = nombre.trim()

        if (valor.isEmpty() || valor.length > TipoUsuario.MAX_LARGO) {
            _message.value = "El tipo debe tener entre 1 y ${TipoUsuario.MAX_LARGO} caracteres"
            return
        }

        if (!Validaciones.esSoloLetras(valor)) {
            _message.value = "El tipo solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val tipo = TipoUsuario(id = id, tipoUsuario = valor)
                    if (id == 0) dao.insert(tipo) else dao.update(tipo)
                }

                _message.value = null
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "El tipo \"$valor\" ya existe"
            }
        }
    }

    /**
     * DELETE. Falla si algún usuario tiene este tipo (FK RESTRICT).
     * El mensaje se muestra en TipoUsuarioScreen.
     */
    fun delete(tipo: TipoUsuario) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.delete(tipo) }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar \"${tipo.tipoUsuario}\": hay usuarios con ese tipo"
            }
        }
    }
}
