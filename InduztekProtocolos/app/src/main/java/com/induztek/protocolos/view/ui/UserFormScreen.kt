package com.induztek.protocolos.view.ui

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.induztek.protocolos.view.utils.AnimacionEntradaFormulario
import com.induztek.protocolos.view.utils.CameraCapture
import com.induztek.protocolos.view.utils.PhotoBox
import com.induztek.protocolos.viewmodels.TipoUsuarioViewModel
import com.induztek.protocolos.viewmodels.UserViewModel

/**
 * FORMULARIO DE USUARIO (capa View).
 *
 * Se comunica con:
 *   - UserViewModel: getUser() para editar, saveUser() para guardar.
 *   - TipoUsuarioViewModel: tipos para el dropdown de rol.
 *   - CameraCapture/PhotoBox: foto de perfil.
 *
 * Es el mismo patrón del UserFormScreen del profesor:
 *   - El estado del formulario vive en la pantalla con rememberSaveable.
 *   - El ViewModel solo valida el nombre y persiste en Room.
 *   - Al guardar, onSuccess llama a onDone para volver a la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    viewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    userId: Int,
    onDone: () -> Unit,
    bloquearTipoUsuario: Boolean = false
) {
    val tipos by tipoUsuarioViewModel.tipos.collectAsStateWithLifecycle()

    // Estado local del formulario.
    var tipoUsuarioId by rememberSaveable { mutableIntStateOf(0) }
    var tipoExpanded by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    val dbMessage by viewModel.message.collectAsStateWithLifecycle()

    // Limpia mensajes del ViewModel al salir del formulario.
    DisposableEffect(Unit) {
        onDispose { viewModel.clearMessage() }
    }

    // Si userId > 0, carga el usuario desde Room para editarlo.
    LaunchedEffect(userId) {
        if (userId > 0 && !loaded) {
            viewModel.getUser(userId)?.let {
                name = it.name
                email = it.email
                password = it.password
                imageUri = it.profileImageUri
                tipoUsuarioId = it.tipoUsuarioId
            }
        }
        loaded = true
        visible = true
    }

    // La cámara ocupa toda la pantalla mientras está abierta.
    if (showCamera) {
        CameraCapture(
            carpeta = "usuarios",
            onImageCaptured = { uri ->
                imageUri = uri.toString()
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    // ANIMACIÓN 1: fade + deslizamiento vertical al entrar al formulario.
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                when {
                    bloquearTipoUsuario -> "Mi perfil"
                    userId > 0 -> "Editar Usuario"
                    else -> "Nuevo Usuario"
                },
                style = MaterialTheme.typography.headlineMedium
            )

            // Foto de perfil circular: al tocarla se abre CameraCapture.
            PhotoBox(
                imageUri = imageUri,
                circle = true,
                onTakePhoto = { showCamera = true }
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearMessage()
                },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Tipo de usuario (FK a tipo_usuarios).
            // En "Mi perfil" se muestra solo lectura para que el técnico no
            // pueda cambiarse el rol y obtener permisos de administrador.
            val nombreTipo = tipos.firstOrNull { it.id == tipoUsuarioId }?.tipoUsuario ?: ""

            if (bloquearTipoUsuario) {
                OutlinedTextField(
                    value = nombreTipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de usuario") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = tipoExpanded,
                    onExpandedChange = { tipoExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nombreTipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de usuario") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = tipoExpanded,
                        onDismissRequest = { tipoExpanded = false }
                    ) {
                        tipos.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.tipoUsuario) },
                                onClick = {
                                    tipoUsuarioId = tipo.id
                                    tipoExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Error local del formulario o mensaje del ViewModel (email repetido).
            (error ?: dbMessage)?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "El nombre es obligatorio"
                        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                            error = "Email inválido"
                        password.length < 4 ->
                            error = "La contraseña debe tener al menos 4 caracteres"
                        tipos.none { it.id == tipoUsuarioId } ->
                            error = "Seleccione un tipo de usuario"
                        else -> {
                            error = null
                            viewModel.saveUser(
                                id = userId,
                                name = name.trim(),
                                email = email.trim(),
                                password = password,
                                imageUri = imageUri,
                                tipoUsuarioId = tipoUsuarioId,
                                onSuccess = onDone
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Usuario")
            }

            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
