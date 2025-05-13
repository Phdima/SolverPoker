package com.example.solverpoker.domain.repository

import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChart


interface ChartRepository {
    suspend fun getChart(position: Position): RangeChart?
}