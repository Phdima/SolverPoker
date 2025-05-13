package com.example.solverpoker

import com.example.solverpoker.data.RangeParser
import com.example.solverpoker.domain.pokerLogic.PokerHand
import com.example.solverpoker.domain.pokerLogic.RangeChecker
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test


class RangeCheckerTest {
    private val rangeParser = RangeParser()
    private val rangeChecker = RangeChecker(rangeParser)

    @Test
    fun `AA should be in AA range`() {
        val hand = PokerHand("Ah", "Ac")
        assertTrue(rangeChecker.isHandInRange(hand, listOf("AA")))
    }

    @Test
    fun `AKs should be in AKs+ range`() {
        val hand = PokerHand("Ah", "Kh")
        assertTrue(rangeChecker.isHandInRange(hand, listOf("AKs+")))
    }

    @Test
    fun `AQo should not be in AKs+ range`() {
        val hand = PokerHand("Ah", "Qd")
        assertFalse(rangeChecker.isHandInRange(hand, listOf("AKs+")))
    }

    @Test
    fun `TT should be in 22+ range`() {
        val hand = PokerHand("Th", "Tc")
        assertTrue(rangeChecker.isHandInRange(hand, listOf("22+")))
    }

    @Test
    fun `TT should be in 22-KK range`(){
        val hand = PokerHand("Th", "Td")
        assertTrue(rangeChecker.isHandInRange(hand, listOf("22-KK")))
    }

    @Test
    fun `77 should be in 55-99 range`() {
        val hand = PokerHand("7h", "7c")
        assertTrue(rangeChecker.isHandInRange(hand, listOf("55-99")))
    }

    @Test
    fun `KQs should be in QJs+ range`() {
        val hand = PokerHand("Kh", "Qh")
        assertTrue(rangeChecker.isHandInRange(hand, listOf("QJs+")))
    }

    @Test
    fun `A5s should be in A2s+ range with bluffs`() {
        val hand = PokerHand("Ah", "5h")
        assertTrue(rangeChecker.isHandInRange(hand, listOf("A2s+")))
    }

    @Test
    fun `J9o should not be in QJo+ range`() {
        val hand = PokerHand("Jh", "9d")
        assertFalse(rangeChecker.isHandInRange(hand, listOf("QJo+")))
    }
}