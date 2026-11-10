package com.ejemplo.medilee.di

import android.content.Context
import com.ejemplo.medilee.data.remote.api.CimaApi
import com.ejemplo.medilee.data.repository.MedicamentoRepositoryImpl
import com.ejemplo.medilee.domain.repository.MedicamentoRepository
import com.ejemplo.medilee.domain.usecase.DetectorMedicamento
import com.ejemplo.medilee.integration.ocr.ReconocedorTexto
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Único lugar donde se construyen los servicios (inyección manual).
 * Las pantallas nunca crean Retrofit ni repositorios por su cuenta.
 */
class AppContainer(context: Context) {

    private val clienteHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://cima.aemps.es/cima/rest/")
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val cimaApi: CimaApi = retrofit.create(CimaApi::class.java)

    val medicamentoRepository: MedicamentoRepository = MedicamentoRepositoryImpl(
        api = cimaApi,
        reconocedor = ReconocedorTexto(context.applicationContext),
        detector = DetectorMedicamento()
    )
}
