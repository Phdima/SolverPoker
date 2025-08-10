package com.example.solverpoker.domain.pokerLogic

import com.example.solverpoker.data.parser.RangeParser


class RangeChecker(private val rangeParser: RangeParser) {

    fun isHandInRange(hand: String, range: List<String>): Boolean {
        val parsedRange = rangeParser.parse(range.joinToString(", "))
        return parsedRange.contains(hand)
    }

    fun isHandInRange(hand: PokerHand, range: List<String>): Boolean {
        return isHandInRange(hand.toStringNotation(), range)
    }

}