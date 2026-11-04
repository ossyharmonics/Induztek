package com.induztek.protocolos.view.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * COMPONENTE DE FOTO REUTILIZABLE (igual al del profesor).
 *
 * Muestra la imagen si ya existe una Uri o un ícono de cámara si todavía no.
 * Al tocar el recuadro se abre la cámara (la pantalla decide cómo).
 *
 * Se usa en:
 *   - UserFormScreen: foto de perfil con forma circular (circle = true).
 *   - PruebaFormScreen: foto de evidencia con esquinas redondeadas (circle = false).
 *
 * La pantalla guarda la Uri devuelta por CameraCapture y la entrega a este
 * componente; PhotoBox solo dibuja.
 */
@Composable
fun PhotoBox(imageUri: String?, circle: Boolean, onTakePhoto: () -> Unit) {
    val shape = if (circle) CircleShape else RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onTakePhoto),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            // Coil carga la imagen desde la Uri local (file://...).
            AsyncImage(
                model = imageUri,
                contentDescription = "Foto",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Text("Tomar foto", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
