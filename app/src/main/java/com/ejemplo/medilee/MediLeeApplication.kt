package com.ejemplo.medilee

import android.app.Application
import com.ejemplo.medilee.di.AppContainer

class MediLeeApplication : Application() {
    lateinit var contenedor: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(this)
    }
}
