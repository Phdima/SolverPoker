package com.example.solverpoker.presentation.components

import android.graphics.Paint.Align
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solverpoker.domain.pokerLogic.DefenseAction
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.presentation.viewmodel.ChartsViewModel

@Composable
fun ChartControls(viewModel: ChartsViewModel = hiltViewModel()) {
    val state = viewModel.state.value

    LazyColumn (modifier = Modifier.padding(16.dp)) {

      item {
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 5.dp),
              horizontalArrangement = Arrangement.SpaceEvenly
          ) {

              Box(
                  modifier = Modifier
                      .background(color = Color(0xFF4CAF50))
                      .size(15.dp)
                      .align(alignment = Alignment.CenterVertically)
              )
              Text(
                  "- Raise/call",
                  modifier = Modifier.align(alignment = Alignment.CenterVertically)
              )

              Box(
                  modifier = Modifier
                      .background(color = Color.Yellow)
                      .size(15.dp)
                      .align(alignment = Alignment.CenterVertically)
              )
              Text("- 3bet", modifier = Modifier.align(alignment = Alignment.CenterVertically))

              Box(
                  modifier = Modifier
                      .background(color = Color(0xFF2196F3))
                      .size(15.dp)
                      .align(alignment = Alignment.CenterVertically)
              )
              Text("- 4bet/Push", modifier = Modifier.align(alignment = Alignment.CenterVertically))


              Box(
                  modifier = Modifier
                      .background(color = Color(0xFFF44336))
                      .size(15.dp)
                      .align(alignment = Alignment.CenterVertically)
              )
              Text("- Fold", modifier = Modifier.align(alignment = Alignment.CenterVertically))
          }
      }
      item{
          PositionSelector(
              title = "Ваша позиция:",
              selectedPosition = state.heroPosition,
              onPositionSelected = viewModel::selectHeroPosition
          )
      }



      item{
          ActionSelector(
              selectedAction = state.selectedAction,
              validActions = getValidOption(state.heroPosition),
              onActionSelected = viewModel::selectDefenseAction
          )
      }

        if (state.selectedAction != DefenseAction.RAISE) {
            item{
                PositionSelector(
                    title = "Позиция оппонента:",
                    selectedPosition = state.opponentPosition,
                    positions = getValidOpponentPositions(state.heroPosition, state.selectedAction),
                    onPositionSelected = viewModel::selectOpponentPosition
                )
            }
        }
    }
}


private fun getValidOpponentPositions(
    heroPosition: Position,
    action: DefenseAction
): List<Position> {
    return when (action) {
        DefenseAction.RAISE -> {
           emptyList()
        }

        DefenseAction.VS_RAISE -> {
            when (heroPosition) {
                Position.UTG -> emptyList()
                Position.MP -> listOf(Position.UTG)
                Position.CO -> listOf(Position.UTG, Position.MP)
                Position.BTN -> listOf(Position.UTG, Position.MP, Position.CO)
                Position.SB -> listOf(Position.UTG, Position.MP, Position.CO, Position.BTN)
                Position.BB -> listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.SB)
            }
        }

        DefenseAction.VS_3BET -> {
            when (heroPosition) {
                Position.UTG -> listOf( Position.MP, Position.CO, Position.BTN, Position.SB, Position.BB)
                Position.MP -> listOf( Position.CO, Position.BTN, Position.SB, Position.BB)
                Position.CO -> listOf( Position.BTN, Position.SB, Position.BB)
                Position.BTN -> listOf( Position.SB, Position.BB)
                Position.SB -> listOf(Position.BB)
                Position.BB -> emptyList()
            }
        }

       DefenseAction.VS_4BET -> {
           when (heroPosition) {
               Position.UTG -> emptyList()
               Position.MP -> listOf(Position.UTG)
               Position.CO -> listOf(Position.UTG, Position.MP)
               Position.BTN -> listOf(Position.UTG, Position.MP, Position.CO)
               Position.SB -> listOf(Position.UTG, Position.MP, Position.CO, Position.BTN)
               Position.BB -> listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.SB)
           }
        }

    }

}


private fun getValidOption(heroPosition: Position): List<DefenseAction> {
    return when (heroPosition) {
        Position.UTG -> listOf(DefenseAction.RAISE, DefenseAction.VS_3BET)
        Position.MP -> listOf(
            DefenseAction.RAISE,
            DefenseAction.VS_RAISE,
            DefenseAction.VS_3BET,
            DefenseAction.VS_4BET
        )

        Position.CO -> listOf(
            DefenseAction.RAISE,
            DefenseAction.VS_RAISE,
            DefenseAction.VS_3BET,
            DefenseAction.VS_4BET
        )

        Position.BTN -> listOf(
            DefenseAction.RAISE,
            DefenseAction.VS_RAISE,
            DefenseAction.VS_3BET,
            DefenseAction.VS_4BET
        )

        Position.SB -> listOf(
            DefenseAction.RAISE,
            DefenseAction.VS_RAISE,
            DefenseAction.VS_3BET,
            DefenseAction.VS_4BET
        )

        Position.BB -> listOf(DefenseAction.VS_RAISE, DefenseAction.VS_4BET)
    }
}