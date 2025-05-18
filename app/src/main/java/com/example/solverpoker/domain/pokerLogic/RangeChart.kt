package com.example.solverpoker.domain.pokerLogic

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
data class RangeChart(
    val position: Position,
    val openRaise: List<String>? = emptyList(),
    val vsOpenRaise: Map<Position, Map<Action, List<String>>>? = emptyMap(),
    val vsThreeBet: Map<Position, Map<Action, List<String>>>? = emptyMap(),
    val vsFourBet: Map<Position, Map<Action, List<String>>>? = emptyMap(),
    val vsPush: Map<Position, Map<Action, List<String>>>? = emptyMap(),
)

class PositionAdapter {
    @ToJson
    fun toJson(position: Position): String = position.name
    @FromJson
    fun fromJson(name: String): Position = Position.valueOf(name)
}

class ActionAdapter {
    @ToJson fun toJson(action: Action): String = action.name
    @FromJson fun fromJson(name: String): Action = Action.valueOf(name)
}
