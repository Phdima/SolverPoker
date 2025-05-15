package com.example.solverpoker.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChart
import com.example.solverpoker.domain.repository.ChartRepository
import com.example.solverpoker.presentation.ChartScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    fun selectPosition(position: Position) {
        _state.value = _state.value.copy(selectedPosition = position)
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