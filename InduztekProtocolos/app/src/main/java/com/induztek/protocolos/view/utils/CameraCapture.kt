package com.induztek.protocolos.view.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

/**
 * COMPONENTE REUTILIZABLE DE CÁMARA (CameraX), copiado del patrón del profesor.
 *
 * ¿Cómo se comunica?
 *   - PruebaFormScreen lo abre con carpeta = "pruebas".
 *   - UserFormScreen lo abre con carpeta = "usuarios" para la foto de perfil.
 *   - CameraScreen lo abre con carpeta = "camara".
 *   - Cuando el usuario captura, devuelve la Uri por onImageCaptured.
 *     La pantalla que lo invocó guarda esa Uri en su estado local.
 *   - onClose avisa que el usuario cerró la cámara.
 *
 * La foto se guarda en el almacenamiento interno privado:
 *   /data/data/com.induztek.protocolos/files/fotos/<carpeta>/img_<timestamp>.jpg
 * No requiere permisos de almacenamiento y no aparece en la Galería.
 */
@Composable
fun CameraCapture(
    carpeta: String,
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Estado del permiso de cámara en tiempo de ejecución.
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher que muestra el diálogo de permiso.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    // Al entrar por primera vez, si no hay permiso, se solicita.
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (hasPermission) {
        CameraPreviewContent(carpeta, onImageCaptured, onClose, modifier)
    } else {
        // Sin permiso: se explica y se ofrece volver a pedirlo o cerrar.
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Se necesita el permiso de cámara para continuar.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Conceder permiso")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onClose) { Text("Volver") }
        }
    }
}

/**
 * Vista previa + captura.
 * Se separa de CameraCapture para que el bloque de permiso quede corto.
 */
@Composable
private fun CameraPreviewContent(
    carpeta: String,
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Lente actual: trasera por defecto.
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }

    // PreviewView es la vista Android donde CameraX dibuja la cámara.
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    // ImageCapture es el caso de uso que toma la foto.
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Vincula la cámara al ciclo de vida de la pantalla. Se vuelve a ejecutar
    // cuando cambia lensFacing (botón de cambiar cámara).
    DisposableEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo iniciar la cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            if (cameraProviderFuture.isDone) cameraProviderFuture.get().unbindAll()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {

        // Vista previa de la cámara.
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Botón para cerrar la cámara.
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        // Controles inferiores: capturar y cambiar de lente.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(56.dp))

            FilledIconButton(
                onClick = { takePhoto(context, carpeta, imageCapture, onImageCaptured) },
                modifier = Modifier.size(76.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = "Capturar",
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Cameraswitch,
                    contentDescription = "Cambiar cámara",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Guarda la foto en el almacenamiento interno privado y devuelve su Uri.
 * La carpeta se crea si no existe. El nombre usa timestamp para no sobrescribir.
 */
private fun takePhoto(
    context: Context,
    carpeta: String,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val dir = File(context.filesDir, "fotos/$carpeta").apply { mkdirs() }
    val outputFile = File(dir, "img_${System.currentTimeMillis()}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(outputFile)
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(Uri.fromFile(outputFile))
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(
                    context,
                    "Error al capturar: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}
