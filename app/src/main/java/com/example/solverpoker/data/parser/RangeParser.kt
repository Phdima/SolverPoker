package com.example.solverpoker.data.parser

class RangeParser {
    private val pairRegex = Regex("(\\d{2}|[TJQKA]{2})\\+?")             // 22+, TT+
    private val hyphenatedPairRegex = Regex("([2-9TJQKA]{2})-([2-9TJQKA]{2})")  // 22-AA
    private val suitedRegex = Regex("([TJQKA2-9])([TJQKA2-9])s\\+?")         // 78s+, ATs+
    private val offsuitRegex = Regex("([TJQKA2-9])([TJQKA2-9])o\\+?")          // KQo+, J9o+


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
        val hasPlus = part.endsWith("+") // Добавляем проверку на плюс
        val basePair = part.removeSuffix("+")

        if (basePair.length != 2 || basePair[0] != basePair[1]) return

        val startRank = basePair[0].toString()
        val startIndex = ranks.indexOf(startRank)

        if (startIndex == -1) return

        // Определяем конечный индекс
        val endIndex = if (hasPlus) ranks.size - 1 else startIndex

        // Генерируем все пары от startIndex до endIndex
        for (i in startIndex..endIndex) {
            val pair = ranks[i] + ranks[i]
            hands.add(pair)
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
        val hasPlus = part.endsWith("+")
        val (highCard, lowCard) = parseHandComponents(part)
        generateSuitCombos(highCard, lowCard, hands, hasPlus)
    }

    private fun handleOffsuit(part: String, hands: MutableSet<String>) {
        val hasPlus = part.endsWith("+")
        val (highCard, lowCard) = parseHandComponents(part)
        generateOffsuitCombos(highCard, lowCard, hands, hasPlus)
    }



    private fun parseHandComponents(part: String): Pair<String, String> {
        val cleanPart = part.replace("[+os]".toRegex(), "")
        val sorted = cleanPart.toList().sortedByDescending { ranks.indexOf(it.toString()) }
        return sorted[0].toString() to sorted[1].toString()
    }


    private fun generateSuitCombos(
        highCard: String,
        lowCard: String,
        hands: MutableSet<String>,
        hasPlus: Boolean
    ) {
        val highIndex = ranks.indexOf(highCard)
        val lowIndex = ranks.indexOf(lowCard)


        if (!hasPlus) {
            hands.add("${highCard}${lowCard}s")
            return
        }

        for (i in lowIndex..highIndex) {
            val currentLow = ranks[i]
            if (currentLow == highCard) continue
            hands.add("${highCard}${currentLow}s")
        }
    }

    private fun generateOffsuitCombos(
        highCard: String,
        lowCard: String,
        hands: MutableSet<String>,
        hasPlus: Boolean
    ) {
        val highIndex = ranks.indexOf(highCard)
        val lowIndex = ranks.indexOf(lowCard)


        if (!hasPlus) {
            hands.add("${highCard}${lowCard}o")
            return
        }

        for (i in lowIndex..highIndex) {
            val currentLow = ranks[i]
            if (currentLow == highCard) continue
            hands.add("${highCard}${currentLow}o")
        }
    }


}