package com.induztek.protocolos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.repository.data.AppDatabase
import com.induztek.protocolos.repository.models.Prueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * VIEWMODEL DE PRUEBAS (capa ViewModel de MVVM).
 *
 * Este ViewModel reemplaza al antiguo PruebaViewModel + Repository.
 * Ahora sigue el patrón simple del profesor: el ViewModel usa el DAO
 * directamente y expone a la UI:
 *   - pruebas: lista reactiva (Flow -> StateFlow).
 *   - totalPruebas: contador reactivo para Inicio.
 *   - funciones suspend para detalle y comparación.
 *   - savePrueba / deletePrueba para las acciones del usuario.
 *
 * ¿Cómo se comunica?
 *   PruebaListScreen   -> observa pruebas
 *   HomeScreen         -> observa totalPruebas
 *   PruebaFormScreen   -> llama savePrueba() y getPrueba()
 *   PruebaDetailScreen -> llama getPrueba()
 *   CompararScreen     -> llama getPrueba() y getPreviousPrueba()
 *
 * La pantalla maneja su propio estado local del formulario (igual que
 * ProductFormScreen del profesor); el ViewModel solo persiste.
 */
class PruebaViewModel(application: Application) : AndroidViewModel(application) {

    // Acceso directo al DAO, sin capa Repository, igual que el profesor.
    private val pruebaDao = AppDatabase.getDatabase(application).pruebaDao()

    // READ: historial reactivo. Room emite una nueva lista al insertar/editar/borrar.
    val pruebas: StateFlow<List<Prueba>> = pruebaDao.getAllPruebas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // READ: contador reactivo de pruebas registradas.
    val totalPruebas: StateFlow<Int> = pruebaDao.countPruebas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Lectura puntual para el detalle y la edición.
    suspend fun getPrueba(id: Long): Prueba? = withContext(Dispatchers.IO) {
        pruebaDao.getPruebaById(id)
    }

    // Lectura de la prueba anterior del mismo equipo para la comparación.
    suspend fun getPreviousPrueba(codigoEquipo: String, fechaHora: Long): Prueba? =
        withContext(Dispatchers.IO) {
            pruebaDao.getPreviousPrueba(codigoEquipo, fechaHora)
        }

    /**
     * CREATE / UPDATE.
     *
     * id = 0L -> inserta una prueba nueva.
     * id > 0L -> actualiza la prueba existente.
     *
     * usuarioId llega desde PruebaFormScreen:
     *   - En una prueba nueva es el usuario logueado.
     *   - En una edición se conserva el autor original cargado desde Room.
     *
     * Se ejecuta en Dispatchers.IO porque es una operación de base de datos.
     */
    fun savePrueba(
        id: Long,
        tipoEquipo: String,
        codigoEquipo: String,
        clienteInstalacion: String,
        medicion1: Double,
        medicion2: Double,
        observaciones: String,
        fechaHora: Long,
        rutaFoto: String?,
        latitud: Double?,
        longitud: Double?,
        usuarioId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val prueba = Prueba(
                id = id,
                tipoEquipo = tipoEquipo,
                codigoEquipo = codigoEquipo,
                clienteInstalacion = clienteInstalacion,
                medicion1 = medicion1,
                medicion2 = medicion2,
                observaciones = observaciones,
                fechaHora = fechaHora,
                rutaFoto = rutaFoto,
                latitud = latitud,
                longitud = longitud,
                usuarioId = usuarioId
            )

            if (id == 0L) {
                pruebaDao.insertPrueba(prueba)
            } else {
                pruebaDao.updatePrueba(prueba)
            }
        }
    }

    // DELETE: la lista pide confirmación antes de llamar aquí.
    fun deletePrueba(prueba: Prueba) {
        viewModelScope.launch(Dispatchers.IO) {
            pruebaDao.deletePrueba(prueba)
        }
    }
}
