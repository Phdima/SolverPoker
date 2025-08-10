package com.example.solverpoker.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCbrt
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.Player
import kotlinx.coroutines.delay


@Composable
fun PlayerProfile(
    player: Player,
    color: Color,
    isDealing: Boolean,
    modifier: Modifier,
) {
    var startFoldAnimation by remember { mutableStateOf(false) }
    var startActionAnimation by remember { mutableStateOf(false) }


    val offsetY = remember { Animatable(0.dp, Dp.VectorConverter) }

    LaunchedEffect(player.action) {
        if (player.action == Action.FOLD) {
            delay(player.id * 100L)
            startFoldAnimation = true
        } else {
            startFoldAnimation = false
        }

        if (player.action == Action.RAISE || player.action == Action.THREE_BET || player.action == Action.FOUR_BET || player.action == Action.PUSH) {
            delay(player.id * 100L)
            startActionAnimation = true
        } else {
            startActionAnimation = false
        }
    }


    LaunchedEffect(startActionAnimation) {
        if (startActionAnimation) {

            offsetY.animateTo(
                targetValue = 20.dp,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            )


            offsetY.animateTo(
                targetValue = 0.dp,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                )
            )
            startActionAnimation = false

        } else {
            offsetY.snapTo(0.dp)
        }
    }
    val cardsFoldAnimation by animateDpAsState(
        targetValue = if (startFoldAnimation) 1000.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    )


    val card1Offset by animateDpAsState(
        targetValue = if (isDealing) 500.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        )
    )

    val card2Offset by animateDpAsState(
        targetValue = if (isDealing) 500.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        )
    )





    Column(
        modifier = modifier.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Используем дополнительный Box для фона и рамки
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            // Фон и рамка
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                    .border(2.dp, color = color, CircleShape)
            )
            // Основное содержимое
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (player.cards.isNotEmpty()) {
                    Row() {
                        PokerCard(
                            player.cards[0],
                            player.isHero,
                            modifier = Modifier
                                .height(80.dp)
                                .width(50.dp)
                                .offset(x = -card1Offset)
                                .offset(x = cardsFoldAnimation)
                                .offset(y = -offsetY.value)
                        )
                        PokerCard(
                            player.cards[1],
                            player.isHero,
                            modifier = Modifier
                                .height(80.dp)
                                .width(50.dp)
                                .offset(x = card2Offset)
                                .offset(x = cardsFoldAnimation)
                                .offset(y = -offsetY.value)
                        )
                    }
                }

                // Дилерская кнопка
                if (player.isDealer) {
                    PokerDealerButton(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                    )
                }

                // Малый блайнд
                if (player.isSmallBlind) {
                    BlindMarker(
                        text = "SB",
                        color = Color.Blue,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                    )
                }

                // Большой блайнд
                if (player.isBigBlind) {
                    BlindMarker(
                        text = "BB",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }

        BetView(
            player = player,
        )
    }

}




