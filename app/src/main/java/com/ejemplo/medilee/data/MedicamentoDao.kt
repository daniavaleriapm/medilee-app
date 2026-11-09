package com.ejemplo.medilee.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos_guardados ORDER BY id DESC")
    fun obtenerHistorial(): Flow<List<MedicamentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMedicamento(medicamento: MedicamentoEntity)
}