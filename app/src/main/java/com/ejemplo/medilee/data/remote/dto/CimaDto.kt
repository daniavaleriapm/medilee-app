package com.ejemplo.medilee.data.remote.dto

// Campos opcionales: la API puede omitir alguno y la app no debe cerrarse por eso.
data class CimaRespuestaDto(
    val totalFilas: Int? = null,
    val resultados: List<CimaMedicamentoDto>? = null
)

data class CimaMedicamentoDto(
    val nregistro: String? = null,
    val nombre: String? = null,
    val dosis: String? = null,
    val labtitular: String? = null
)
