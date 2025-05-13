package com.example.solverpoker.domain.pokerLogic

import com.example.solverpoker.data.RangeParser


class RangeChecker(private val rangeParser: RangeParser) {

    fun isHandInRange(hand: PokerHand, range: List<String>): Boolean {
        val handNotation = hand.toStringNotation()
        val parsedRange = rangeParser.parse(range.joinToString(", "))
        return parsedRange.contains(handNotation)
    }
}