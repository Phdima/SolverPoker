package com.example.solverpoker.domain.pokerLogic

data class Player(
    val id: Int,
    val name: String,
    val chips: Int,
    val cards: List<PlayingCard> = emptyList(),
    val action: Action = Action.FOLD,
    val position: Position,
    val isHero: Boolean = false,
    val isDealer: Boolean = false,
    val isSmallBlind: Boolean = false,
    val isBigBlind: Boolean = false
)