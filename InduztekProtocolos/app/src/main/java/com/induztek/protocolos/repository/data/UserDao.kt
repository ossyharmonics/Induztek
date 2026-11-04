package com.induztek.protocolos.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.induztek.protocolos.repository.models.User
import kotlinx.coroutines.flow.Flow

/**
 * DAO de usuarios.
 *
 * Es el puente entre SQLite y los ViewModels de login y de administración
 * de usuarios:
 *
 *   LoginScreen   -> LoginViewModel -> UserDao.login()
 *   UserListScreen-> UserViewModel  -> UserDao.getAllUsers() / deleteUser()
 *   UserFormScreen-> UserViewModel  -> UserDao.insertUser() / updateUser()
 *
 * Room implementa esta interfaz automáticamente (UserDao_Impl).
 */
@Dao
interface UserDao {

    // READ: lista reactiva de usuarios ordenada por nombre.
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    // READ puntual: se usa para cargar el formulario de edición.
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    // LOGIN: valida email + contraseña contra la tabla. LIMIT 1 porque el email es único.
    // Esta consulta es la que usa LoginViewModel.
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // CREATE: ABORT hace que Room lance SQLiteConstraintException si el email
    // ya existe. El UserViewModel captura esa excepción y muestra un mensaje.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    // UPDATE
    @Update
    suspend fun updateUser(user: User)

    // DELETE: puede fallar si el usuario tiene pruebas (FK RESTRICT).
    @Delete
    suspend fun deleteUser(user: User)
}
