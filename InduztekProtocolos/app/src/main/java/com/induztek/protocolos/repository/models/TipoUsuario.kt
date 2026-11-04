package com.induztek.protocolos.repository.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * MODELO (capa Model de MVVM): tipo de usuario.
 *
 * Esta entidad representa la tabla "tipo_usuarios" de SQLite.
 * El profesor la usa como catálogo de roles y la relaciona con User mediante
 * una clave foránea. En Induztek se mantienen solo dos roles iniciales:
 *   1 = Administrador (admin)
 *   2 = Técnico
 *
 * "Empresa" ya no es un tipo de usuario: ahora es una entidad independiente
 * (repository/models/Empresa.kt) usada como catálogo de clientes.
 *
 * ¿Cómo se comunica con el resto del código?
 *   AppDatabase (Room) -> TipoUsuarioDao -> TipoUsuarioViewModel -> TipoUsuarioScreen
 *   UserViewModel / UserFormScreen consultan el catálogo para el dropdown.
 *
 * La columna se llama "tipo_usuario" (no "tipoUsuario") para que el SQL sea
 * legible, igual que en el proyecto del profesor.
 */
@Entity(
    tableName = "tipo_usuarios",
    indices = [Index(value = ["tipo_usuario"], unique = true)]
)
data class TipoUsuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "tipo_usuario") val tipoUsuario: String
) {
    companion object {
        // Largo máximo permitido al escribir un tipo en la pantalla de administración.
        // SQLite no limita el largo por sí solo: la regla se valida en el ViewModel y la vista.
        const val MAX_LARGO = 30
    }
}
