package com.ejemplo.medilee.domain.model

/** Lo que se pudo deducir del texto leído en el empaque (antes de verificarlo con la API). */
data class DeteccionMedicamento(
    val candidatos: List<String>,
    val dosis: String?
)
