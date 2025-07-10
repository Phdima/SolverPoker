package com.example.solverpoker.domain.pokerLogic

data class GameState(
    val players: List<Player>,
    val deck: Deck,
    val dealerPosition: Int, // Индекс дилера
    val currentPlayerIndex: Int,
    val pot: Int,
    val smallBlindAmount: Int = 10,
    val bigBlindAmount: Int = 20
) {
    // Обновляем состояния игроков
    fun nextHand(): GameState {
        val newDealerPosition = (dealerPosition + 1) % players.size

        val initialPositions = listOf(
            Position.BTN,  // Игрок 0 - дилер
            Position.SB,   // Игрок 1
            Position.BB,   // Игрок 2
            Position.UTG,  // Игрок 3
            Position.MP,   // Игрок 4
            Position.CO    // Игрок 5
        )

        val newPositions = List(players.size) { index ->
            // Вычисляем относительную позицию от дилера
            val positionIndex = (index - newDealerPosition + players.size) % players.size
            initialPositions[positionIndex]
        }
        return copy(
            dealerPosition = newDealerPosition,
            players = players.mapIndexed { index, player ->
                player.copy(
                    position = newPositions[index],
                    isDealer = index == newDealerPosition,
                    isSmallBlind = index == (newDealerPosition + 1) % players.size,
                    isBigBlind = index == (newDealerPosition + 2) % players.size,
                    cards = deck.drawCard()
                )
            }
        )
    }
    fun updatePlayerAction(playerId: Int, action: Action): GameState {
        return copy(
            players = players.map { player ->
                if (player.id == playerId && !player.isHero) player.copy(action = action) else player
            }
        )
    }

}
