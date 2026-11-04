package com.induztek.protocolos.view.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// ============================================================
// UTILIDADES DE FORMATO (fecha, hora y números)
// ¿Por qué centralizarlas? Para que TODAS las pantallas muestren el MISMO
// formato (dd/MM/yyyy, hora en 24 hrs, mediciones sin ceros de más) sin
// repetir código. Se ubican en view/utils, igual que los componentes del profesor.
// ============================================================

// Formato "15/10/2026" (tarjetas del historial y comparación).
private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

// Formato "10:30" (hora en 24 hrs).
private val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

// Formato "15/10/2026 - 10:30" (detalle completo de la prueba).
private val formatoFechaHora = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())

// Convierte milisegundos -> texto "dd/MM/yyyy".
fun formatearFecha(millis: Long): String = formatoFecha.format(Date(millis))

// Convierte milisegundos -> texto "HH:mm".
fun formatearHora(millis: Long): String = formatoHora.format(Date(millis))

// Convierte milisegundos -> texto "dd/MM/yyyy - HH:mm".
fun formatearFechaHora(millis: Long): String = formatoFechaHora.format(Date(millis))

// Muestra una medición sin decimales innecesarios: 220.0 -> "220" y 15.7 -> "15.7".
fun formatearMedicion(valor: Double): String = DecimalFormat("0.##").format(valor)

// Diferencia CON SIGNO para la pantalla Comparar: "+5", "-0.8" o "0".
// El signo permite ver de inmediato si la medición subió o bajó.
fun formatearDiferencia(valor: Double): String {
    val texto = DecimalFormat("0.##").format(abs(valor))
    return when {
        valor > 0 -> "+$texto"
        valor < 0 -> "-$texto"
        else -> "0"
    }
}
