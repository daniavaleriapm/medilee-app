package com.ejemplo.medilee.presentation.camara

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ejemplo.medilee.TarjetaAmarilla
import com.ejemplo.medilee.TarjetaVerde
import com.ejemplo.medilee.TextoSerif
import com.ejemplo.medilee.VerdeOscuro
import com.ejemplo.medilee.integration.camara.tomarFoto

@Composable
fun CamaraScreen(
    onFotoCapturada: (rutaFoto: String) -> Unit,
    viewModel: CamaraViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { LifecycleCameraController(context) }

    // Permiso de cámara: se pide al entrar a la pantalla (momento de uso)
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> viewModel.onPermisoResultado(concedido) }

    LaunchedEffect(Unit) {
        val yaConcedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (yaConcedido) viewModel.onPermisoResultado(true)
        else lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    // Cuando hay foto nueva, se entrega a la siguiente pantalla (una sola vez)
    LaunchedEffect(estado.rutaFoto) {
        estado.rutaFoto?.let { ruta ->
            onFotoCapturada(ruta)
            viewModel.onFotoEntregada()
        }
    }

    val forma = RoundedCornerShape(18.dp)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .shadow(4.dp, forma)
                .clip(forma)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when {
                estado.permisoConcedido -> {
                    // Vista previa en vivo
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().height(460.dp),
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                this.controller = controller
                                controller.bindToLifecycle(lifecycleOwner)
                            }
                        }
                    )
                    // Botón de captura
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                            .size(68.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .clickable(enabled = !estado.capturando) {
                                viewModel.onCapturaIniciada()
                                tomarFoto(
                                    context = context,
                                    controller = controller,
                                    alExito = viewModel::onFotoCapturada,
                                    alFallar = viewModel::onErrorCaptura
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Tomar foto del medicamento",
                            tint = VerdeOscuro,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                estado.permisoDenegado -> {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextoSerif(
                            "Necesitamos acceso a la cámara para leer tu medicamento",
                            Color.White, 16, align = TextAlign.Center
                        )
                        TarjetaVerde(
                            onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) }
                        ) {
                            TextoSerif("Dar permiso", size = 14)
                        }
                    }
                }

                else -> TextoSerif("Preparando cámara…", Color.White, 16)
            }
        }

        // Mensaje de error recuperable (el usuario puede volver a intentar)
        estado.mensajeError?.let { mensaje ->
            TarjetaAmarilla(Modifier.fillMaxWidth()) {
                TextoSerif(mensaje, Color(0xFF5E8A74), 14, align = TextAlign.Center)
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}
