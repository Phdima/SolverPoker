package com.example.solverpoker.data

class RangeParser {
    private val pairRegex = Regex("(\\d{2}|[TJQKA]{2})\\+?")                    // 22+, TT+
    private val hyphenatedPairRegex = Regex("([2-9TJQKA]{2})-([2-9TJQKA]{2})")  // 22-AA
    private val suitedRegex = Regex("([2-9TJQKA])([2-9TJQKA])s\\+?")            // 78s+, ATs+
    private val offsuitRegex = Regex("([2-9TJQKA])([2-9TJQKA])o\\+?")           // KQo+, J9o+

    private val ranks = listOf("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A")

    fun parse(rangeStr: String): Set<String> {
        val hands = mutableSetOf<String>()
        val parts = rangeStr.split(", ")

        for (part in parts) {
            when {
                part.matches(pairRegex) -> handlePairs(part, hands)
                part.matches(hyphenatedPairRegex) -> handleHyphenatedPairs(part, hands)
                part.matches(suitedRegex) -> handleSuited(part, hands)
                part.matches(offsuitRegex) -> handleOffsuit(part, hands)
                else -> hands.add(part)
            }
        }
        return hands
    }

    private fun handlePairs(part: String, hands: MutableSet<String>) {
        val rank = part.substring(0, 2)
        val startRank = rank[0].toString()
        val startIndex = ranks.indexOf(startRank)

        if (startIndex != -1) {
            for (i in startIndex until ranks.size) {
                hands.add("${ranks[i]}${ranks[i]}")
            }
        }
    }

    private fun handleHyphenatedPairs(part: String, hands: MutableSet<String>) {
        val match = hyphenatedPairRegex.find(part) ?: return
        val (start, end) = match.destructured

        val startRank = start.take(1)
        val endRank = end.take(1)

        val startIndex = ranks.indexOf(startRank)
        val endIndex = ranks.indexOf(endRank)

        if (startIndex != -1 && endIndex != -1) {
            for (i in startIndex..endIndex) {
                val rank = ranks[i]
                hands.add("$rank$rank")
            }
        }
    }



    private fun handleSuited(part: String, hands: MutableSet<String>) {
        val (highCard, lowCard) = parseHandComponents(part)
        generateCombos(highCard, lowCard, hands, isSuited = true)
    }

    private fun handleOffsuit(part: String, hands: MutableSet<String>) {
        val (highCard, lowCard) = parseHandComponents(part)
        generateCombos(highCard, lowCard, hands, isSuited = false)
    }



    private fun parseHandComponents(part: String): Pair<String, String> {
        val cards = part.replace("[+o]".toRegex(), "").take(2)
        val card1 = cards[0].toString()
        val card2 = cards[1].toString()

        val index1 = ranks.indexOf(card1)
        val index2 = ranks.indexOf(card2)
        return if (index1 >= index2) card1 to card2 else card2 to card1
    }

    private fun generateCombos(
        highCard: String,
        lowCard: String,
        hands: MutableSet<String>,
        isSuited: Boolean
    ) {
        val suffix = if (isSuited) "s" else "o"
        val highIndex = ranks.indexOf(highCard)
        val lowIndex = ranks.indexOf(lowCard)

        // Генерируем все комбинации от lowCard до highCard
        for (i in lowIndex until highIndex + 1) {
            for (j in i + 1 until ranks.size) {
                val hand = "${ranks[j]}${ranks[i]}${suffix}"
                hands.add(hand)
            }
        }
    }

}