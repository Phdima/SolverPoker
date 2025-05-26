package com.example.solverpoker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solverpoker.domain.pokerLogic.Action


data class HandAction(
    val hand: String,
    val action: Action? = null
)

@Composable
fun PokerHandMatrix( handActions: List<HandAction>, modifier: Modifier = Modifier) {
    val cardRanks =
        remember { listOf("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2") }

    val actionsMap = remember(handActions) {
        handActions.groupBy { it.hand }
    }


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
            val actions = actionsMap[handName]?.map { it.action }?.distinct() ?: emptyList()

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                DualActionBackground(actions)

                Text(
                    text = handName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun DualActionBackground(actions: List<Action?>) {
    val uniqueActions = actions.distinct()

    when (uniqueActions.size) {
        1 -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getActionColor(uniqueActions[0]))
            )
        }
        2 -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .background(getActionColor(uniqueActions[0]))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterEnd)
                        .background(getActionColor(uniqueActions[1]))
                )
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun getActionColor(action: Action?): Color {
    return when (action) {
        Action.RAISE -> Color(0xFF4CAF50)
        Action.CALL -> Color(0xFF4CAF50)
        Action.THREE_BET -> Color.Yellow
        Action.FOUR_BET -> Color(0xFF2196F3)
        Action.PUSH -> Color(0xFF2196F3)
        Action.FOLD -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

