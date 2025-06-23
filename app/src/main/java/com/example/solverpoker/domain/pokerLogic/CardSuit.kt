package com.example.solverpoker.domain.pokerLogic

import androidx.compose.ui.graphics.Color

enum class CardSuit(val symbol: String, val color: Color) {
    HEARTS("♡", Color.Red),
    DIAMONDS("♢", Color.Blue),
    CLUBS("♧", Color.Green),
    SPADES("♤", Color.Black);
}