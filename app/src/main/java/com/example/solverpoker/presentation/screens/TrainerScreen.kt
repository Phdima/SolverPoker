package com.example.solverpoker.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.displaySymbol
import com.example.solverpoker.presentation.components.PlayerProfile
import com.example.solverpoker.presentation.viewmodel.TrainerViewModel

@Composable
fun TrainerScreen(viewModel: TrainerViewModel = hiltViewModel()) {
    val gameState by viewModel.gameState.collectAsState()
    val positionsOrder =
        listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.SB, Position.BB)
    val heroPosition = gameState.players.find { it.isHero }?.position ?: Position.BTN
    val hasRaiserBeforeHero = remember(gameState) {
        gameState.players.any { player ->
            !player.isHero &&
                    positionsOrder.indexOf(player.position) < positionsOrder.indexOf(heroPosition) &&
                    (player.action == Action.RAISE ||
                            player.action == Action.THREE_BET ||
                            player.action == Action.FOUR_BET ||
                            player.action == Action.PUSH)
        }
    }
    val raiseAction = if (hasRaiserBeforeHero) Action.THREE_BET else Action.RAISE
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray)
    ) {
        val tableWidth = maxWidth * 0.9f
        val tableHeight = maxHeight * 0.8f


        val playerSize = maxWidth * 0.4f

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(tableWidth, tableHeight)
                .padding(top = 30.dp)

        )
        {
            Box(
                modifier = Modifier
                    .size(tableWidth, tableHeight)
                    .background(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 4.dp,
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(16.dp)
                    )

            )
            {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(tableHeight),
                    Arrangement.SpaceEvenly
                ) {
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[5]
                    )
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[4]
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(tableHeight),
                    Arrangement.SpaceEvenly
                ) {
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[1]
                    )
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[2]
                    )
                }
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                        .align(Alignment.TopCenter),
                    player = gameState.players[0]
                )
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                        .align(Alignment.BottomCenter),
                    player = gameState.players[3]
                )
            }

        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {

            // DebugPositionView()
            feedbackMessage?.let {
                Text(
                    text = it,
                    color = when (viewModel.answerResult.value) {
                        true -> Color.Green
                        false -> Color.Red
                        else -> Color.Gray
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }

            Row(

            ) {
                Button(
                    onClick = {
                        viewModel.selectAction(raiseAction)
                        viewModel.checkAnswer()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.action.value == raiseAction) Color.LightGray else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (hasRaiserBeforeHero) "3-Bet" else "Raise")
                }
                Button(
                    onClick = {
                        viewModel.selectAction(Action.CALL)
                        viewModel.checkAnswer()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.action.value == Action.CALL) Color.LightGray else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Call")
                }

                Button(
                    onClick = {
                        viewModel.selectAction(Action.FOLD)
                        viewModel.checkAnswer()

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.action.value == Action.FOLD) Color.LightGray else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Fold")
                }
                Button(
                    onClick = {
                        viewModel.startNewHand()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.action.value == Action.FOLD) Color.LightGray else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("nextHand")
                }
            }

        }
    }
}

@Composable
fun DebugPositionView(viewModel: TrainerViewModel = hiltViewModel()) {
    val gameState by viewModel.gameState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp)
    ) {
        Text("Отладка позиций и карт:", fontWeight = FontWeight.Bold)
        gameState.players.forEach { player ->
            val cardsInfo = player.cards.joinToString(", ") {
                "${it.rank.displaySymbol()}${it.suit.symbol}"
            }

            val positionInfo = buildString {
                append(player.name)
                append(": ${player.position}")
                if (player.isDealer) append(" [D]")
                if (player.isSmallBlind) append(" [SB]")
                if (player.isBigBlind) append(" [BB]")
                if (player.isHero) append(" [HERO]")
                append(" | $cardsInfo")
            }

            Text(
                text = positionInfo,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}