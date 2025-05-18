package com.example.solverpoker.data.repository

import android.content.Context
import android.util.Log
import com.example.solverpoker.data.RangeParser
import com.example.solverpoker.domain.pokerLogic.Action
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
    private val rangeParser: RangeParser,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ChartRepository {

    private val chartsCache = mutableMapOf<Position, RangeChart>()

    init {
        runBlocking(dispatcher) {
            loadCharts()
        }
    }

    override suspend fun getChart(position: Position): RangeChart? {
        return chartsCache[position]?.let { parseChart(it) }
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

    private fun parseChart(chart: RangeChart): RangeChart {
        return chart.copy(
            openRaise = chart.openRaise.parseRanges(),
            vsOpenRaise = chart.vsOpenRaise?.parseNestedRanges(),
            vsThreeBet = chart.vsThreeBet?.parseNestedRanges(),
            vsFourBet = chart.vsFourBet?.parseNestedRanges(),
            vsPush = chart.vsPush?.parseNestedRanges()
        )
    }

    private fun List<String>?.parseRanges(): List<String>? {
        return this?.flatMap { rangeParser.parse(it) }
    }

    private fun Map<Position, Map<Action, List<String>>>?.parseNestedRanges(): Map<Position, Map<Action, List<String>>>? {
        return this?.mapValues { (_, actions) ->
            actions.mapValues { (_, ranges) ->
                ranges.flatMap { rangeParser.parse(it) }
            }
        }
    }
}