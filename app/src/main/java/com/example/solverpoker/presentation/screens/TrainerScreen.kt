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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.displaySymbol
import com.example.solverpoker.presentation.components.PlayerProfile
import com.example.solverpoker.presentation.viewmodel.TrainerViewModel
import kotlinx.coroutines.delay

@Composable
fun TrainerScreen(viewModel: TrainerViewModel = hiltViewModel()) {


    val gameState by viewModel.gameState.collectAsState()
    val isDealing by viewModel.isDealing.collectAsState()

    val lastAggressiveActionBeforeHero = remember(gameState) {
        gameState.players
            .filter {
                !it.isHero
            }
            .mapNotNull { player ->
                when (player.action) {
                    Action.RAISE -> Action.RAISE to player
                    Action.THREE_BET -> Action.THREE_BET to player
                    Action.FOUR_BET -> Action.FOUR_BET to player
                    Action.PUSH -> Action.PUSH to player
                    else -> null
                }
            }
            .lastOrNull()?.first
    }

    val heroAggressiveAction = when (lastAggressiveActionBeforeHero) {
        Action.RAISE -> Action.THREE_BET
        Action.THREE_BET -> Action.FOUR_BET
        Action.FOUR_BET -> Action.PUSH
        Action.PUSH -> Action.PUSH
        else -> Action.RAISE
    }
    val actionText = when (heroAggressiveAction) {
        Action.RAISE -> "Raise"
        Action.THREE_BET -> "3-Bet"
        Action.FOUR_BET -> "4-Bet"
        Action.PUSH -> "Push"
        else -> "Raise"
    }
    val hasAggressiveAction = remember(gameState) {
        val aggressiveAction =
            gameState.players.any { it.action == Action.RAISE || it.action == Action.THREE_BET || it.action == Action.FOUR_BET || it.action == Action.PUSH }

        aggressiveAction
    }
    val hasCallOrFold = remember(gameState) {
        val callExists = gameState.players.any { it.action == Action.CALL }
        val foldCount = gameState.players.count { it.action == Action.FOLD }
        val heroChoice = gameState.players.filter { it.isHero }.any { it.action == Action.FOLD }

        callExists || foldCount == 5 || viewModel.answerResult.value == false || heroChoice
    }

    val colorScheme = MaterialTheme.colorScheme

    val colorForProfileBorder = remember(gameState) {
        gameState.players.map { player ->
            when (player.action) {
                Action.FOLD -> Color.Red
                Action.WAIT -> colorScheme.inversePrimary
                else -> colorScheme.secondary
            }
        }
    }

    val feedbackMessage by viewModel.feedbackMessage.collectAsState()



    LaunchedEffect(Unit) {
        viewModel.dealAnimation()
    }


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        val tableWidth = maxWidth * 0.9f
        val tableHeight = maxHeight * 0.8f


        val playerSize = maxWidth * 0.4f

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(tableWidth, tableHeight)
                .padding(top = 5.dp)

        )
        {
            Box(
                modifier = Modifier
                    .size(tableWidth, tableHeight)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.secondaryContainer,
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
                        player = gameState.players[2],
                        color = colorForProfileBorder[2],
                        isDealing = isDealing,


                    )
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[1],
                        color = colorForProfileBorder[1]
                        ,
                        isDealing = isDealing,

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
                        player = gameState.players[4],
                        color = colorForProfileBorder[4],
                        isDealing = isDealing,

                    )
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[5],
                        color = colorForProfileBorder[5],
                        isDealing = isDealing,

                    )
                }
                PlayerProfile(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(playerSize),
                    player = gameState.players[3],
                    color = colorForProfileBorder[3],
                    isDealing = isDealing,

                )
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                        .align(Alignment.BottomCenter),
                    player = gameState.players[0],
                    color = colorForProfileBorder[0],
                    isDealing = isDealing,

                )
            }


        }
        feedbackMessage?.let {
            Text(
                text = it,
                color = when (viewModel.answerResult.value) {
                    true -> MaterialTheme.colorScheme.secondary
                    false -> Color.Red
                    else -> Color.Gray
                },
                modifier = Modifier
                    .align(Alignment.TopCenter) // Выравниваем по верхнему центру
                    .fillMaxWidth(0.9f) // Занимаем 90% ширины
                    .padding(top = tableHeight * 1.01f) // Отступ ниже стола
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(size = 40.dp)
                    ),
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {

            // DebugPositionView()

            Row {
                TextButton(
                    onClick = {
                        viewModel.selectAction(heroAggressiveAction)
                        viewModel.checkAnswer()
                    },
                    enabled = !hasCallOrFold,
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        actionText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.selectAction(Action.CALL)
                        viewModel.checkAnswer()
                    },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !hasCallOrFold && hasAggressiveAction,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Call", maxLines = 1)
                }

                TextButton(
                    onClick = {
                        viewModel.selectAction(Action.FOLD)
                        viewModel.checkAnswer()
                    },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !hasCallOrFold,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Fold", maxLines = 1)
                }

                TextButton(
                    onClick = {
                        viewModel.startNewHand()
                    },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Next", maxLines = 1)
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