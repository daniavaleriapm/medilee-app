package com.ejemplo.medilee.core.common

enum class TipoError { SIN_TEXTO, SIN_CONEXION, NO_ENCONTRADO, DESCONOCIDO }

/** Resultado de una operación: éxito con datos o error tipificado. */
sealed class Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>()
    data class Error(val tipo: TipoError) : Resultado<Nothing>()
}
