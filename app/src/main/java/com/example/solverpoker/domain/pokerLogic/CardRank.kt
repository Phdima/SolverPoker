package com.example.solverpoker.domain.pokerLogic

enum class CardRank(val symbol: String) {
    TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"),
    EIGHT("8"), NINE("9"), TEN("T"), JACK("J"), QUEEN("Q"), KING("K"), ACE("A")
}

fun CardRank.displaySymbol(): String = when(this) {
    CardRank.TWO -> "2"
    CardRank.THREE -> "3"
    CardRank.FOUR -> "4"
    CardRank.FIVE -> "5"
    CardRank.SIX -> "6"
    CardRank.SEVEN -> "7"
    CardRank.EIGHT -> "8"
    CardRank.NINE -> "9"
    CardRank.TEN -> "T"
    CardRank.JACK -> "J"
    CardRank.QUEEN -> "Q"
    CardRank.KING -> "K"
    CardRank.ACE -> "A"
}