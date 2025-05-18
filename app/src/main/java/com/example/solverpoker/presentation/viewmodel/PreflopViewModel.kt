package com.example.solverpoker.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.DefenseAction
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.repository.ChartRepository
import com.example.solverpoker.presentation.components.HandAction
import com.example.solverpoker.presentation.stateholder.ChartScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val chartRepository: ChartRepository
) : ViewModel() {

    private val _state = mutableStateOf(ChartScreenState())
    val state: State<ChartScreenState> = _state

    init {
        loadCharts()
    }

    fun selectDefenseAction(defenseAction: DefenseAction) {
        _state.value = _state.value.copy(selectedAction = defenseAction)
    }

    fun selectOpponentPosition(position: Position) {
        _state.value = _state.value.copy(opponentPosition = position)
    }

    fun selectHeroPosition(position: Position) {
        _state.value = _state.value.copy(heroPosition = position)
    }


    fun getHandsForMatrix(state: ChartScreenState): List<HandAction> {
        val chart = state.charts[state.heroPosition] ?: return emptyList()

        return when (state.selectedAction) {

            DefenseAction.RAISE -> {
                chart.openRaise?.map { HandAction(it, Action.RAISE) } ?: emptyList()
            }

            DefenseAction.VS_RAISE -> {
                state.opponentPosition?.let { pos ->
                    chart.vsOpenRaise?.get(pos)?.flatMap { (action, hands) ->
                        hands.map { HandAction(it, action) }
                    } ?: emptyList()
                } ?: emptyList()
            }

            DefenseAction.VS_3BET -> {
                state.opponentPosition?.let { pos ->
                    chart.vsThreeBet?.get(pos)?.flatMap { (action, hands) ->
                        hands.map { HandAction(it, action) }
                    } ?: emptyList()
                } ?: emptyList()
            }


            DefenseAction.VS_4BET -> {
                state.opponentPosition?.let { pos ->
                    chart.vsFourBet?.get(pos)?.flatMap { (action, hands) ->
                        hands.map { HandAction(it, action) }
                    } ?: emptyList()
                } ?: emptyList()
            }
        }
    }


    private fun loadCharts() {
        viewModelScope.launch {
            try {
                val charts = Position.values().associateWith { position ->
                    chartRepository.getChart(position) ?: throw Exception("Chart not found")
                }
                _state.value = ChartScreenState(
                    charts = charts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}