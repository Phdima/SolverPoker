package com.example.solverpoker

import android.app.Application
import com.example.solverpoker.data.RangeParser
import com.example.solverpoker.data.repository.ChartRepositoryImpl
import com.example.solverpoker.domain.repository.ChartRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class PokerApp : Application() {
    val chartRepository: ChartRepository by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        ChartRepositoryImpl(applicationContext, moshi, RangeParser())
    }
}