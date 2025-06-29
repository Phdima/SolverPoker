package com.example.solverpoker.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.solverpoker.domain.pokerLogic.Deck
import com.example.solverpoker.domain.pokerLogic.GameState
import com.example.solverpoker.domain.pokerLogic.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainerViewModel @Inject constructor() : ViewModel() {
    private val deck = Deck()

    private val _gameState = mutableStateOf(createInitialGameState())
    val gameState: State<GameState> = _gameState

    fun startNewHand() {
        _gameState.value = _gameState.value.nextHand()
        deck.resetAndShuffle()
    }

    private fun createInitialGameState(): GameState {
        val players = listOf(
            Player(1, "Alice0", 1000, cards = deck.drawCard()),
            Player(2, "Bob1", 1000, cards = deck.drawCard()),
            Player(3, "Charlie2", 1000, cards = deck.drawCard()),
            Player(4, "Dave3", 1000, cards = deck.drawCard()),
            Player(5, "Sasha4", 1000, cards = deck.drawCard()),
            Player(6, "Masha5", 1000, cards = deck.drawCard())
        )
        return GameState(
            players = players,
            dealerPosition = 0,
            currentPlayerIndex = 0,
            pot = 0,
            deck = deck
        ).nextHand()

    }

    fun placeBlinds() {
        val state = _gameState.value
        val updatedPlayers = state.players.map { player ->
            when {
                player.isSmallBlind -> player.copy(chips = player.chips - state.smallBlindAmount)
                player.isBigBlind -> player.copy(chips = player.chips - state.bigBlindAmount)
                else -> player
            }
        }

        val newPot = state.pot + state.smallBlindAmount + state.bigBlindAmount

        _gameState.value = state.copy(
            players = updatedPlayers,
            pot = newPot,
            currentPlayerIndex = (state.dealerPosition + 3) % state.players.size
        )
    }
}