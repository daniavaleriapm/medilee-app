package com.ejemplo.medilee.data.repository

import com.ejemplo.medilee.core.common.Resultado
import com.ejemplo.medilee.core.common.TipoError
import com.ejemplo.medilee.data.remote.api.CimaApi
import com.ejemplo.medilee.domain.model.DeteccionMedicamento
import com.ejemplo.medilee.domain.model.Medicamento
import com.ejemplo.medilee.domain.repository.MedicamentoRepository
import com.ejemplo.medilee.domain.usecase.DetectorMedicamento
import com.ejemplo.medilee.integration.ocr.ReconocedorTexto
import kotlinx.coroutines.CancellationException
import java.io.IOException

class MedicamentoRepositoryImpl(
    private val api: CimaApi,
    private val reconocedor: ReconocedorTexto,
    private val detector: DetectorMedicamento
) : MedicamentoRepository {

    override suspend fun analizarFoto(rutaFoto: String): Resultado<Medicamento> {
        // 1) OCR
        val lineas = try {
            reconocedor.leerLineas(rutaFoto)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Resultado.Error(TipoError.DESCONOCIDO)
        }

        // 2) Detectar nombre y dosis
        val deteccion = detector.detectar(lineas)
        if (deteccion.candidatos.isEmpty()) return Resultado.Error(TipoError.SIN_TEXTO)

        // 3) Verificar con la API
        return try {
            buscarEnCima(deteccion)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Resultado.Error(TipoError.SIN_CONEXION)
        } catch (e: Exception) {
            Resultado.Error(TipoError.DESCONOCIDO)
        }
    }

    private suspend fun buscarEnCima(deteccion: DeteccionMedicamento): Resultado<Medicamento> {
        for (candidato in deteccion.candidatos) {
            val encontrado = api.buscarMedicamentos(candidato)
                .resultados
                ?.firstOrNull { !it.nombre.isNullOrBlank() && !it.nregistro.isNullOrBlank() }

            if (encontrado != null) {
                return Resultado.Exito(
                    Medicamento(
                        nregistro = encontrado.nregistro.orEmpty(),
                        nombre = candidato.replaceFirstChar { it.uppercase() },
                        dosis = deteccion.dosis ?: encontrado.dosis,
                        nombreCompleto = encontrado.nombre.orEmpty(),
                        laboratorio = encontrado.labtitular
                    )
                )
            }
        }
        return Resultado.Error(TipoError.NO_ENCONTRADO)
    }
}
