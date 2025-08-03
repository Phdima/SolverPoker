package com.example.solverpoker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.solverpoker.domain.pokerLogic.Player

@Composable
fun PlayerProfile(
    player: Player,
    color: Color,
    modifier: Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        // Используем дополнительный Box для фона и рамки
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ){
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
                        )
                        PokerCard(
                            player.cards[1],
                            player.isHero,
                            modifier = Modifier
                                .height(80.dp)
                                .width(50.dp)
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
            modifier = Modifier
        )
    }

}



