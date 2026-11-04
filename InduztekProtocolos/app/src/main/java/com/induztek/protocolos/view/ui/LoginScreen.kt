package com.induztek.protocolos.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.induztek.protocolos.view.utils.pulsoYRotacion
import com.induztek.protocolos.viewmodels.LoginViewModel

/**
 * PANTALLA DE LOGIN (capa View).
 *
 * Se comunica ÚNICAMENTE con LoginViewModel:
 *   - Observa uiState con collectAsStateWithLifecycle.
 *   - Escribe email/contraseña llamando a los handlers del ViewModel.
 *   - El botón llama a viewModel.login().
 *   - La pantalla NO consulta la base de datos ni valida credenciales:
 *     solo dibuja el estado que el ViewModel publica.
 *
 * Cuando uiState.loggedUser deja de ser null, LaunchedEffect ejecuta
 * onLoginSuccess, y AppNavigation navega a Home borrando el Login de la pila.
 *
 *
 * logo animado, dos campos, error, spinner y credenciales de prueba.
 */
@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Observa el resultado del login. Si hay usuario, avisa a la navegación.
    LaunchedEffect(state.loggedUser) {
        if (state.loggedUser != null) onLoginSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo de la marca con la animación de pulso + rotación (ANIMACIÓN 2).
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(96.dp)
                .pulsoYRotacion()
        )

        Spacer(Modifier.height(16.dp))

        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // Campo email. Cada tecla actualiza el estado del ViewModel.
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Campo contraseña. PasswordVisualTransformation oculta los caracteres.
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Error publicado por el ViewModel (campos vacíos o credenciales inválidas).
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        // Botón de ingreso. Se bloquea mientras el ViewModel consulta Room.
        Button(
            onClick = viewModel::login,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Ingresar")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Usuario sembrado por AppDatabase.seedCallback.
        Text(
            "Usuario de prueba: admin@duoc.cl / admin123",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
