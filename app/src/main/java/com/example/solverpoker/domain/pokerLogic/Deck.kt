package com.example.solverpoker.domain.pokerLogic

import javax.inject.Inject

class Deck {
    private val cards: MutableList<PlayingCard> = mutableListOf()

    init {
        reset()
        shuffle()
    }

    private fun reset() {
        CardSuit.entries.forEach { suit ->
            CardRank.entries.forEach { rank ->
                cards.add(PlayingCard(suit, rank))
            }
        }
    }

   private fun shuffle() {
        cards.shuffle()
    }

    fun drawCard(): PlayingCard {
        return cards.removeAt(0)
    }
}