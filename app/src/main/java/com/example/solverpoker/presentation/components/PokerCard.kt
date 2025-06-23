package com.example.solverpoker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solverpoker.R
import com.example.solverpoker.domain.pokerLogic.CardSuit
import com.example.solverpoker.domain.pokerLogic.PlayingCard

@Composable
fun PokerCard(
    card: PlayingCard,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    elevation: Dp = 4.dp
) {


    Card(
        modifier = modifier
            .shadow(elevation, shape = RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        if (card.isFaceUp) {
            FaceUpCardContent(card)
        } else {
            CardBack()
        }
    }
}


@Composable
private fun FaceUpCardContent(card: PlayingCard) {
    Box(modifier = Modifier.fillMaxSize().background(card.suit.color)) {

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        ) {
            CardCornerText(card.rank.symbol, card.suit)
        }


        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
        ) {
            Text(
                text = card.rank.symbol,
                color = Color.White,
                fontSize = 48.sp,
                fontFamily = FontFamily.SansSerif,
            )
        }
    }
}

@Composable
private fun CardCornerText(rank: String, suit: CardSuit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = rank,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = suit.symbol,
            color = Color.White,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun CardBack() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                ),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Узор на рубашке
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val patternSize = minOf(size.width, size.height) * 0.8f

            // Рисуем узор
            drawCircle(
                color = Color(0x44FFFFFF),
                radius = patternSize * 0.1f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )

            drawCircle(
                color = Color(0x44FFFFFF),
                radius = patternSize * 0.3f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )

        }
    }
}
