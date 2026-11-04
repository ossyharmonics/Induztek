package com.induztek.protocolos.view.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.repository.models.Prueba
import com.induztek.protocolos.view.utils.formatearDiferencia
import com.induztek.protocolos.view.utils.formatearFecha
import com.induztek.protocolos.view.utils.formatearMedicion
import com.induztek.protocolos.viewmodels.PruebaViewModel

/**
 * COMPARACIÓN CON LA PRUEBA ANTERIOR (capa View).
 *
 * Se comunica con PruebaViewModel:
 *   1) getPrueba(id) -> prueba actual.
 *   2) getPreviousPrueba(codigo, fecha) -> medición previa del mismo equipo.
 *
 * El estado de la comparación vive en la pantalla (remember/LaunchedEffect),
 * igual que las lecturas puntuales del profesor. No se usa Repository ni
 * clases UiState adicionales: se mantiene la estructura simple.
 */
@Composable
fun CompararScreen(
    viewModel: PruebaViewModel,
    idPrueba: Long,
    onVolver: () -> Unit
) {
    var actual by remember { mutableStateOf<Prueba?>(null) }
    var anterior by remember { mutableStateOf<Prueba?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(idPrueba) {
        cargando = true

        val pruebaActual = viewModel.getPrueba(idPrueba)
        actual = pruebaActual
        anterior = pruebaActual?.let {
            viewModel.getPreviousPrueba(it.codigoEquipo, it.fechaHora)
        }

        cargando = false
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val pruebaActual = actual

    if (pruebaActual == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontró la prueba.")
        }
        return
    }

    val pruebaAnterior = anterior

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "${pruebaActual.codigoEquipo} - ${pruebaActual.tipoEquipo}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        if (pruebaAnterior == null) {
            // No es un error: simplemente todavía no hay una medición previa.
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "No existe una prueba anterior registrada para este equipo. " +
                        "Cuando registre una nueva medición del mismo código, " +
                        "podrá compararlas aquí.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // ---------- TABLA COMPARATIVA ----------
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("", modifier = Modifier.weight(1.2f))
                Text(
                    "Anterior",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Actual",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            FilaTabla(
                "Fecha",
                formatearFecha(pruebaAnterior.fechaHora),
                formatearFecha(pruebaActual.fechaHora)
            )

            FilaTabla(
                "Medición 1",
                formatearMedicion(pruebaAnterior.medicion1),
                formatearMedicion(pruebaActual.medicion1)
            )
            FilaTabla(
                "Diferencia",
                "",
                formatearDiferencia(pruebaActual.medicion1 - pruebaAnterior.medicion1)
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            FilaTabla(
                "Medición 2",
                formatearMedicion(pruebaAnterior.medicion2),
                formatearMedicion(pruebaActual.medicion2)
            )
            FilaTabla(
                "Diferencia",
                "",
                formatearDiferencia(pruebaActual.medicion2 - pruebaAnterior.medicion2)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Observación:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                generarObservacion(pruebaAnterior, pruebaActual),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al detalle")
        }
    }
}

/**
 * Fila de la tabla: etiqueta + valor anterior + valor actual.
 * Se separa como componente privado para no repetir el mismo Row.
 */
@Composable
private fun FilaTabla(
    etiqueta: String,
    valorAnterior: String,
    valorActual: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            etiqueta,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            valorAnterior,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            valorActual,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Observación automática: compara ambas mediciones y redacta el resultado.
 * Es una función pura para que la regla sea fácil de leer y probar.
 */
private fun generarObservacion(anterior: Prueba, actual: Prueba): String {
    val diferencia1 = actual.medicion1 - anterior.medicion1
    val diferencia2 = actual.medicion2 - anterior.medicion2

    return when {
        diferencia1 > 0 && diferencia2 > 0 ->
            "Los valores aumentaron respecto a la medición anterior."

        diferencia1 < 0 && diferencia2 < 0 ->
            "Los valores disminuyeron respecto a la medición anterior."

        diferencia1 == 0.0 && diferencia2 == 0.0 ->
            "Los valores se mantienen igual respecto a la medición anterior."

        else ->
            "Los valores presentan variaciones mixtas respecto a la medición anterior."
    }
}
