package com.example.solverpoker.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solverpoker.data.RangeParser
import com.example.solverpoker.domain.pokerLogic.CardRank
import com.example.solverpoker.domain.pokerLogic.Deck
import com.example.solverpoker.domain.pokerLogic.GameState
import com.example.solverpoker.domain.pokerLogic.Player
import com.example.solverpoker.domain.pokerLogic.PlayingCard
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChecker
import com.example.solverpoker.domain.pokerLogic.displaySymbol
import com.example.solverpoker.domain.repository.ChartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainerViewModel @Inject constructor(
    private val chartRepository: ChartRepository
) : ViewModel() {
    private val deck = Deck()

    private val rangeParser = RangeParser()
    private val rangeChecker = RangeChecker(rangeParser)

    private val _gameState = MutableStateFlow(createInitialGameState())
    val gameState: StateFlow<GameState> = _gameState

    private val _answerResult = mutableStateOf<Boolean?>(null)
    val answerResult: State<Boolean?> = _answerResult

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage

    fun startNewHand() {
        deck.resetAndShuffle()
        _gameState.value = _gameState.value.nextHand()
        _answerResult.value = null
        _feedbackMessage.value = null
    }

    fun checkAnswer() {
        viewModelScope.launch {
            checkCorrectAnswer()
        }
    }

    private suspend fun checkCorrectAnswer() {
        val result = try {
            val hero = gameState.value.players.find { it.isHero } ?: return
            val heroCards = hero.cards
            if (heroCards.size != 2) false
            else {
                val handNotation = cardsToNotation(heroCards)
                val chart = chartRepository.getChart(hero.position) ?: return
                rangeChecker.isHandInRange(handNotation, chart.openRaise ?: emptyList())
            }
        } catch (e: Exception) {
            false
        }
        _answerResult.value = result
        _feedbackMessage.value = if (result) "Правильно! Рука в диапазоне"
        else "Неправильно! Рука вне диапазона"
    }


    private fun cardsToNotation(cards: List<PlayingCard>): String {
        require(cards.size == 2) { "Hand must consist of exactly 2 cards" }

        val card1 = cards[0]
        val card2 = cards[1]

        // Определяем старшую и младшую карту
        val (high, low) = listOf(card1, card2).sortedByDescending {
            CardRank.entries.indexOf(it.rank)
        }

        return when {
            // Пара
            card1.rank == card2.rank ->
                "${card1.rank.displaySymbol()}${card2.rank.displaySymbol()}"

            // Одна масть (suited)
            card1.suit == card2.suit ->
                "${high.rank.displaySymbol()}${low.rank.displaySymbol()}s"

            // Разные масти (offsuit)
            else ->
                "${high.rank.displaySymbol()}${low.rank.displaySymbol()}o"
        }
    }

    private fun createInitialGameState(): GameState {
        val players = listOf(
            Player(1, "Alice0", 1000, cards = deck.drawCard(), position = Position.UTG),
            Player(2, "Bob1", 1000, cards = deck.drawCard(),  position = Position.MP),
            Player(3, "Charlie2", 1000, cards = deck.drawCard(),position = Position.CO),
            Player(4, "Dave3", 1000, cards = deck.drawCard(), position = Position.BTN, isHero = true),
            Player(5, "Sasha4", 1000, cards = deck.drawCard(), position = Position.SB),
            Player(6, "Masha5", 1000, cards = deck.drawCard(), position = Position.BB)
        )
        return GameState(
            players = players,
            dealerPosition = 0,
            currentPlayerIndex = 0,
            pot = 0,
            deck = deck
        ).nextHand()

    }

}