package com.induztek.protocolos.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * MODELO (capa Model de MVVM): empresa cliente.
 *
 * Es una clase independiente de User y de TipoUsuario. Ya no existe el rol
 * "Empresa": una empresa es simplemente un cliente que se registra con su
 * nombre y luego se reutiliza en el formulario de pruebas.
 *
 * Solo guarda:
 *   - id: generado por SQLite.
 *   - nombre: nombre de la empresa (único para no duplicarla).
 *
 * ¿Cómo se comunica?
 *   AppDatabase -> EmpresaDao -> EmpresaViewModel -> PruebaFormScreen
 *
 * En la prueba se guarda el NOMBRE de la empresa (clienteInstalacion), no una
 * FK, para mantener el ejemplo simple como el del profesor.
 */
@Entity(
    tableName = "empresas",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Empresa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
) {
    companion object {
        // Largo máximo del nombre para evitar textos desmedidos.
        const val MAX_LARGO = 80
    }
}
