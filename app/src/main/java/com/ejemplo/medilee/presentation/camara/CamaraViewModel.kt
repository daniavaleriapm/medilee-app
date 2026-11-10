package com.ejemplo.medilee.presentation.camara

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Coordina los eventos de la pantalla de cámara y expone el estado.
 * No guarda referencias a Activity ni a Context.
 */
class CamaraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CamaraUiState())
    val uiState: StateFlow<CamaraUiState> = _uiState.asStateFlow()

    fun onPermisoResultado(concedido: Boolean) {
        _uiState.update {
            it.copy(permisoConcedido = concedido, permisoDenegado = !concedido)
        }
    }

    fun onCapturaIniciada() {
        _uiState.update { it.copy(capturando = true, mensajeError = null) }
    }

    fun onFotoCapturada(ruta: String) {
        _uiState.update { it.copy(capturando = false, rutaFoto = ruta) }
    }

    fun onErrorCaptura(mensaje: String) {
        _uiState.update { it.copy(capturando = false, mensajeError = mensaje) }
    }

    /** La pantalla ya entregó la foto a la siguiente vista; se limpia para no repetirla. */
    fun onFotoEntregada() {
        _uiState.update { it.copy(rutaFoto = null) }
    }
}
