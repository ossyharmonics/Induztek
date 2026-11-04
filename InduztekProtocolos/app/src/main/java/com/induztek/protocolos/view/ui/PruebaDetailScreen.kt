package com.induztek.protocolos.view.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.induztek.protocolos.repository.models.Prueba
import com.induztek.protocolos.view.utils.EstadoRecurso
import com.induztek.protocolos.view.utils.formatearFechaHora
import com.induztek.protocolos.view.utils.formatearMedicion
import com.induztek.protocolos.viewmodels.PruebaViewModel
import com.induztek.protocolos.viewmodels.UserViewModel

/**
 * DETALLE DE UNA PRUEBA (capa View).
 *
 * Se comunica con:
 *   - PruebaViewModel.getPrueba(id) para cargar la fila desde Room.
 *   - UserViewModel.users para traducir usuarioId -> nombre del autor.
 *   - AppNavigation.onComparar para abrir la pantalla de comparación.
 *
 * El estado de carga vive en esta pantalla con remember/LaunchedEffect,
 * siguiendo el mismo patrón que ProductFormScreen del profesor cuando
 * necesita una lectura puntual del ViewModel.
 */
@Composable
fun PruebaDetailScreen(
    viewModel: PruebaViewModel,
    userViewModel: UserViewModel,
    idPrueba: Long,
    onComparar: () -> Unit
) {
    // Estado local de la lectura puntual.
    var prueba by remember { mutableStateOf<Prueba?>(null) }
    var cargando by remember { mutableStateOf(true) }

    val usuarios by userViewModel.users.collectAsStateWithLifecycle()
    val nombreUsuario = usuarios.associate { it.id to it.name }

    // Cada vez que cambia el id, se vuelve a cargar la prueba.
    LaunchedEffect(idPrueba) {
        cargando = true
        prueba = viewModel.getPrueba(idPrueba)
        cargando = false
    }

    val datos = prueba

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (datos == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontró la prueba.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ---------- Encabezado ----------
        Text(
            datos.codigoEquipo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            datos.tipoEquipo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        // ---------- Datos técnicos ----------
        FilaDato("Empresa", datos.clienteInstalacion)
        FilaDato("Fecha y hora", formatearFechaHora(datos.fechaHora))
        FilaDato("Medición 1", formatearMedicion(datos.medicion1))
        FilaDato("Medición 2", formatearMedicion(datos.medicion2))
        FilaDato(
            "Observaciones",
            datos.observaciones.ifBlank { "Sin observaciones" }
        )

        // Autoría: el nombre sale de la tabla users mediante usuarioId.
        FilaDato(
            "Registrada por",
            nombreUsuario[datos.usuarioId] ?: "Usuario #${datos.usuarioId}"
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // ---------- Foto de evidencia ----------
        if (datos.rutaFoto != null) {
            AsyncImage(
                model = datos.rutaFoto,
                contentDescription = "Foto de la prueba",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(8.dp))
            EstadoRecurso(
                icono = Icons.Filled.CheckCircle,
                texto = "Foto registrada",
                capturado = true
            )
        } else {
            EstadoRecurso(
                icono = Icons.Filled.CameraAlt,
                texto = "Sin foto registrada",
                capturado = false
            )
        }

        Spacer(Modifier.height(12.dp))

        // ---------- Ubicación ----------
        val lat = datos.latitud
        val lng = datos.longitud

        if (lat != null && lng != null) {
            EstadoRecurso(
                icono = Icons.Filled.CheckCircle,
                texto = "Ubicación registrada (${"%.5f".format(lat)}, ${"%.5f".format(lng)})",
                capturado = true
            )
        } else {
            EstadoRecurso(
                icono = Icons.Filled.LocationOn,
                texto = "Sin ubicación registrada",
                capturado = false
            )
        }

        Spacer(Modifier.height(24.dp))

        // ---------- Comparar con la medición anterior ----------
        Button(
            onClick = onComparar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comparar anterior", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Fila "Etiqueta: valor" reutilizada en el detalle.
 * Se mantiene privada porque es un formato exclusivo de esta pantalla.
 */
@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
