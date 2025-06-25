package com.example.solverpoker.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solverpoker.domain.pokerLogic.CardRank
import com.example.solverpoker.domain.pokerLogic.CardSuit
import com.example.solverpoker.domain.pokerLogic.Deck
import com.example.solverpoker.domain.pokerLogic.Player
import com.example.solverpoker.domain.pokerLogic.PlayingCard

@Composable
fun PlayerProfile(
    player: Player,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        // Аватар игрока
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.DarkGray, CircleShape)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.name,
                color = Color.White,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center
            )

            // Фишки игрока
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${player.chips}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
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
}



