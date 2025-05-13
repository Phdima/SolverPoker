package com.example.solverpoker.domain.pokerLogic

data class PokerHand(val card1: String, val card2: String) {
    fun toStringNotation(): String {
        val ranks = "${card1[0]}${card2[0]}"
        val suited = card1[1] == card2[1]
        return when {
            card1[0] == card2[0] -> ranks  // Pair (AA)
            suited -> "${ranks}s"          // suited (AKs)
            else -> "${ranks}o"            // offSuited (AKo)
        }
    }
}