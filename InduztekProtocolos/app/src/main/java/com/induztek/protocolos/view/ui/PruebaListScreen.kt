package com.induztek.protocolos.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.induztek.protocolos.view.utils.formatearFechaHora
import com.induztek.protocolos.viewmodels.PruebaViewModel
import com.induztek.protocolos.viewmodels.UserViewModel

/**
 * LISTA / HISTORIAL DE PRUEBAS (capa View).

 *   - Lista reactiva desde PruebaViewModel.pruebas.
 *   - FAB para crear una prueba nueva.
 *   - Botones editar y eliminar en cada tarjeta.
 *   - Al tocar una tarjeta se abre el detalle.
 *
 * Además conserva el buscador de Induztek (filtra por código, tipo o empresa)
 * y muestra el nombre del autor usando UserViewModel.users.
 *
 * ¿Cómo se comunica?
 *   PruebaViewModel -> pruebas (Flow de Room)
 *   UserViewModel   -> users (para traducir usuarioId -> nombre)
 *   AppNavigation   -> onAdd, onEdit y onDetail
 */
@Composable
fun PruebaListScreen(
    viewModel: PruebaViewModel,
    userViewModel: UserViewModel,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onDetail: (Long) -> Unit
) {
    val pruebas by viewModel.pruebas.collectAsStateWithLifecycle()
    val usuarios by userViewModel.users.collectAsStateWithLifecycle()

    // Mapa id -> nombre para mostrar quién registró cada prueba.
    val nombreUsuario = usuarios.associate { it.id to it.name }

    // El buscador vive en la pantalla, igual que el estado local del profesor.
    var busqueda by rememberSaveable { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<Prueba?>(null) }

    // Filtro local: no toca la base de datos, solo la lista ya cargada.
    val listaFiltrada = if (busqueda.isBlank()) {
        pruebas
    } else {
        pruebas.filter { prueba ->
            prueba.codigoEquipo.contains(busqueda, ignoreCase = true) ||
                prueba.tipoEquipo.contains(busqueda, ignoreCase = true) ||
                prueba.clienteInstalacion.contains(busqueda, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // ---------- BUSCADOR ----------
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar equipo...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ---------- LISTA O MENSAJE VACÍO ----------
            if (listaFiltrada.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (pruebas.isEmpty()) {
                            "Aún no hay pruebas. Presiona + para agregar."
                        } else {
                            "No se encontraron pruebas para \"$busqueda\""
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaFiltrada, key = { it.id }) { prueba ->
                        Card(
                            onClick = { onDetail(prueba.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Miniatura de la foto de evidencia, si existe.
                                if (prueba.rutaFoto != null) {
                                    AsyncImage(
                                        model = prueba.rutaFoto,
                                        contentDescription = prueba.codigoEquipo,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        prueba.codigoEquipo,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        prueba.tipoEquipo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        prueba.clienteInstalacion,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        formatearFechaHora(prueba.fechaHora),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    // Autoría: usuarioId es la FK hacia users.
                                    Text(
                                        "Registrada por: ${
                                            nombreUsuario[prueba.usuarioId]
                                                ?: "Usuario #${prueba.usuarioId}"
                                        }",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = { onEdit(prueba.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }

                                IconButton(onClick = { toDelete = prueba }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB para crear una prueba nueva.
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar prueba")
        }
    }

    // Confirmación antes de eliminar.
    toDelete?.let { prueba ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar prueba") },
            text = { Text("¿Eliminar la prueba \"${prueba.codigoEquipo}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePrueba(prueba)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar") }
            }
        )
    }
}
