package com.induztek.protocolos.viewmodels

/**
 * Reglas de validación compartidas por los ViewModels y las pantallas.
 *
 * ¿Por qué existe esta clase?
 *   El profesor concentra en Validaciones.kt las reglas que usan varios
 *   ViewModels. Así la UI no inventa reglas por su cuenta: solo llama a estas
 *   funciones puras (sin Android, sin Compose) y muestra el mensaje devuelto.
 *
 * Contrato de las funciones de validación:
 *   - Devuelven null si el valor es correcto.
 *   - Devuelven un String con el mensaje si hay error.
 */
object Validaciones {

    // Letra válida: mayúsculas, minúsculas, ñ, tildes y diéresis.
    private const val LETRA = "[A-Za-zÁÉÍÓÚáéíóúÜüÑñ]"

    /**
     * Solo letras y UN espacio entre palabras.
     * Válidos: "Juan Pérez", "Administrador", "Jefe Ventas".
     * Inválidos: " Juan", "Juan ", "Juan  Pérez", "Juan3", "Jefe-Ventas".
     *
     * Se usa en UserViewModel (nombre del usuario) y en TipoUsuarioViewModel
     * (nombre del tipo), exactamente como en el proyecto del profesor.
     */
    private val SOLO_LETRAS_Y_ESPACIOS = Regex("^$LETRA+( $LETRA+)*$")

    fun esSoloLetras(valor: String): Boolean = SOLO_LETRAS_Y_ESPACIOS.matches(valor)

    // ------------------------------------------------------------------
    // VALIDACIONES DEL FORMULARIO DE PRUEBAS
    // La pantalla PruebaFormScreen llama a estas funciones antes de guardar.
    // ------------------------------------------------------------------

    fun validarTipoEquipo(tipo: String): String? =
        if (tipo.isBlank()) "Seleccione un tipo de equipo" else null

    fun validarCodigoEquipo(codigo: String): String? =
        if (codigo.isBlank()) "El código es obligatorio" else null

    fun validarCliente(cliente: String): String? =
        if (cliente.isBlank()) "Debe seleccionar una empresa" else null

    /**
     * Medición obligatoria y numérica.
     * Acepta coma decimal porque el teclado en español usa coma (ej: "15,7").
     */
    fun validarMedicion(valor: String, numeroMedicion: Int): String? {
        if (valor.isBlank()) return "La medición $numeroMedicion es obligatoria"

        val numero = valor.replace(",", ".").toDoubleOrNull()
        return if (numero == null) "La medición $numeroMedicion debe ser un valor numérico" else null
    }

    /**
     * Convierte el texto YA VALIDADO a Double.
     * Se llama solo después de que validarMedicion devolvió null.
     */
    fun textoAMedicion(valor: String): Double = valor.replace(",", ".").trim().toDouble()
}
