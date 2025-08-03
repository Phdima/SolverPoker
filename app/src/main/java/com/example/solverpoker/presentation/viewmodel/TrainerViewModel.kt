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

    private var preSimulationState = _gameState.value

    init {
        startNewHand()
    }

    fun startNewHand() {
        viewModelScope.launch {
            _action.value = null
            _answerResult.value = null
            _feedbackMessage.value = null
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
            preSimulationState = currentState.copy()
            val updatedState = currentState.updatePlayerAction(
                currentState.players.find { it.isHero }?.id ?: return@launch,
                action
            )
            _gameState.value = updatedState

            if (action != Action.FOLD && action != Action.CALL) {
                val resumedState = resumeSimulateAfterHeroPreflop(updatedState)
                _gameState.value = resumedState
                val finalStateUpdate = resumeSimulationRound(resumedState)
                _gameState.value = finalStateUpdate
            }


        }
    }

    fun checkAnswer() {
        viewModelScope.launch {
            _action.value?.let { checkCorrectAnswer(preSimulationState) }
        }
    }

    private suspend fun simulateUntilHeroPreflop(state: GameState): GameState {
        Timber.d(
            "-------------------------------------- \n" +
                    "Starting preflop simulation"
        )
        var currentState = state
        var lastRaiser: Player? = findCurrentLastRaiser(state)


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


            val currentLastRaiser = findCurrentLastRaiser(currentState)
            val action = determineBotAction(player, currentLastRaiser)
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

            if (action == Action.THREE_BET) {
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

    private suspend fun resumeSimulateAfterHeroPreflop(state: GameState): GameState {
        Timber.d("Resuming preflop simulation after hero action")
        var currentState = state
        var lastRaiser: Player? = findCurrentLastRaiser(currentState)

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

            lastRaiser = findCurrentLastRaiser(currentState)
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

        Timber.d(
            "Resumed preflop simulation completed. Last raiser: ${lastRaiser?.position ?: "NONE"}\n" +
                    "------------------------------------"
        )
        return currentState
    }

    private suspend fun resumeSimulationRound(state: GameState): GameState {
        Timber.d("Round preflop simulation")
        var currentState = state
        var lastRaiser = findCurrentLastRaiser(currentState)
        val activePlayers = currentState.players.filter { it.action != Action.FOLD }.toMutableList()


        if (activePlayers.size <= 1) {
            Timber.d("Only one active player left, skipping simulation")
            return currentState
        }

        val startPosition = lastRaiser?.position ?: Position.UTG
        var currentIndex = positionsOrder.indexOf(startPosition)
        var actionsCount = 0
        val maxActions = activePlayers.size * 3
        val actedPlayers = mutableSetOf<Int>()

        currentIndex = (currentIndex + 1) % positionsOrder.size
        Timber.d("Starting from position: ${positionsOrder[currentIndex]}")

        while (activePlayers.size > 1 && actionsCount < maxActions) {
            val currentPosition = positionsOrder[currentIndex]
            val player = activePlayers.find {
                it.position == currentPosition && it.action != Action.FOLD && it.id !in actedPlayers
            }


            if (player != null && !player.isHero) {
                Timber.d("Processing bot: ${player.position} with cards: ${player.cards}")

                lastRaiser = findCurrentLastRaiser(currentState)
                val action = determineBotAction(player, lastRaiser)
                Timber.d("Bot ${player.position} decided to: $action")

                currentState = currentState.updatePlayerAction(player.id, action)
                actionsCount++
                actedPlayers.add(player.id)

                if (action == Action.FOLD || action == Action.CALL) {
                    activePlayers.removeAll { it.id == player.id }
                }

                if (action == Action.RAISE || action == Action.THREE_BET ||
                    action == Action.FOUR_BET || action == Action.PUSH
                ) {
                    lastRaiser = player
                    Timber.d("New last raiser: ${player.position}")
                }
            }
            currentIndex = (currentIndex + 1) % positionsOrder.size

            if (positionsOrder[currentIndex] == startPosition) {
                Timber.d("Completed full circle")
                if (actedPlayers.size >= activePlayers.size || actedPlayers.isEmpty()) {
                    Timber.d("Ending simulation after full circle")
                    break
                }
                actedPlayers.clear()
                Timber.d("Starting new betting round")
            }

        }
        Timber.d(
            "Round preflop simulation completed. Last raiser: ${lastRaiser?.position ?: "NONE"}\n" +
                    "------------------------------------"
        )
        return currentState
    }


    private suspend fun determineBotAction(player: Player, lastRaiser: Player?): Action {
        val chart = chartRepository.getChart(player.position) ?: return Action.FOLD.also {
            Timber.e("Chart not found for position: ${player.position}")
        }

        val handNotation = cardsToNotation(player.cards)
        Timber.d("Determining action for ${player.position} with hand: $handNotation")

        // Проверка невалидных действий lastRaiser
        if (lastRaiser == null) {
            Timber.d("No valid raiser - checking open raise range")
            val openRaiseRange = chart.openRaise ?: emptyList()
            return if (rangeChecker.isHandInRange(handNotation, openRaiseRange)) {
                Action.RAISE
            } else {
                Action.FOLD
            }
        }

        Timber.d("Raiser detected: ${lastRaiser.position}, action: ${lastRaiser.action}")

        return when (lastRaiser.action) {
            Action.RAISE -> {
                val responseRange = chart.vsOpenRaise?.get(lastRaiser.position)
                    ?.get(Action.THREE_BET) ?: emptyList()

                val callRange = chart.vsOpenRaise?.get(lastRaiser.position)
                    ?.get(Action.CALL) ?: emptyList()

                when {
                    rangeChecker.isHandInRange(handNotation, responseRange) -> Action.THREE_BET
                    rangeChecker.isHandInRange(handNotation, callRange) -> Action.CALL
                    else -> Action.FOLD
                }
            }

            Action.THREE_BET -> {
                val responseRange = chart.vsThreeBet?.get(lastRaiser.position)
                    ?.get(Action.FOUR_BET) ?: emptyList()
                val callRange = chart.vsThreeBet?.get(lastRaiser.position)
                    ?.get(Action.CALL) ?: emptyList()

                when {
                    rangeChecker.isHandInRange(handNotation, responseRange) -> Action.FOUR_BET
                    rangeChecker.isHandInRange(handNotation, callRange) -> Action.CALL
                    else -> Action.FOLD
                }
            }

            Action.FOUR_BET -> {
                val responseRange = chart.vsFourBet?.get(lastRaiser.position)
                    ?.get(Action.PUSH) ?: emptyList()
                val callRange = chart.vsFourBet?.get(lastRaiser.position)
                    ?.get(Action.CALL) ?: emptyList()

                when {
                    rangeChecker.isHandInRange(handNotation, responseRange) -> Action.PUSH
                    rangeChecker.isHandInRange(handNotation, callRange) -> Action.CALL
                    else -> Action.FOLD
                }
            }

            Action.PUSH -> {
                val callRange = chart.vsPush?.get(lastRaiser.position)
                    ?.get(Action.CALL) ?: emptyList()

                if (rangeChecker.isHandInRange(
                        handNotation,
                        callRange
                    )
                ) Action.CALL else Action.FOLD
            }

            // Обработка неожиданных действий
            else -> {
                Timber.w("Unexpected raiser action: ${lastRaiser.action}")
                Action.FOLD
            }
        }
    }

    private suspend fun checkCorrectAnswer(preSimulationState: GameState,) {
        Timber.d("Checking correct answer...")

        // Находим последнего рейзера перед героем
        val raiser = findLastRaiserBeforeHero(preSimulationState.copy())
        val opponentPosition = raiser?.position

        if (opponentPosition == null) {
            Timber.d("No raiser found before hero")

        } else {
            Timber.d("Last raiser before hero: $opponentPosition" + " " + "${raiser?.action}")
        }

        val action = _action.value ?: return.also {
            Timber.d("No action selected by user")
        }

        Timber.d("User selected action: $action")

        val result = try {
            val hero = preSimulationState.players.find { it.isHero } ?: return
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
                        Timber.d("Checking CALL action vs $opponentPosition + ${raiser?.action}")
                        val callRange = when (raiser?.action) {
                            Action.RAISE ->
                                chart.vsOpenRaise?.get(opponentPosition)?.get(Action.CALL)
                            Action.THREE_BET ->
                                chart.vsThreeBet?.get(opponentPosition)?.get(Action.CALL)
                            Action.FOUR_BET ->
                                chart.vsFourBet?.get(opponentPosition)?.get(Action.CALL)
                            Action.PUSH ->
                                chart.vsPush?.get(opponentPosition)?.get(Action.CALL)
                            else -> null
                        } ?: emptyList()

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

                        val inFourBet =
                            chart.vsThreeBet?.get(opponentPosition)?.get(Action.FOUR_BET)?.let {
                                rangeChecker.isHandInRange(handNotation, it)
                            } ?: false

                        val inCallRangeForThreeBet =
                            chart.vsThreeBet?.get(opponentPosition)?.get(Action.CALL)?.let {
                                rangeChecker.isHandInRange(handNotation, it)
                            } ?: false

                        val inPush =
                            chart.vsFourBet?.get(opponentPosition)?.get(Action.PUSH)?.let {
                                rangeChecker.isHandInRange(handNotation, it)
                            } ?: false

                        val inCallRangeForFourBet =
                            chart.vsFourBet?.get(opponentPosition)?.get(Action.CALL)?.let {
                                rangeChecker.isHandInRange(handNotation, it)
                            } ?: false

                        val inCallRangeForPush =
                            chart.vsPush?.get(opponentPosition)?.get(Action.CALL)?.let {
                                rangeChecker.isHandInRange(handNotation, it)
                            } ?: false

                        Timber.d("Hand in ranges: openRaise=$inOpenRaise, call=$inCallRange $inCallRangeForThreeBet $inCallRangeForFourBet $inCallRangeForPush, 3bet=$inThreeBet, 4bet =$inFourBet, push =$inPush")

                        !(inOpenRaise || inCallRange || inThreeBet || inFourBet || inCallRangeForThreeBet || inPush || inCallRangeForFourBet || inCallRangeForPush)
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

    private fun findLastRaiserBeforeHero(state: GameState): Player? {
        val players = state.players
        val hero = players.find { it.isHero } ?: return null
        val heroPositionIndex = positionsOrder.indexOf(hero.position)

        // Собираем всех рейзеров (кроме героя)
        val raisers = players.filter { player ->
            player.id != hero.id && (
                    player.action == Action.RAISE ||
                            player.action == Action.THREE_BET ||
                            player.action == Action.FOUR_BET ||
                            player.action == Action.PUSH
                    )
        }

        if (raisers.isEmpty()) return null

        // Вычисляем циклическое расстояние до героя
        return raisers.maxByOrNull { raiser ->
            val raiserIndex = positionsOrder.indexOf(raiser.position)
            if (raiserIndex < heroPositionIndex) {
                // Рейзер перед героем в обычном порядке
                raiserIndex + positionsOrder.size
            } else {
                // Рейзер после героя в обычном порядке (но перед ним в циклическом)
                raiserIndex
            }
        }
    }


    private fun findCurrentLastRaiser(state: GameState): Player? {
        Timber.d("Finding raiser. Players: ${state.players.map { "${it.position}:${it.action}" }}")
        return state.players
            .filter { player ->
                player.action?.let { action ->
                    action == Action.RAISE ||
                            action == Action.THREE_BET ||
                            action == Action.FOUR_BET ||
                            action == Action.PUSH
                } ?: false
            }
            .maxByOrNull { positionsOrder.indexOf(it.position) }
    }

    private fun generateFeedbackMessage(action: Action, isCorrect: Boolean): String {
        return when {
            isCorrect && action == Action.RAISE -> "Правильно! Рука в диапазоне"
            isCorrect && action == Action.CALL -> "Правильно! Рука в диапазоне"
            isCorrect && action == Action.FOLD -> "Правильно! Рука вне диапазона"
            isCorrect && action == Action.THREE_BET -> "Правильно! Рука в диапазоне"
            isCorrect && action == Action.FOUR_BET -> "Правильно! Рука в диапазоне"
            isCorrect && action == Action.PUSH -> "Правильно! Рука в диапазоне"
            !isCorrect && action == Action.RAISE -> "Неправильно! Рука вне диапазона"
            !isCorrect && action == Action.CALL -> "Неправильно! Рука вне диапазона"
            !isCorrect && action == Action.FOLD -> "Неправильно! Рука в диапазоне"
            !isCorrect && action == Action.THREE_BET -> "Неправильно! Рука вне диапазона"
            !isCorrect && action == Action.FOUR_BET -> "Неправильно! Рука вне диапазона"
            !isCorrect && action == Action.PUSH -> "Неправильно! Рука вне диапазона"
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
            Player(1, "Alice", 1000, cards = deck.drawCard(), position = Position.UTG),
            Player(2, "Bob", 1000, cards = deck.drawCard(), position = Position.MP),
            Player(3, "Charlie", 1000, cards = deck.drawCard(), position = Position.CO),
            Player(
                4,
                "HERO",
                1000,
                cards = deck.drawCard(),
                position = Position.BTN,
                isHero = true
            ),
            Player(5, "Sasha", 1000, cards = deck.drawCard(), position = Position.SB),
            Player(6, "Masha", 1000, cards = deck.drawCard(), position = Position.BB)
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