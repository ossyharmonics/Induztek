package com.induztek.protocolos.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.induztek.protocolos.repository.models.Prueba
import kotlinx.coroutines.flow.Flow

/**
 * DAO de pruebas eléctricas.
 *
 * Concentra TODAS las consultas SQL de la tabla "pruebas".
 *
 * ¿Cómo se comunica?
 *   PruebaListScreen   -> PruebaViewModel -> getAllPruebas() (Flow)
 *   HomeScreen         -> PruebaViewModel -> countPruebas()   (Flow)
 *   PruebaFormScreen   -> PruebaViewModel -> insertPrueba() / updatePrueba()
 *   PruebaDetailScreen -> PruebaViewModel -> getPruebaById()
 *   CompararScreen     -> PruebaViewModel -> getPreviousPrueba()
 *
 * Flow: Room emite la lista/cantidad cada vez que cambia la tabla.
 * suspend: Room ejecuta la operación fuera del hilo de UI.
 */
@Dao
interface PruebaDao {

    // CREATE: inserta una prueba nueva y devuelve el id generado por SQLite.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPrueba(prueba: Prueba): Long

    // READ: historial completo, de la prueba más reciente a la más antigua.
    @Query("SELECT * FROM pruebas ORDER BY fechaHora DESC")
    fun getAllPruebas(): Flow<List<Prueba>>

    // READ: contador que se muestra en Inicio.
    @Query("SELECT COUNT(*) FROM pruebas")
    fun countPruebas(): Flow<Int>

    // READ puntual: detalle de una prueba.
    @Query("SELECT * FROM pruebas WHERE id = :id LIMIT 1")
    suspend fun getPruebaById(id: Long): Prueba?

    // READ: prueba ANTERIOR del mismo equipo.
    // Busca el mismo código con fecha menor a la actual y toma la más cercana.
    // La usa CompararScreen para comparar la medición actual con la previa.
    @Query(
        "SELECT * FROM pruebas " +
            "WHERE codigoEquipo = :codigoEquipo AND fechaHora < :fechaHora " +
            "ORDER BY fechaHora DESC LIMIT 1"
    )
    suspend fun getPreviousPrueba(codigoEquipo: String, fechaHora: Long): Prueba?

    // UPDATE: se usa cuando el formulario se abre con un id existente.
    @Update
    suspend fun updatePrueba(prueba: Prueba)

    // DELETE: el listado de pruebas permite eliminar con confirmación.
    @Delete
    suspend fun deletePrueba(prueba: Prueba)
}
