package com.ejemplo.medilee.integration.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

/** Integración con ML Kit (OCR en el dispositivo): convierte una foto en líneas de texto. */
class ReconocedorTexto(private val context: Context) {

    private val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun leerLineas(rutaFoto: String): List<String> = withContext(Dispatchers.IO) {
        val imagen = InputImage.fromFilePath(context, Uri.fromFile(File(rutaFoto)))
        val resultado = reconocedor.process(imagen).await()
        resultado.textBlocks.flatMap { bloque -> bloque.lines.map { it.text } }
    }
}
