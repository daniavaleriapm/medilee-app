package com.ejemplo.medilee.presentation.analisis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ejemplo.medilee.MediLeeApplication
import com.ejemplo.medilee.TarjetaAmarilla
import com.ejemplo.medilee.TarjetaVerde
import com.ejemplo.medilee.TextoSerif
import com.ejemplo.medilee.TextoVerde
import com.ejemplo.medilee.VerdeOscuro
import com.ejemplo.medilee.core.common.TipoError
import com.ejemplo.medilee.domain.model.Medicamento

@Composable
fun AnalisisScreen(
    rutaFoto: String,
    onResultado: (Medicamento) -> Unit,
    onVolverACamara: () -> Unit,
    viewModel: AnalisisViewModel = viewModel(
        factory = AnalisisViewModel.fabrica(
            (LocalContext.current.applicationContext as MediLeeApplication)
                .contenedor.medicamentoRepository
        )
    )
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(rutaFoto) { viewModel.iniciarAnalisis(rutaFoto) }

    LaunchedEffect(estado.medicamento) {
        estado.medicamento?.let { medicamento ->
            onResultado(medicamento)
            viewModel.onResultadoEntregado()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val error = estado.tipoError

        if (estado.cargando) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .shadow(6.dp, CircleShape)
                    .background(VerdeOscuro, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MoreHoriz, null, tint = Color.White, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(32.dp))
            TarjetaAmarilla(Modifier.fillMaxWidth()) {
                TextoSerif(
                    "Analizando el medicamento, un momento",
                    TextoVerde, 14, align = TextAlign.Center
                )
            }
        } else if (error != null) {
            TarjetaAmarilla(Modifier.fillMaxWidth()) {
                TextoSerif(mensajeDeError(error), TextoVerde, 14, align = TextAlign.Center)
            }
            Spacer(Modifier.height(16.dp))

            if (error == TipoError.SIN_CONEXION || error == TipoError.DESCONOCIDO) {
                TarjetaVerde(Modifier.fillMaxWidth(), onClick = { viewModel.reintentar() }) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextoSerif("Reintentar", size = 14)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            TarjetaVerde(Modifier.fillMaxWidth(), onClick = onVolverACamara) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextoSerif("Volver a tomar foto", size = 14)
                }
            }
        }
    }
}

private fun mensajeDeError(tipo: TipoError): String = when (tipo) {
    TipoError.SIN_TEXTO -> "No pude leer texto en la foto. Intenta con más luz y acerca el empaque."
    TipoError.SIN_CONEXION -> "Sin conexión a internet. Revisa tu conexión e intenta de nuevo."
    TipoError.NO_ENCONTRADO -> "No encontré este medicamento. Intenta con otra foto del nombre."
    TipoError.DESCONOCIDO -> "Ocurrió un problema al analizar. Intenta de nuevo."
}
