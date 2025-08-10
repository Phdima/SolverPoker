package com.example.solverpoker.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.presentation.components.ActionSelector
import com.example.solverpoker.presentation.components.ChartControls
import com.example.solverpoker.presentation.components.PokerHandMatrix
import com.example.solverpoker.presentation.stateholder.ChartScreenState
import com.example.solverpoker.presentation.viewmodel.ChartsViewModel
import com.example.solverpoker.presentation.viewmodel.HintViewModel


@Composable
fun PreflopChartScreen(
    chartViewModel: ChartsViewModel = hiltViewModel(),
    hintViewModel: HintViewModel = hiltViewModel(),
) {
    val state = chartViewModel.state.value
    val hands = chartViewModel.getHandsForMatrix(state)

    Column(
        Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)

    ) {

        PokerHandMatrix(
            handActions = hands,
            modifier = Modifier
        )

        ChartControls()
    }

}

