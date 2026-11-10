package com.ejemplo.medilee.data.remote.api

import com.ejemplo.medilee.data.remote.dto.CimaRespuestaDto
import retrofit2.http.GET
import retrofit2.http.Query

/** API pública de la AEMPS (CIMA). Base: https://cima.aemps.es/cima/rest/ */
interface CimaApi {
    @GET("medicamentos")
    suspend fun buscarMedicamentos(
        @Query("nombre") nombre: String,
        @Query("comerc") comercializado: Int = 1
    ): CimaRespuestaDto
}
