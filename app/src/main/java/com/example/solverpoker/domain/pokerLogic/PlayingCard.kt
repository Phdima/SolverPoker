package com.example.solverpoker.domain.pokerLogic

data class PlayingCard(
    val suit: CardSuit,
    val rank: CardRank,
    val isFaceUp: Boolean = true
) {
    val displayName = "${rank.symbol}${suit.symbol}"
}