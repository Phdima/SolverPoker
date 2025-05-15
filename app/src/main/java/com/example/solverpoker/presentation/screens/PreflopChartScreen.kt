package com.example.solverpoker.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChart
import com.example.solverpoker.presentation.viewmodel.ChartsViewModel

@Composable
fun PreflopChartScreen(viewModel: ChartsViewModel = hiltViewModel()) {

    val state = viewModel.state.value


    if (state.isLoading) {
        LoadingIndicator()
        return
    }

    if (state.error != null) {
        ErrorMessage(state.error)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        state.charts[state.selectedPosition]?.let { chart ->
            ChartDetails(chart)
        } ?: run {
            Text("No chart available for selected position", color = Color.Red)
        }

        PositionSelector(
            onPositionSelected = viewModel::selectPosition
        )
    }

}

@Composable
private fun PositionSelector(
    onPositionSelected: (Position) -> Unit
) {
    Column {
        Text(
            text = "Выберите позицию:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Position.values()) { position ->
                Button(
                    onClick = { onPositionSelected(position) },

                    ) {
                    Text(text = position.displayName)
                }
            }
        }
    }
}

@Composable
private fun ChartDetails(chart: RangeChart) {
    Column {

        chart.openRaise?.takeIf { it.isNotEmpty() }?.let {
            ChartSection(title = "Открытие", hands = it)
        }

        chart.vsOpenRaise?.forEach { (position, actionsMap) ->
            actionsMap.takeIf { it.isNotEmpty() }?.let { actions ->

                val allHands = actions.values.flatten()

                allHands.takeIf { it.isNotEmpty() }?.let {
                    ChartSection(
                        title = "vs Open Raise: ${position.displayName}",
                        hands = allHands
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartSection(title: String, hands: List<String>) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(5.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

            }


            PokerHandMatrix(
                hands = hands,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

        }
    }
}


@Composable
private fun PokerHandMatrix(hands: List<String>, modifier: Modifier = Modifier) {
    val cardRanks =
        remember { listOf("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2") }

    LazyVerticalGrid(
        columns = GridCells.Fixed(13),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(cardRanks.size * cardRanks.size) { index ->
            val row = index / 13
            val col = index % 13

            val (rank1, rank2) = cardRanks[row] to cardRanks[col]
            val handType = when {
                row == col -> "pair"
                row < col -> "suited"
                else -> "offsuit"
            }

            val handName = buildHandName(rank1, rank2, handType)
            val isSelected = hands.contains(handName)

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = handName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}


private fun buildHandName(rank1: String, rank2: String, type: String): String {
    val (first, second) = listOf(rank1, rank2).sortedByDescending { cardOrder(it) }
    return when (type) {
        "pair" -> "$first$second"
        "suited" -> "$first${second}s"
        "offsuit" -> "$first${second}o"
        else -> ""
    }
}

private fun cardOrder(rank: String): Int = when (rank) {
    "A" -> 14
    "K" -> 13
    "Q" -> 12
    "J" -> 11
    "T" -> 10
    else -> rank.toInt()
}


@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message ?: "Ошибка загрузки данных",
            color = MaterialTheme.colorScheme.error
        )
    }
}

val Position.displayName: String
    get() = when (this) {
        Position.UTG -> "UTG"
        Position.MP -> "MP"
        Position.CO -> "CO"
        Position.BTN -> "BTN"
        Position.SB -> "SB"
        Position.BB -> "BB"
    }