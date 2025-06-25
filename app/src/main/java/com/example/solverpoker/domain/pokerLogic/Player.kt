package com.example.solverpoker.domain.pokerLogic

data class Player(
    val id: Int,
    val name: String,
    val chips: Int,
    val cards: List<PlayingCard> = emptyList(),
    val isDealer: Boolean = false,
    val isSmallBlind: Boolean = false,
    val isBigBlind: Boolean = false
)