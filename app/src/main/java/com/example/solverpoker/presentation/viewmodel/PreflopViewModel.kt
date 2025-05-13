package com.example.solverpoker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChart
import com.example.solverpoker.domain.repository.ChartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class PreflopViewModel @Inject constructor(
    private val chartRepository: ChartRepository
) : ViewModel() {

    fun getChart(position: Position): RangeChart? {
        return runBlocking { chartRepository.getChart(position) }
    }
}