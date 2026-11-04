package com.induztek.protocolos.view.ui

import androidx.compose.foundation.layout.Column              // Columna vertical
import androidx.compose.foundation.layout.Row                 // Fila horizontal
import androidx.compose.foundation.layout.Spacer              // Espaciador
import androidx.compose.foundation.layout.fillMaxSize         // Ocupa toda la pantalla
import androidx.compose.foundation.layout.fillMaxWidth        // Ocupa todo el ancho
import androidx.compose.foundation.layout.height              // Alto del espaciador
import androidx.compose.foundation.layout.padding             // Márgenes internos
import androidx.compose.foundation.layout.width               // Ancho del espaciador
import androidx.compose.material.icons.Icons                  // Acceso a íconos Material
import androidx.compose.material.icons.filled.Add             // Ícono "+"
import androidx.compose.material.icons.filled.Assignment      // Ícono de la tarjeta contador
import androidx.compose.material.icons.filled.History         // Ícono de historial
import androidx.compose.material3.Button                      // Botón principal
import androidx.compose.material3.Card                        // Tarjeta del contador
import androidx.compose.material3.CardDefaults                // Colores de tarjeta
import androidx.compose.material3.Icon                        // Ícono
import androidx.compose.material3.MaterialTheme               // Colores/tipografías
import androidx.compose.material3.OutlinedButton              // Botón secundario (borde)
import androidx.compose.material3.Text                        // Texto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue                        // Para el delegate "by"
import androidx.compose.ui.Alignment                           // Alineación
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight                // Grosor de fuente
import androidx.compose.ui.text.style.TextAlign                // Alineación de texto
import androidx.compose.ui.unit.dp                            // Unidad de densidad
import androidx.lifecycle.compose.collectAsStateWithLifecycle  // Observa StateFlow respetando el ciclo de vida
import com.induztek.protocolos.viewmodels.PruebaViewModel       // ViewModel compartido

/**
 * PANTALLA 1 - INICIO (según el mockup original de Induztek:
 * logo, registro de pruebas en terreno, acceso a "+ Nueva prueba",
 * "Ver historial" y contador de pruebas registradas).
 *
 * Se comunica con:
 *   - PruebaViewModel.totalPruebas -> contador en vivo desde Room.
 *   - AppNavigation -> onNuevaPrueba y onVerHistorial.
 *
 * Es la misma pantalla que tenía el proyecto original;
 */
@Composable
fun HomeScreen(
    viewModel: PruebaViewModel,          // ViewModel compartido con todas las pantallas
    onNuevaPrueba: () -> Unit,           // Navegar al formulario
    onVerHistorial: () -> Unit           // Navegar al historial
) {
    // Observa el contador de pruebas: Room emite el nuevo valor cada vez que se guarda.
    // collectAsStateWithLifecycle: la UI deja de observar cuando la pantalla no está visible.
    val total by viewModel.totalPruebas.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally   // Todo centrado como el mockup
    ) {
        // Spacer con weight: empuja el contenido al centro de la pantalla.
        Spacer(modifier = Modifier.weight(1f))

        // ---------- Identidad de la aplicación ----------
        Text(
            text = "INDUZTEK",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Protocolos eléctricos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Registro de pruebas eléctricas en terreno",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ---------- Acción principal: nueva prueba ----------
        Button(
            onClick = onNuevaPrueba,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Nueva prueba")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---------- Acción secundaria: historial ----------
        OutlinedButton(
            onClick = onVerHistorial,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Filled.History, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Ver historial")
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ---------- Contador de pruebas registradas (mockup: recuadro gris) ----------
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pruebas registradas",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Número grande: viene EN VIVO de la base de datos (Room + Flow).
                Text(
                    text = "$total",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
