package com.example.solverpoker.data.repository

import android.content.Context
import android.util.Log
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChart
import com.example.solverpoker.domain.repository.ChartRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


class ChartRepositoryImpl(
    private val context: Context,
    private val moshi: Moshi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ChartRepository {

    private val chartsCache = mutableMapOf<Position, RangeChart>()

    init {
        runBlocking(dispatcher) {
            loadCharts()
        }
    }

    override suspend fun getChart(position: Position): RangeChart? {
        return chartsCache[position]
    }

    private suspend fun loadCharts() {
        try {
            val json = context.assets.open("preflop_charts.json")
                .bufferedReader()
                .use { it.readText() }

            val type = Types.newParameterizedType(List::class.java, RangeChart::class.java)
            val adapter = moshi.adapter<List<RangeChart>>(type)
            val charts = adapter.fromJson(json) ?: return

            chartsCache.clear()
            charts.forEach { chart ->
                chartsCache[chart.position] = chart
            }
        } catch (e: Exception) {
            Log.e("ChartRepository", "Error loading charts", e)
        }
    }
}