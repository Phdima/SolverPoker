package com.example.solverpoker.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solverpoker.data.RangeParser
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.CardRank
import com.example.solverpoker.domain.pokerLogic.Deck
import com.example.solverpoker.domain.pokerLogic.GameState
import com.example.solverpoker.domain.pokerLogic.Player
import com.example.solverpoker.domain.pokerLogic.PlayingCard
import com.example.solverpoker.domain.pokerLogic.Position
import com.example.solverpoker.domain.pokerLogic.RangeChart
import com.example.solverpoker.domain.pokerLogic.RangeChecker
import com.example.solverpoker.domain.pokerLogic.displaySymbol
import com.example.solverpoker.domain.repository.ChartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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

    private val _action = mutableStateOf<Action?>(null)
    val action: State<Action?> = _action

    private val _answerResult = mutableStateOf<Boolean?>(null)
    val answerResult: State<Boolean?> = _answerResult

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage

    init {
        startNewHand()
    }

    fun startNewHand() {
        viewModelScope.launch {
            deck.resetAndShuffle()
            _gameState.value = _gameState.value.nextHand()
            val currentState = _gameState.value
            _gameState.value = simulatePreflop(currentState)
            Timber.d("-------------------------------------------")
        }

    }


    fun selectAction(action: Action) {
        _action.value = action
    }

    fun checkAnswer() {
        viewModelScope.launch {
            checkCorrectAnswer()
        }
    }

    private suspend fun simulatePreflop(state: GameState): GameState {
        Timber.d("Starting preflop simulation")
        var currentState = state
        var lastRaiser: Player? = null

        // Правильный порядок действий в префлопе
        val positionsOrder =
            listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.SB, Position.BB)
        val playersInOrder = state.players.sortedBy { positionsOrder.indexOf(it.position) }

        Timber.d("Players in action order:")
        playersInOrder.forEach { player ->
            Timber.d("${player.position} (${if (player.isHero) "HERO" else "BOT"})")
        }

        // Находим героя
        val heroIndex = playersInOrder.indexOfFirst { it.isHero }
        Timber.d("Hero index: $heroIndex at ${playersInOrder.getOrNull(heroIndex)?.position}")

        // Обрабатываем только игроков до героя
        for (i in 0 until heroIndex) {
            val player = playersInOrder[i]
            Timber.d("Processing bot: ${player.position} with cards: ${player.cards.joinToString { it.toString() }}")


            val action = determineBotAction(player, lastRaiser?.position)
            Timber.d("Bot ${player.position} decided to: $action")

            currentState = currentState.updatePlayerAction(player.id, action)

            // Обновляем последнего рейзера
            if (action == Action.RAISE || action == Action.THREE_BET ||
                action == Action.FOUR_BET || action == Action.PUSH
            ) {
                lastRaiser = player
                Timber.d("New last raiser: ${player.position}")
            }
        }
        for (i in heroIndex + 1 until playersInOrder.size) {
            val player = playersInOrder[i]
            Timber.d("Setting WAIT for bot (after hero): ${player.position}")
            currentState = currentState.updatePlayerAction(player.id, Action.WAIT)
        }
        Timber.d("Preflop simulation completed. Last raiser: ${lastRaiser?.position ?: "NONE"}")
        return currentState
    }

    private suspend fun determineBotAction(player: Player, raiserPosition: Position?): Action {
        val chart = chartRepository.getChart(player.position) ?: return Action.FOLD.also {
            Timber.e("Chart not found for position: ${player.position}")
        }


        val handNotation = cardsToNotation(player.cards)
        Timber.d("Determining action for ${player.position} with hand: $handNotation")

        return if (raiserPosition == null ) {
            Timber.d("No raiser before - checking open raise range")
            val openRaiseRange = chart.openRaise ?: emptyList()
            if (rangeChecker.isHandInRange(handNotation, openRaiseRange)) {
                Action.RAISE
            } else {
                Action.FOLD
            }
        } else {
            Timber.d("Raiser detected: $raiserPosition - checking response range")
            val vsRaise = chart.vsOpenRaise?.get(raiserPosition) ?: emptyMap()
            when {
                vsRaise[Action.THREE_BET]?.let {
                    rangeChecker.isHandInRange(handNotation, it)
                } == true -> Action.THREE_BET

                vsRaise[Action.CALL]?.let {
                    rangeChecker.isHandInRange(handNotation, it)
                } == true -> Action.CALL

                else -> Action.FOLD
            }
        }
    }

    private suspend fun checkCorrectAnswer() {
        Timber.d("Checking correct answer...")

        // Находим последнего рейзера перед героем
        val raiser = findLastRaiserBeforeHero()
        val opponentPosition = raiser?.position

        if (opponentPosition == null) {
            Timber.d("No raiser found before hero")
        } else {
            Timber.d("Last raiser before hero: $opponentPosition")
        }

        val action = _action.value ?: return.also {
            Timber.d("No action selected by user")
        }

        Timber.d("User selected action: $action")

        val result = try {
            val hero = gameState.value.players.find { it.isHero } ?: return
            val heroCards = hero.cards
            if (heroCards.size != 2) {
                Timber.e("Hero doesn't have 2 cards")
                false
            } else {
                val handNotation = cardsToNotation(heroCards)
                Timber.d("Hero hand: $handNotation")

                val chart = chartRepository.getChart(hero.position) ?: return

                when (action) {
                    Action.RAISE -> {
                        if (opponentPosition == null) {
                            Timber.d("Checking RAISE action")
                            val raiseRange = chart.openRaise ?: emptyList()
                            rangeChecker.isHandInRange(handNotation, raiseRange).also {
                                Timber.d("Hand in open raise range: $it")
                            }
                        } else {
                            false
                        }
                    }

                    Action.THREE_BET, Action.FOUR_BET, Action.PUSH -> {
                        Timber.d("Checking $action action")
                        val range = when (action) {
                            Action.THREE_BET -> chart.vsOpenRaise?.get(opponentPosition)
                                ?.get(Action.THREE_BET)

                            Action.FOUR_BET -> chart.vsThreeBet?.get(opponentPosition)
                                ?.get(Action.FOUR_BET)

                            Action.PUSH -> chart.vsFourBet?.get(opponentPosition)?.get(Action.PUSH)
                            else -> null
                        } ?: emptyList()

                        rangeChecker.isHandInRange(handNotation, range).also {
                            Timber.d("Hand in $action range: $it")
                        }
                    }

                    Action.CALL -> {
                        Timber.d("Checking CALL action")
                        val callRange = chart.vsOpenRaise?.get(opponentPosition)?.get(Action.CALL)
                            ?: emptyList()
                        rangeChecker.isHandInRange(handNotation, callRange).also {
                            Timber.d("Hand in call range: $it")
                        }
                    }

                    Action.FOLD -> {
                        Timber.d("Checking FOLD action")
                        // Проверяем, что рука НЕ входит ни в один игровой диапазон

                        val inOpenRaise = chart.openRaise?.let {
                            if (opponentPosition == null) {
                                rangeChecker.isHandInRange(handNotation, it)
                            } else false
                        } ?: false

                        val inCallRange =
                            chart.vsOpenRaise?.get(opponentPosition)?.get(Action.CALL)?.let {
                                rangeChecker.isHandInRange(handNotation, it)
                            } ?: false

                        val inThreeBet =
                            chart.vsOpenRaise?.get(opponentPosition)?.get(Action.THREE_BET)?.let {
                                rangeChecker.isHandInRange(handNotation, it)
                            } ?: false

                        Timber.d("Hand in ranges: openRaise=$inOpenRaise, call=$inCallRange, 3bet=$inThreeBet")

                        !(inOpenRaise || inCallRange || inThreeBet)
                    }
                    Action.WAIT -> {false}
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking correct answer")
            false
        }

        Timber.d("Answer result: $result")
        _answerResult.value = result
        _feedbackMessage.value = generateFeedbackMessage(action, result)
    }

    private fun findLastRaiserBeforeHero(): Player? {
        val positionsOrder = listOf(
            Position.UTG,
            Position.MP,
            Position.CO,
            Position.BTN,
            Position.SB,
            Position.BB
        )

        // Сортируем игроков по реальному порядку действий
        val playersInActionOrder = gameState.value.players.sortedBy {
            positionsOrder.indexOf(it.position)
        }

        // Находим индекс героя в правильном порядке действий
        val heroIndex = playersInActionOrder.indexOfFirst { it.isHero }
        if (heroIndex == -1) return null

        // Ищем только среди игроков, которые действуют ДО героя
        return playersInActionOrder
            .subList(0, heroIndex)
            .reversed()
            .firstOrNull { player ->
                player.action == Action.RAISE ||
                        player.action == Action.THREE_BET ||
                        player.action == Action.FOUR_BET ||
                        player.action == Action.PUSH
            }
    }

    private fun generateFeedbackMessage(action: Action, isCorrect: Boolean): String {
        return when {
            isCorrect && action == Action.RAISE -> "Правильно! Рука в диапазоне для рейза"
            isCorrect && action == Action.CALL -> "Правильно! Рука в диапазоне для колла"
            isCorrect && action == Action.FOLD -> "Правильно! Рука вне диапазона"
            !isCorrect && action == Action.RAISE -> "Неправильно! Рука вне диапазона для рейза"
            !isCorrect && action == Action.CALL -> "Неправильно! Рука вне диапазона для колла"
            !isCorrect && action == Action.FOLD -> "Неправильно! Рука в диапазоне, не нужно фолдить"
            else -> "Результат неизвестен"
        }
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
            Player(2, "Bob1", 1000, cards = deck.drawCard(), position = Position.MP),
            Player(3, "Charlie2", 1000, cards = deck.drawCard(), position = Position.CO),
            Player(
                4,
                "HERO",
                1000,
                cards = deck.drawCard(),
                position = Position.BTN,
                isHero = true
            ),
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