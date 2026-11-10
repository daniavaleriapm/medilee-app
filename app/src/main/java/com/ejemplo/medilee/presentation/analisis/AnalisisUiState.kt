package com.ejemplo.medilee.presentation.analisis

import com.ejemplo.medilee.core.common.TipoError
import com.ejemplo.medilee.domain.model.Medicamento

data class AnalisisUiState(
    val cargando: Boolean = true,
    val medicamento: Medicamento? = null,
    val tipoError: TipoError? = null
)
