package com.ejemplo.medilee.domain.repository

import com.ejemplo.medilee.core.common.Resultado
import com.ejemplo.medilee.domain.model.Medicamento

interface MedicamentoRepository {
    /** Lee el texto de la foto, detecta nombre y dosis, y los verifica con la API. */
    suspend fun analizarFoto(rutaFoto: String): Resultado<Medicamento>
}
