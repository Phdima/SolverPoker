package com.example.solverpoker.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.solverpoker.domain.pokerLogic.GameState
import com.example.solverpoker.domain.pokerLogic.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainerViewModel @Inject constructor() : ViewModel() {
    private val _gameState = mutableStateOf(createInitialGameState())
    val gameState: State<GameState> = _gameState

    fun startNewHand() {
        _gameState.value = _gameState.value.nextHand()
    }

    private fun createInitialGameState(): GameState {
        val players = listOf(
            Player(1, "Alice0", 1000),
            Player(2, "Bob1", 1000),
            Player(3, "Charlie2", 1000),
            Player(4, "Dave3", 1000),
            Player(5, "Sasha4", 1000),
            Player(6, "Masha5", 1000)
        )
        return GameState(
            players = players,
            dealerPosition = 0,
            currentPlayerIndex = 0,
            pot = 0
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