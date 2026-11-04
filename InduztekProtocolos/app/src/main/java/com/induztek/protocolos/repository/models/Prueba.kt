package com.induztek.protocolos.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * MODELO (capa Model de MVVM): prueba eléctrica registrada en terreno.
 *
 * Cada fila de la tabla "pruebas" representa UNA medición hecha por un técnico.
 *
 * Relación nueva solicitada: usuarioId indica QUIÉN realizó la prueba.
 *   - Es una FK hacia users.id.
 *   - Se guarda automáticamente desde el usuario que tiene la sesión iniciada
 *     (ver PruebaFormScreen y AppNavigation).
 *   - RESTRICT evita borrar un usuario que tenga pruebas registradas, igual
 *     que el profesor protege la relación User -> TipoUsuario.
 *
 * ¿Cómo se comunica con el resto del código?
 *   AppDatabase -> PruebaDao -> PruebaViewModel -> PruebaListScreen / PruebaFormScreen
 *   AppDatabase -> UserDao   -> UserViewModel   -> UserListScreen (muestra el nombre)
 */
@Entity(
    tableName = "pruebas",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.RESTRICT, // No se borra un usuario que tenga pruebas
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])] // Índice para la FK y para búsquedas por autor
)
data class Prueba(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,                    // 0L = prueba nueva; Room asigna el id real

    val tipoEquipo: String,               // Transformador, relé, celda, cable, etc.
    val codigoEquipo: String,             // Código identificador, por ejemplo "TR-001"
    val clienteInstalacion: String,       // Nombre de la empresa cliente (texto simple, sin FK)
    val medicion1: Double,                // Primera medición eléctrica
    val medicion2: Double,                // Segunda medición eléctrica
    val observaciones: String,            // Notas del técnico (puede ir vacío)
    val fechaHora: Long,                  // Marca de tiempo en milisegundos

    val rutaFoto: String? = null,         // URI local de la foto de evidencia
    val latitud: Double? = null,          // Ubicación capturada (puede ser null)
    val longitud: Double? = null,         // Ubicación capturada (puede ser null)

    val usuarioId: Int                    // FK -> users.id: autor de la prueba
)
