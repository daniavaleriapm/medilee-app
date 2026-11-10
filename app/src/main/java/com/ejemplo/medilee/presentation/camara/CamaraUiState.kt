package com.ejemplo.medilee.presentation.camara

/**
 * Estado observable de la pantalla de cámara.
 * Solo contiene datos para la vista (sin permisos ni acceso a archivos).
 */
data class CamaraUiState(
    val permisoConcedido: Boolean = false,
    val permisoDenegado: Boolean = false,
    val capturando: Boolean = false,
    val rutaFoto: String? = null,
    val mensajeError: String? = null
)
