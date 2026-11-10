package com.ejemplo.medilee.presentation.analisis

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ejemplo.medilee.core.common.Resultado
import com.ejemplo.medilee.domain.repository.MedicamentoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalisisViewModel(
    private val repositorio: MedicamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalisisUiState())
    val uiState: StateFlow<AnalisisUiState> = _uiState.asStateFlow()

    private var trabajo: Job? = null
    private var ultimaRuta: String? = null

    /** Inicia el análisis de una foto nueva (si es la misma foto, no se repite). */
    fun iniciarAnalisis(rutaFoto: String) {
        if (rutaFoto == ultimaRuta) return
        ultimaRuta = rutaFoto
        ejecutar(rutaFoto)
    }

    fun reintentar() {
        ultimaRuta?.let { ejecutar(it) }
    }

    /** La pantalla ya entregó el resultado; se limpia para no repetirlo. */
    fun onResultadoEntregado() {
        _uiState.update { it.copy(medicamento = null) }
    }

    private fun ejecutar(rutaFoto: String) {
        trabajo?.cancel()
        _uiState.value = AnalisisUiState(cargando = true)
        trabajo = viewModelScope.launch {
            when (val resultado = repositorio.analizarFoto(rutaFoto)) {
                is Resultado.Exito -> {
                    Log.d("MediLee", "Detectado: ${resultado.datos}")
                    _uiState.update { it.copy(cargando = false, medicamento = resultado.datos) }
                }
                is Resultado.Error -> {
                    Log.d("MediLee", "Error de análisis: ${resultado.tipo}")
                    _uiState.update { it.copy(cargando = false, tipoError = resultado.tipo) }
                }
            }
        }
    }

    companion object {
        fun fabrica(repositorio: MedicamentoRepository): ViewModelProvider.Factory =
            viewModelFactory { initializer { AnalisisViewModel(repositorio) } }
    }
}
