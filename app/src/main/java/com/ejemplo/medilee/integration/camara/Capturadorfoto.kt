package com.ejemplo.medilee.integration.camara

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Integración con CameraX: toma la foto y la guarda en un archivo temporal (cache).
 * Aquí vive el acceso al dispositivo; la pantalla y el ViewModel no tocan archivos.
 */
fun tomarFoto(
    context: Context,
    controller: LifecycleCameraController,
    alExito: (rutaArchivo: String) -> Unit,
    alFallar: (mensaje: String) -> Unit
) {
    val carpeta = File(context.cacheDir, "fotos").apply { mkdirs() }
    val archivo = File(carpeta, "medicamento_${System.currentTimeMillis()}.jpg")
    val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()

    controller.takePicture(
        opciones,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                alExito(archivo.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                alFallar("No se pudo tomar la foto. Intenta de nuevo.")
            }
        }
    )
}