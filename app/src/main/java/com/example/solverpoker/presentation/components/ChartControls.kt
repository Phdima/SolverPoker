package com.example.solverpoker.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solverpoker.domain.pokerLogic.DefenseAction
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.presentation.viewmodel.ChartsViewModel

@Composable
fun ChartControls(viewModel: ChartsViewModel) {
    val state = viewModel.state.value

    Column(modifier = Modifier.padding(16.dp)) {

        PositionSelector(
            title = "Ваша позиция:",
            selectedPosition = state.heroPosition,
            onPositionSelected = viewModel::selectHeroPosition
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionSelector(
            selectedAction = state.selectedAction,
            onActionSelected = viewModel::selectDefenseAction
        )

        if (state.selectedAction != DefenseAction.RAISE) {
            PositionSelector(
                title = "Позиция оппонента:",
                selectedPosition = state.opponentPosition,
                positions = getValidOpponentPositions(state.heroPosition),
                onPositionSelected = viewModel::selectOpponentPosition
            )
        }
    }
}


private fun getValidOpponentPositions(heroPosition: Position): List<Position> {
    return when (heroPosition) {
        Position.UTG -> listOf(Position.MP, Position.CO, Position.BTN, Position.SB, Position.BB)
        Position.MP -> listOf(Position.UTG, Position.CO, Position.BTN, Position.SB, Position.BB)
        Position.CO -> listOf(Position.UTG, Position.MP, Position.BTN, Position.SB, Position.BB)
        Position.BTN -> listOf(Position.UTG, Position.MP, Position.CO, Position.SB, Position.BB)
        Position.SB -> listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.BB)
        Position.BB -> listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.SB)

    }
}