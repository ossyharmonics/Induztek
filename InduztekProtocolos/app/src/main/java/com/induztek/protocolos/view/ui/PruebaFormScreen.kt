package com.induztek.protocolos.view.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.induztek.protocolos.view.utils.AnimacionEntradaFormulario
import com.induztek.protocolos.view.utils.CameraCapture
import com.induztek.protocolos.view.utils.EstadoRecurso
import com.induztek.protocolos.view.utils.PhotoBox
import com.induztek.protocolos.view.utils.formatearFecha
import com.induztek.protocolos.view.utils.formatearHora
import com.induztek.protocolos.viewmodels.EmpresaViewModel
import com.induztek.protocolos.viewmodels.PruebaViewModel
import com.induztek.protocolos.viewmodels.Validaciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FORMULARIO DE PRUEBA (capa View).
 *
 *   - Todo el estado del formulario vive aquí con rememberSaveable.
 *   - El ViewModel solo persiste en Room (savePrueba).
 *   - Utiliza los componentes reutilizables PhotoBox y CameraCapture.
 *
 * Diferencia clave solicitada: cada prueba guarda el usuarioId del autor.
 *   - Si la prueba es nueva, se usa currentUserId (el usuario logueado).
 *   - Si se está editando, se conserva el usuarioId cargado desde Room.
 *
 * La pantalla NO navega por su cuenta: al guardar o cancelar llama a onDone,
 * y AppNavigation decide volver atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruebaFormScreen(
    viewModel: PruebaViewModel,
    empresaViewModel: EmpresaViewModel,
    pruebaId: Long,
    currentUserId: Int?,
    onDone: () -> Unit
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cliente de ubicación (recurso nativo GPS).
    val clienteUbicacion = remember {
        LocationServices.getFusedLocationProviderClient(contexto.applicationContext)
    }

    // ------------------ ESTADO LOCAL DEL FORMULARIO ------------------
    var tipoEquipo by rememberSaveable { mutableStateOf("") }
    var codigoEquipo by rememberSaveable { mutableStateOf("") }
    var clienteInstalacion by rememberSaveable { mutableStateOf("") }
    var medicion1 by rememberSaveable { mutableStateOf("") }

    // Empresas registradas: se muestran en el desplegable de cliente.
    val empresas by empresaViewModel.empresas.collectAsStateWithLifecycle()
    val mensajeEmpresa by empresaViewModel.message.collectAsStateWithLifecycle()
    var medicion2 by rememberSaveable { mutableStateOf("") }
    var observaciones by rememberSaveable { mutableStateOf("") }
    var rutaFoto by rememberSaveable { mutableStateOf<String?>(null) }
    var latitud by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitud by rememberSaveable { mutableStateOf<Double?>(null) }
    var fechaHora by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var usuarioId by rememberSaveable { mutableIntStateOf(currentUserId ?: 0) }

    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var mensajeUbicacion by rememberSaveable { mutableStateOf<String?>(null) }
    var cargandoUbicacion by rememberSaveable { mutableStateOf(false) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var menuEmpresaAbierto by remember { mutableStateOf(false) }
    var mostrarDialogoEmpresa by rememberSaveable { mutableStateOf(false) }
    var nuevaEmpresa by rememberSaveable { mutableStateOf("") }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    // ------------------ CARGA PARA EDITAR ------------------
    // Si pruebaId > 0, se carga la prueba desde Room. Si es 0, es una nueva.
    LaunchedEffect(pruebaId) {
        if (pruebaId > 0L && !loaded) {
            viewModel.getPrueba(pruebaId)?.let { prueba ->
                tipoEquipo = prueba.tipoEquipo
                codigoEquipo = prueba.codigoEquipo
                clienteInstalacion = prueba.clienteInstalacion
                medicion1 = prueba.medicion1.toString()
                medicion2 = prueba.medicion2.toString()
                observaciones = prueba.observaciones
                rutaFoto = prueba.rutaFoto
                latitud = prueba.latitud
                longitud = prueba.longitud
                fechaHora = prueba.fechaHora
                usuarioId = prueba.usuarioId // Se conserva el autor original
            }
        }
        loaded = true
        visible = true
    }

    // ------------------ CÁMARA (CameraX) ------------------
    // CameraCapture ocupa la pantalla completa y devuelve la Uri de la foto.
    if (showCamera) {
        CameraCapture(
            carpeta = "pruebas",
            onImageCaptured = { uri ->
                rutaFoto = uri.toString()
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    // ------------------ UBICACIÓN (GPS) ------------------
    // Obtiene una ubicación fresca. Si el permiso falta, la pantalla lo solicita.
    // Los mensajes se muestran en el formulario, sin bloquear el guardado.
    @SuppressLint("MissingPermission")
    fun obtenerUbicacion() {
        scope.launch {
            cargandoUbicacion = true
            mensajeUbicacion = null

            try {
                val permisoFino = ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val permisoGrueso = ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!permisoFino && !permisoGrueso) {
                    mensajeUbicacion =
                        "Permiso de ubicación denegado. Puede guardar la prueba sin ubicación."
                    return@launch
                }

                // Si hay permiso fino se busca mayor precisión; si no, se ahorra batería.
                val prioridad = if (permisoFino) {
                    Priority.PRIORITY_HIGH_ACCURACY
                } else {
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY
                }

                val ubicacion = withContext(Dispatchers.IO) {
                    clienteUbicacion
                        .getCurrentLocation(prioridad, CancellationTokenSource().token)
                        .await()
                }

                if (ubicacion != null) {
                    latitud = ubicacion.latitude
                    longitud = ubicacion.longitude
                } else {
                    mensajeUbicacion =
                        "No se pudo obtener la ubicación. Verifique el GPS e intente nuevamente."
                }
            } catch (e: Exception) {
                mensajeUbicacion =
                    "Error al obtener la ubicación: ${e.message ?: "desconocido"}"
            } finally {
                cargandoUbicacion = false
            }
        }
    }

    // Launcher que pide los dos permisos de ubicación juntos.
    val launcherPermisoUbicacion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.any { it }) {
            obtenerUbicacion()
        } else {
            cargandoUbicacion = false
            mensajeUbicacion =
                "Permiso de ubicación denegado. Puede guardar la prueba sin ubicación."
        }
    }

    // Si ya hay permiso, consulta; si no, lo solicita.
    fun onClickObtenerUbicacion() {
        val fino = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val grueso = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fino || grueso) {
            obtenerUbicacion()
        } else {
            launcherPermisoUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ------------------ INTERFAZ ------------------
    // ANIMACIÓN 1: entrada con fade + deslizamiento vertical.
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
                if (pruebaId > 0L) "Editar Prueba" else "Nueva Prueba",
                style = MaterialTheme.typography.headlineMedium
            )

            // Foto de evidencia: al tocar el recuadro se abre CameraCapture.
            PhotoBox(
                imageUri = rutaFoto,
                circle = false,
                onTakePhoto = { showCamera = true }
            )

            // ---------- TIPO DE EQUIPO (dropdown) ----------
            var menuAbierto by remember { mutableStateOf(false) }

            val tiposEquipo = listOf(
                "Transformador",
                "Relé de protección",
                "Celda",
                "Cable",
                "Otro"
            )

            ExposedDropdownMenuBox(
                expanded = menuAbierto,
                onExpandedChange = { menuAbierto = !menuAbierto }
            ) {
                OutlinedTextField(
                    value = tipoEquipo,
                    onValueChange = {}, // Solo se elige del menú, no se escribe
                    readOnly = true,
                    label = { Text("Tipo de equipo") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto)
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    tiposEquipo.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoEquipo = tipo
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            // ---------- CAMPOS DE TEXTO ----------
            OutlinedTextField(
                value = codigoEquipo,
                onValueChange = { codigoEquipo = it },
                label = { Text("Código del equipo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- EMPRESA / CLIENTE ----------
            // Desplegable con las empresas ya registradas. Si la empresa no
            // existe, se puede crear en el mismo formulario con "+ Nueva empresa".
            ExposedDropdownMenuBox(
                expanded = menuEmpresaAbierto,
                onExpandedChange = { menuEmpresaAbierto = !menuEmpresaAbierto }
            ) {
                OutlinedTextField(
                    value = clienteInstalacion,
                    onValueChange = {}, // Solo se elige del menú, no se escribe
                    readOnly = true,
                    label = { Text("Empresa / cliente") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuEmpresaAbierto)
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = menuEmpresaAbierto,
                    onDismissRequest = { menuEmpresaAbierto = false }
                ) {
                    if (empresas.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay empresas registradas") },
                            onClick = {},
                            enabled = false
                        )
                    }

                    empresas.forEach { empresa ->
                        DropdownMenuItem(
                            text = { Text(empresa.nombre) },
                            onClick = {
                                clienteInstalacion = empresa.nombre
                                menuEmpresaAbierto = false
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Nueva empresa") },
                        leadingIcon = {
                            Icon(Icons.Filled.Add, contentDescription = null)
                        },
                        onClick = {
                            menuEmpresaAbierto = false
                            nuevaEmpresa = ""
                            empresaViewModel.clearMessage()
                            mostrarDialogoEmpresa = true
                        }
                    )
                }
            }

            OutlinedTextField(
                value = medicion1,
                onValueChange = { medicion1 = it },
                label = { Text("Medición 1") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = medicion2,
                onValueChange = { medicion2 = it },
                label = { Text("Medición 2") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- FECHA Y HORA AUTOMÁTICAS ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Fecha: ${formatearFecha(fechaHora)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Hora: ${formatearHora(fechaHora)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // ---------- UBICACIÓN ----------
            OutlinedButton(
                onClick = { onClickObtenerUbicacion() },
                enabled = !cargandoUbicacion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (latitud == null) "Obtener ubicación"
                    else "Actualizar ubicación"
                )
            }

            if (cargandoUbicacion) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Obteniendo ubicación...")
                }
            }

            // Se usan variables locales para que Kotlin pueda hacer smart-cast.
            val lat = latitud
            val lng = longitud

            if (lat != null && lng != null) {
                EstadoRecurso(
                    icono = Icons.Filled.CheckCircle,
                    texto = "Ubicación registrada (${"%.5f".format(lat)}, ${"%.5f".format(lng)})",
                    capturado = true
                )
            }

            mensajeUbicacion?.let { mensaje ->
                EstadoRecurso(
                    icono = Icons.Filled.LocationOn,
                    texto = mensaje,
                    capturado = false,
                    esError = true
                )
            }

            // ---------- ERROR DE VALIDACIÓN ----------
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // ---------- GUARDAR ----------
            Button(
                onClick = {
                    val errorTipo = Validaciones.validarTipoEquipo(tipoEquipo)
                    val errorCodigo = Validaciones.validarCodigoEquipo(codigoEquipo)
                    val errorCliente = Validaciones.validarCliente(clienteInstalacion)
                    val errorMedicion1 = Validaciones.validarMedicion(medicion1, 1)
                    val errorMedicion2 = Validaciones.validarMedicion(medicion2, 2)

                    when {
                        currentUserId == null ->
                            error = "No hay un usuario en sesión"

                        errorTipo != null -> error = errorTipo
                        errorCodigo != null -> error = errorCodigo
                        errorCliente != null -> error = errorCliente
                        errorMedicion1 != null -> error = errorMedicion1
                        errorMedicion2 != null -> error = errorMedicion2

                        else -> {
                            error = null

                            viewModel.savePrueba(
                                id = pruebaId,
                                tipoEquipo = tipoEquipo,
                                codigoEquipo = codigoEquipo.trim(),
                                clienteInstalacion = clienteInstalacion.trim(),
                                medicion1 = Validaciones.textoAMedicion(medicion1),
                                medicion2 = Validaciones.textoAMedicion(medicion2),
                                observaciones = observaciones.trim(),
                                fechaHora = fechaHora,
                                rutaFoto = rutaFoto,
                                latitud = latitud,
                                longitud = longitud,
                                usuarioId = usuarioId
                            )

                            onDone()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Guardar Prueba")
            }

            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }

    // ---------- DIÁLOGO PARA CREAR EMPRESA ----------
    // Se abre desde "+ Nueva empresa" del desplegable. Al guardar, la empresa
    // queda seleccionada en el formulario y también queda registrada para las
    // próximas pruebas.
    if (mostrarDialogoEmpresa) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEmpresa = false
                nuevaEmpresa = ""
                empresaViewModel.clearMessage()
            },
            title = { Text("Nueva empresa") },
            text = {
                OutlinedTextField(
                    value = nuevaEmpresa,
                    onValueChange = {
                        nuevaEmpresa = it
                        empresaViewModel.clearMessage()
                    },
                    label = { Text("Nombre de la empresa") },
                    singleLine = true,
                    isError = mensajeEmpresa != null,
                    supportingText = {
                        mensajeEmpresa?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nombre = nuevaEmpresa.trim()
                        empresaViewModel.crearEmpresa(nombre) {
                            clienteInstalacion = nombre
                            mostrarDialogoEmpresa = false
                            nuevaEmpresa = ""
                            empresaViewModel.clearMessage()
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEmpresa = false
                        nuevaEmpresa = ""
                        empresaViewModel.clearMessage()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
