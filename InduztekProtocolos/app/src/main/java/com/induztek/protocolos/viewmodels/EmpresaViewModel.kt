package com.induztek.protocolos.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.repository.data.AppDatabase
import com.induztek.protocolos.repository.models.Empresa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * VIEWMODEL DE EMPRESAS (capa ViewModel de MVVM).
 *
 * Se comunica con:
 *   - EmpresaDao: lista reactiva e inserción.
 *   - PruebaFormScreen: muestra las empresas en el desplegable y permite
 *     crear una nueva en el mismo momento.
 *
 * Sigue el patrón simple del profesor: AndroidViewModel + DAO directo,
 * sin repositorio.
 */
class EmpresaViewModel(application: Application) : AndroidViewModel(application) {

    private val empresaDao = AppDatabase.getDatabase(application).empresaDao()

    // READ: lista reactiva para el desplegable de clientes.
    val empresas: StateFlow<List<Empresa>> = empresaDao.getAllEmpresas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensaje para errores de negocio (nombre vacío o duplicado).
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    /**
     * CREATE: registra una empresa con solo su nombre.
     *
     * onSuccess se ejecuta en el hilo principal para que el formulario pueda
     * seleccionar la empresa recién creada y cerrar el diálogo.
     */
    fun crearEmpresa(nombre: String, onSuccess: () -> Unit) {
        val valor = nombre.trim()

        if (valor.isEmpty()) {
            _message.value = "El nombre de la empresa es obligatorio"
            return
        }

        if (valor.length > Empresa.MAX_LARGO) {
            _message.value = "El nombre no puede superar los ${Empresa.MAX_LARGO} caracteres"
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    empresaDao.insertEmpresa(Empresa(nombre = valor))
                }

                _message.value = null
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "La empresa \"$valor\" ya existe"
            }
        }
    }
}
