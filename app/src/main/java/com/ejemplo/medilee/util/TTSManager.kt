package com.ejemplo.medilee.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TTSManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var pendingRate: Float = 1.0f
    private var pendingLocale: Locale = Locale("es", "ES")

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(pendingLocale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setSpeechRate(pendingRate)
            }
        }
    }

    // Método para reproducir texto
    fun speak(text: String) {
        if (isInitialized && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
        }
    }

    // Método para detener el audio
    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
    }

    // Cambiar velocidad de lectura (0.5f a 2.0f)
    fun setSpeechRate(rate: Float) {
        pendingRate = rate
        if (isInitialized) {
            tts?.setSpeechRate(rate)
        }
    }

    // Cambiar idioma (Español / Inglés)
    fun setLanguage(locale: Locale) {
        pendingLocale = locale
        if (isInitialized) {
            tts?.setLanguage(locale)
        }
    }

    // Liberar recursos al cerrar
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}