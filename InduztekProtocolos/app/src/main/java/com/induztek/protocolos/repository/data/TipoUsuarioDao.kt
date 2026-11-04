package com.induztek.protocolos.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.induztek.protocolos.repository.models.TipoUsuario
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) del catálogo de tipos de usuario.
 *
 * Room genera la implementación de esta interfaz. El ViewModel la usa para
 * leer y modificar la tabla "tipo_usuarios".
 *
 * ¿Cómo se comunica?
 *   TipoUsuarioScreen -> TipoUsuarioViewModel -> TipoUsuarioDao -> SQLite
 *   UserFormScreen    -> TipoUsuarioViewModel -> TipoUsuarioDao (dropdown)
 *
 * Los métodos que devuelven Flow actualizan la UI automáticamente cada vez
 * que cambia la tabla. Los métodos suspend se ejecutan fuera del hilo principal.
 */
@Dao
interface TipoUsuarioDao {

    // READ: todos los tipos ordenados por id. Flow permite que el dropdown se
    // refresque solo cuando se agrega o edita un tipo.
    @Query("SELECT * FROM tipo_usuarios ORDER BY id ASC")
    fun getAll(): Flow<List<TipoUsuario>>

    // READ puntual: se usa al editar o al validar una relación.
    @Query("SELECT * FROM tipo_usuarios WHERE id = :id")
    suspend fun getById(id: Int): TipoUsuario?

    // CREATE: ABORT evita duplicados porque tipo_usuario es UNIQUE.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tipo: TipoUsuario): Long

    // UPDATE
    @Update
    suspend fun update(tipo: TipoUsuario)

    // DELETE: puede fallar si un usuario usa este tipo (FK RESTRICT).
    @Delete
    suspend fun delete(tipo: TipoUsuario)
}
