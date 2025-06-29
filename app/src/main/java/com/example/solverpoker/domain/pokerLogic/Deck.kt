package com.example.solverpoker.domain.pokerLogic



class Deck {
    private val cards: MutableList<PlayingCard> = mutableListOf()

    init {
        reset()
        shuffle()
    }

    fun drawCard(): List<PlayingCard> {
        return mutableListOf(cards.removeAt(0), cards.removeAt(0))
    }

    fun resetAndShuffle() {
        reset()
        shuffle()
    }


    private fun reset() {
        cards.clear()
        CardSuit.entries.forEach { suit ->
            CardRank.entries.forEach { rank ->
                cards.add(PlayingCard(suit, rank))
            }
        }
    }

    private fun shuffle() {
        cards.shuffle()
    }


}