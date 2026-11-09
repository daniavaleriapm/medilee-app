package com.ejemplo.medilee.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos_guardados")
data class MedicamentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val dosis: String,
    val descripcion: String,
    val fecha: String
)