package com.example.solverpoker.data.di

import android.content.Context
import com.example.solverpoker.data.RangeParser
import com.example.solverpoker.data.repository.ChartRepositoryImpl
import com.example.solverpoker.domain.repository.ChartRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideChartRepository(
        @ApplicationContext context: Context,
        moshi: Moshi
    ): ChartRepository {
        return ChartRepositoryImpl(context, moshi, rangeParser = RangeParser())
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
}