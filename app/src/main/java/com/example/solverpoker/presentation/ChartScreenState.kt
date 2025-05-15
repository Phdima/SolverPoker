package com.example.solverpoker.presentation

import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChart

data class ChartScreenState(
    val selectedPosition: Position = Position.UTG,
    val charts: Map<Position, RangeChart> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)