package com.induztek.protocolos.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * MODELO (capa Model de MVVM): usuario de la aplicación.
 *
 * Esta entidad representa la tabla "users" de SQLite y se comunica con:
 *   - TipoUsuario mediante la FK tipoUsuarioId (cada usuario tiene un rol).
 *   - Prueba mediante la FK usuarioId (cada prueba registra quién la hizo).
 *
 * El login del sistema se resuelve con UserDao.login(), que busca una fila
 * por email y contraseña. Por eso el email debe ser único.
 *
 * La contraseña se guarda en texto plano porque el proyecto es académico,
 * exactamente como en el trabajo del profesor (no se implementa hash).
 */
@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = TipoUsuario::class,
            parentColumns = ["id"],
            childColumns = ["tipoUsuarioId"],
            onDelete = ForeignKey.RESTRICT, // No se puede borrar un tipo que esté en uso
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["email"], unique = true), // Evita dos usuarios con el mismo email
        Index(value = ["tipoUsuarioId"])         // Índice obligatorio para la FK
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,                 // Solo para fines académicos (sin hash)
    val profileImageUri: String? = null,  // Ruta local de la foto de perfil (puede ser null)
    val tipoUsuarioId: Int                // FK -> tipo_usuarios.id
)
