package com.ejemplo.medilee.domain.usecase

import java.text.Normalizer

/**
 * Regla de negocio: devuelve una descripción breve de uso para un medicamento.
 * CIMA no entrega una descripción simple, así que se usa un diccionario local
 * de medicamentos comunes (con texto de respaldo si no está en la lista).
 */
object DescripcionMedicamento {

    // clave (sin acentos) -> (español, inglés)
    private val descripciones: Map<String, Pair<String, String>> = mapOf(
        "paracetamol" to Pair(
            "Se usa para aliviar el dolor leve o moderado y bajar la fiebre.",
            "Used to relieve mild to moderate pain and reduce fever."
        ),
        "ibuprofeno" to Pair(
            "Se usa para aliviar el dolor, la inflamación y la fiebre.",
            "Used to relieve pain, inflammation and fever."
        ),
        "naproxeno" to Pair(
            "Se usa para aliviar el dolor y la inflamación.",
            "Used to relieve pain and inflammation."
        ),
        "diclofenaco" to Pair(
            "Se usa para aliviar el dolor y la inflamación.",
            "Used to relieve pain and inflammation."
        ),
        "acetilsalicilico" to Pair(
            "Se usa para aliviar el dolor, la fiebre y la inflamación.",
            "Used to relieve pain, fever and inflammation."
        ),
        "aspirina" to Pair(
            "Se usa para aliviar el dolor, la fiebre y la inflamación.",
            "Used to relieve pain, fever and inflammation."
        ),
        "losartan" to Pair(
            "Se usa para tratar la presión arterial alta.",
            "Used to treat high blood pressure."
        ),
        "enalapril" to Pair(
            "Se usa para tratar la presión arterial alta.",
            "Used to treat high blood pressure."
        ),
        "metformina" to Pair(
            "Se usa para controlar el azúcar en la sangre en la diabetes tipo 2.",
            "Used to control blood sugar in type 2 diabetes."
        ),
        "omeprazol" to Pair(
            "Se usa para reducir el ácido del estómago, en casos de acidez o gastritis.",
            "Used to reduce stomach acid, for heartburn or gastritis."
        ),
        "amoxicilina" to Pair(
            "Es un antibiótico que se usa para tratar infecciones causadas por bacterias.",
            "An antibiotic used to treat infections caused by bacteria."
        ),
        "loratadina" to Pair(
            "Se usa para aliviar los síntomas de la alergia.",
            "Used to relieve allergy symptoms."
        ),
        "atorvastatina" to Pair(
            "Se usa para ayudar a bajar el colesterol.",
            "Used to help lower cholesterol."
        )
    )

    private const val SUFIJO_ES = " Tómalo según lo indicado por el médico."
    private const val SUFIJO_EN = " Take it as directed by your doctor."

    fun para(nombre: String, espanol: Boolean): String {
        val clave = normalizar(nombre)
        val encontrada = descripciones.entries.firstOrNull { clave.contains(it.key) }?.value

        return if (encontrada != null) {
            if (espanol) encontrada.first + SUFIJO_ES else encontrada.second + SUFIJO_EN
        } else {
            if (espanol) {
                "Consulta a tu médico o farmacéutico para conocer el uso de este medicamento."
            } else {
                "Ask your doctor or pharmacist about the use of this medicine."
            }
        }
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}