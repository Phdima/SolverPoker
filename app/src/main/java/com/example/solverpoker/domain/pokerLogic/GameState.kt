package com.example.solverpoker.domain.pokerLogic

data class GameState(
    val players: List<Player>,
    val dealerPosition: Int, // Индекс дилера
    val currentPlayerIndex: Int,
    val pot: Int,
    val smallBlindAmount: Int = 10,
    val bigBlindAmount: Int = 20
) {
    // Обновляем состояния игроков
    fun nextHand(): GameState {
        val newDealerPosition = (dealerPosition + 1) % players.size
        return copy(
            dealerPosition = newDealerPosition,
            players = players.mapIndexed { index, player ->
                player.copy(
                    isDealer = index == newDealerPosition,
                    isSmallBlind = index == (newDealerPosition + 1) % players.size,
                    isBigBlind = index == (newDealerPosition + 2) % players.size
                )
            }
        )
    }
}
