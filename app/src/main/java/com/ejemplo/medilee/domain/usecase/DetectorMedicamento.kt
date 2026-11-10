package com.ejemplo.medilee.domain.usecase

import com.ejemplo.medilee.domain.model.DeteccionMedicamento

/**
 * Regla de negocio: a partir de las líneas de texto del empaque,
 * obtiene la dosis (ej. "500 mg") y posibles nombres del medicamento.
 * Es Kotlin puro, así que se puede probar sin emulador.
 */
class DetectorMedicamento {

    private val regexDosis = Regex(
        """(\d+(?:[.,]\d+)?)\s*(mg|mcg|µg|g|ml|ui)\b""",
        RegexOption.IGNORE_CASE
    )
    private val regexPalabra = Regex("""\p{L}{4,}""")

    private val palabrasIgnoradas = setOf(
        "tabletas", "tableta", "comprimidos", "comprimido", "capsulas", "capsula",
        "cápsulas", "cápsula", "caja", "contiene", "lote", "caducidad", "fabricado",
        "oral", "solucion", "solución", "suspension", "suspensión", "jarabe", "gotas",
        "adultos", "niños", "ninos", "mexico", "méxico", "sanitario", "para", "cada",
        "dosis", "marca", "venta", "receta", "medica", "médica", "requiere", "tabs",
        "ampolletas", "pastillas", "farmaceutica", "farmacéutica", "laboratorios"
    )

    fun detectar(lineas: List<String>): DeteccionMedicamento {
        val textoCompleto = lineas.joinToString(" ")

        val dosis = regexDosis.find(textoCompleto)?.let {
            "${it.groupValues[1].replace(',', '.')} ${it.groupValues[2].lowercase()}"
        }

        val candidatos = lineas
            .flatMap { linea -> regexPalabra.findAll(linea).map { it.value }.toList() }
            .map { it.lowercase() }
            .filter { it !in palabrasIgnoradas }
            .distinct()
            .take(5)

        return DeteccionMedicamento(candidatos = candidatos, dosis = dosis)
    }
}
