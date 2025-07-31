package com.example.solverpoker.domain.pokerLogic

data class Player(
    var id: Int,
    var name: String,
    var chips: Int,
    var cards: List<PlayingCard> = emptyList(),
    var action: Action = Action.WAIT,
    var position: Position,
    var isHero: Boolean = false,
    var isDealer: Boolean = false,
    var isSmallBlind: Boolean = false,
    var isBigBlind: Boolean = false
)