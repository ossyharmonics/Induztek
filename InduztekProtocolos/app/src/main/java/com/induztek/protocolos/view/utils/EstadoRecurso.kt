package com.induztek.protocolos.view.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.view.theme.VerdeExito

/**
 * FILA DE ESTADO DE UN RECURSO (foto / ubicación).
 *
 * Muestra ícono + texto con color según el resultado:
 *   - VERDE  -> recurso capturado correctamente.
 *   - ROJO   -> error o permiso denegado.
 *   - GRIS   -> recurso todavía no capturado.
 *
 * Se usa en PruebaFormScreen, PruebaDetailScreen y CompararScreen para que
 * los estados de cámara y GPS se vean igual en toda la app.
 */
@Composable
fun EstadoRecurso(
    icono: ImageVector,
    texto: String,
    capturado: Boolean,
    esError: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Prioridad de color: error (rojo) > capturado (verde) > pendiente (gris).
    val color = when {
        esError -> MaterialTheme.colorScheme.error
        capturado -> VerdeExito
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icono,
            contentDescription = null, // Decorativo: el texto ya describe el estado
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
