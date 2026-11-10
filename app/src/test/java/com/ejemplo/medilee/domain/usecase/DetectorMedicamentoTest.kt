package com.ejemplo.medilee.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectorMedicamentoTest {

    private val detector = DetectorMedicamento()

    @Test
    fun detecta_nombre_y_dosis() {
        val r = detector.detectar(listOf("PARACETAMOL", "500 mg", "Tabletas"))
        assertEquals("500 mg", r.dosis)
        assertEquals("paracetamol", r.candidatos.first())
    }

    @Test
    fun dosis_con_coma_se_normaliza() {
        val r = detector.detectar(listOf("Levotiroxina 0,5mg"))
        assertEquals("0.5 mg", r.dosis)
    }

    @Test
    fun sin_texto_no_hay_candidatos_ni_dosis() {
        val r = detector.detectar(emptyList())
        assertTrue(r.candidatos.isEmpty())
        assertNull(r.dosis)
    }
}
