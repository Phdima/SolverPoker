package com.example.solverpoker.presentation.stateholder

import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.DefenseAction
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChart

data class ChartScreenState(
    val heroPosition: Position = Position.UTG,
    val selectedAction: DefenseAction = DefenseAction.RAISE,
    val opponentPosition: Position? = null,
    val selectedResponseAction: Action? = null,
    val charts: Map<Position, RangeChart> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

