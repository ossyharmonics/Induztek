package com.induztek.protocolos.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.induztek.protocolos.repository.models.User
import com.induztek.protocolos.viewmodels.TipoUsuarioViewModel
import com.induztek.protocolos.viewmodels.UserViewModel

/**
 * LISTA DE USUARIOS (capa View).
 *
 * Se comunica con:
 *   - UserViewModel: users (Flow reactivo), message y deleteUser().
 *   - TipoUsuarioViewModel: tipos, para traducir tipoUsuarioId a su nombre.
 *   - AppNavigation: currentUserId (no permitir borrarse a sí mismo),
 *     onAdd y onEdit.
 *
 * Es la misma pantalla del profesor: tarjetas con foto/nombre/email/tipo,
 * botones editar/eliminar y FAB para agregar.
 */
@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    currentUserId: Int?,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val tipos by tipoUsuarioViewModel.tipos.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    // Mapa id -> nombre del tipo para mostrarlo en cada tarjeta.
    val nombreTipo = tipos.associate { it.id to it.tipoUsuario }

    var toDelete by remember { mutableStateOf<User?>(null) }

    // Al salir de la pantalla se limpia el mensaje para que no reaparezca.
    DisposableEffect(Unit) {
        onDispose { viewModel.clearMessage() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (users.isEmpty()) {
            Text(
                "No hay usuarios. Presiona + para agregar.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Foto de perfil si existe; si no, ícono genérico.
                            if (user.profileImageUri != null) {
                                AsyncImage(
                                    model = user.profileImageUri,
                                    contentDescription = user.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(52.dp).clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, style = MaterialTheme.typography.titleMedium)
                                Text(user.email, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    nombreTipo[user.tipoUsuarioId] ?: "Sin tipo",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = { onEdit(user.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            // No se permite eliminar al usuario con sesión activa.
                            IconButton(
                                onClick = { toDelete = user },
                                enabled = user.id != currentUserId
                            ) {
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

        // Botón flotante para crear un usuario nuevo.
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Agregar usuario")
        }
    }

    // Diálogo de confirmación antes de borrar.
    toDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Eliminar a ${user.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(user)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar") }
            }
        )
    }

    // Mensaje del ViewModel (por ejemplo, no se puede borrar porque tiene pruebas).
    message?.let { texto ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            title = { Text("Aviso") },
            text = { Text(texto) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) { Text("Aceptar") }
            }
        )
    }
}
