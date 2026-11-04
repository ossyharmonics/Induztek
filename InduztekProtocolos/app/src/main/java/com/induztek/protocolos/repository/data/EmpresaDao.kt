package com.induztek.protocolos.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.induztek.protocolos.repository.models.Empresa
import kotlinx.coroutines.flow.Flow

/**
 * DAO de empresas.
 *
 * ¿Cómo se comunica?
 *   PruebaFormScreen -> EmpresaViewModel -> EmpresaDao -> SQLite
 *
 * getAllEmpresas() devuelve un Flow: el desplegable del formulario se actualiza
 * solo cuando se registra una empresa nueva.
 */
@Dao
interface EmpresaDao {

    // READ: empresas ordenadas alfabéticamente para el desplegable.
    @Query("SELECT * FROM empresas ORDER BY nombre ASC")
    fun getAllEmpresas(): Flow<List<Empresa>>

    // CREATE: ABORT lanza SQLiteConstraintException si el nombre ya existe,
    // porque la columna nombre es UNIQUE. EmpresaViewModel captura el error.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmpresa(empresa: Empresa): Long
}
