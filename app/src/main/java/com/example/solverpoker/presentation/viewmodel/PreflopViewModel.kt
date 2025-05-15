package com.example.solverpoker.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.DefenseAction
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.repository.ChartRepository
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
        _state.value = _state.value.copy(heroPosition  = position)
    }

    fun getHandsForMatrix(state: ChartScreenState): List<String> {
        val chart = state.charts[state.heroPosition] ?: return emptyList()

        return when(state.selectedAction) {
            DefenseAction.RAISE -> {
                chart.openRaise
                    ?: emptyList()
            }
            DefenseAction.VS_3BET -> {
                chart.vsThreeBet
                    ?.flatMap { it.value[Action.THREE_BET] ?: emptyList() }
                    ?: emptyList()
            }
            DefenseAction.VS_4BET -> {
                chart.vsFourBet
                    ?.flatMap { it.value[Action.CALL] ?: emptyList() }
                    ?: emptyList()
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