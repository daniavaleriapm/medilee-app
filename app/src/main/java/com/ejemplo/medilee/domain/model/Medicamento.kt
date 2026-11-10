package com.ejemplo.medilee.domain.model

data class Medicamento(
    val nregistro: String,
    val nombre: String,
    val dosis: String?,
    val nombreCompleto: String,
    val laboratorio: String?
)
