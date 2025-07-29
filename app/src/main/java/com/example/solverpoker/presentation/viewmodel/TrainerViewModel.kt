package com.example.solverpoker.presentation.viewmodel

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
    private val chartRepository: ChartRepository,
) : ViewModel() {
    private val positionsOrder =
        listOf(Position.UTG, Position.MP, Position.CO, Position.BTN, Position.SB, Position.BB)

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
            _gameState.value = simulateUntilHeroPreflop(currentState)
            Timber.d("-------------------------------------------")
        }

    }


    fun selectAction(action: Action) {
        _action.value = action
        viewModelScope.launch {
            val currentState = _gameState.value
            val updatedState = currentState.updatePlayerAction(
                currentState.players.find { it.isHero }?.id ?: return@launch,
                action
            )
            _gameState.value = updatedState


            if (action != Action.FOLD && action != Action.CALL) {
                val resumedState = resumeSimulatePreflop(updatedState)
                _gameState.value = resumedState
            }
        }
    }

    fun checkAnswer() {
        viewModelScope.launch {
            checkCorrectAnswer()
        }
    }

    private suspend fun simulateUntilHeroPreflop(state: GameState): GameState {
        Timber.d("Starting preflop simulation")
        var currentState = state
        var lastRaiser: Player? = null


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


            val action = determineBotAction(player, lastRaiser)
            Timber.d("Bot ${player.position} decided to: $action")

            currentState = currentState.updatePlayerAction(player.id, action)

            // Обновляем последнего рейзера
            if (action == Action.RAISE || action == Action.THREE_BET ||
                action == Action.FOUR_BET || action == Action.PUSH
            ) {
                lastRaiser = player
                Timber.d("New last raiser: ${player.position}")
            }
            if (lastRaiser == null && playersInOrder.getOrNull(heroIndex)?.position == Position.BB) {
                Timber.d(
                    "+++++++++++++++++++++++++++++++ \n " +
                            "стартанули некст руку ибо не было дейсвтий до героя который сидел на BB \n" +
                            "++++++++++++++++++++++++++++++++++++++++"
                )
                startNewHand()
                return currentState

            }

            if (lastRaiser?.action == Action.THREE_BET) {
                Timber.d(
                    "+++++++++++++++++++++++++++++++ \n " +
                            "стартанули некст руку ибо был мультипот решение , которые не будут учитываться в первой версии прило. \n" +
                            "++++++++++++++++++++++++++++++++++++++++"
                )
                startNewHand()
                return currentState
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

    private suspend fun resumeSimulatePreflop(state: GameState): GameState {
        Timber.d("Resuming preflop simulation after hero action")
        var currentState = state
        var lastRaiser: Player? = findCurrentLastRaiser(state)

        // Правильный порядок действий в префлопе
        val playersInOrder = state.players.sortedBy { positionsOrder.indexOf(it.position) }

        // Находим героя
        val heroIndex = playersInOrder.indexOfFirst { it.isHero }
        if (heroIndex == -1) return state

        // Обрабатываем игроков после героя
        for (i in heroIndex + 1 until playersInOrder.size) {
            val player = playersInOrder[i]

            // Пропускаем игроков, которые уже сфолдили
            if (player.action == Action.FOLD) {
                Timber.d("Skipping folded bot: ${player.position}")
                continue
            }

            Timber.d("Processing bot after hero: ${player.position} with cards: ${player.cards.joinToString { it.toString() }}")

            val action = determineBotAction(player, lastRaiser)
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

        Timber.d("Resumed preflop simulation completed. Last raiser: ${lastRaiser?.position ?: "NONE"}")
        return currentState
    }

    private fun findCurrentLastRaiser(state: GameState): Player? {
        return state.players
            .filter { player ->
                player.action == Action.RAISE ||
                        player.action == Action.THREE_BET ||
                        player.action == Action.FOUR_BET ||
                        player.action == Action.PUSH
            }
            .maxByOrNull { positionsOrder.indexOf(it.position) }
    }


    private suspend fun determineBotAction(player: Player, lastRaiser: Player?): Action {
        val chart = chartRepository.getChart(player.position) ?: return Action.FOLD.also {
            Timber.e("Chart not found for position: ${player.position}")
        }

        val handNotation = cardsToNotation(player.cards)
        Timber.d("Determining action for ${player.position} with hand: $handNotation")

        // Если не было рейзера перед нами
        if (lastRaiser == null) {
            Timber.d("No raiser before - checking open raise range")
            val openRaiseRange = chart.openRaise ?: emptyList()
            return if (rangeChecker.isHandInRange(handNotation, openRaiseRange)) {
                Action.RAISE
            } else {
                Action.FOLD
            }
        }

        Timber.d("Raiser detected: ${lastRaiser.position}, action: ${lastRaiser.action}")

        // Определяем, какой чарт использовать в зависимости от типа рейза
        val responseChart = when (lastRaiser.action) {
            Action.RAISE -> {
                Timber.d("Using vsOpenRaise chart")
                chart.vsOpenRaise?.get(lastRaiser.position) ?: emptyMap()
            }
            Action.THREE_BET -> {
                Timber.d("Using vsThreeBet chart")
                chart.vsThreeBet?.get(lastRaiser.position) ?: emptyMap()
            }
            Action.FOUR_BET -> {
                Timber.d("Using vsFourBet chart")
                chart.vsFourBet?.get(lastRaiser.position) ?: emptyMap()
            }
            Action.PUSH -> {
                Timber.d("Using vsPush chart")
                chart.vsPush?.get(lastRaiser.position) ?: emptyMap()
            }
            else -> {
                Timber.w("Unknown aggressive action: ${lastRaiser.action}, using vsOpenRaise as fallback")
                chart.vsOpenRaise?.get(lastRaiser.position) ?: emptyMap()
            }
        }

        // Определяем возможные ответные действия в зависимости от типа рейза
        return when (lastRaiser.action) {
            Action.RAISE -> {
                when {
                    responseChart[Action.THREE_BET]?.let {
                        rangeChecker.isHandInRange(handNotation, it)
                    } == true -> Action.THREE_BET

                    responseChart[Action.CALL]?.let {
                        rangeChecker.isHandInRange(handNotation, it)
                    } == true -> Action.CALL

                    else -> Action.FOLD
                }
            }
            Action.THREE_BET -> {
                when {
                    responseChart[Action.FOUR_BET]?.let {
                        rangeChecker.isHandInRange(handNotation, it)
                    } == true -> Action.FOUR_BET

                    responseChart[Action.CALL]?.let {
                        rangeChecker.isHandInRange(handNotation, it)
                    } == true -> Action.CALL

                    else -> Action.FOLD
                }
            }
            Action.FOUR_BET -> {
                when {
                    responseChart[Action.PUSH]?.let {
                        rangeChecker.isHandInRange(handNotation, it)
                    } == true -> Action.PUSH

                    responseChart[Action.CALL]?.let {
                        rangeChecker.isHandInRange(handNotation, it)
                    } == true -> Action.CALL

                    else -> Action.FOLD
                }
            }
            Action.PUSH -> {
                // На пуш обычно только колл или фолд
                if (responseChart[Action.CALL]?.let {
                        rangeChecker.isHandInRange(handNotation, it)
                    } == true) Action.CALL else Action.FOLD
            }
            else -> {
                // Фолд по умолчанию для неизвестных действий
                Action.FOLD
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

                    Action.WAIT -> {
                        false
                    }
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
            isCorrect && action == Action.THREE_BET -> "Правильно! Рука в диапазоне для 3-бета"
            !isCorrect && action == Action.RAISE -> "Неправильно! Рука вне диапазона для рейза"
            !isCorrect && action == Action.CALL -> "Неправильно! Рука вне диапазона для колла"
            !isCorrect && action == Action.FOLD -> "Неправильно! Рука в диапазоне, не нужно фолдить"
            !isCorrect && action == Action.THREE_BET -> "Неправильно! Рука вне диапазона для 3-бета"
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