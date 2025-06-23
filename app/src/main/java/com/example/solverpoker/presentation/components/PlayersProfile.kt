package com.example.solverpoker.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.solverpoker.domain.pokerLogic.CardRank
import com.example.solverpoker.domain.pokerLogic.CardSuit
import com.example.solverpoker.domain.pokerLogic.PlayingCard

@Composable
fun PlayerProfile(modifier: Modifier) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(border = BorderStroke(5.dp, Color.White), shape = RoundedCornerShape(55.dp))
                .fillMaxSize()

        )
        Row(modifier = Modifier.align(Alignment.BottomCenter)){
            PokerCard(
                card = PlayingCard(CardSuit.SPADES, CardRank.KING),
                modifier = Modifier.height(100.dp).width(60.dp).padding(2.dp)
            )
            PokerCard(
                card = PlayingCard(CardSuit.HEARTS, CardRank.ACE),
                modifier = Modifier.height(100.dp).width(60.dp).padding(2.dp)
            )
        }
    }
}