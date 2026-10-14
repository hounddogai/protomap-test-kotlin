package com.acme.transport.terminal.traileryard.yardforecast

/** One step of the seasonal trailer yard score calculation. */
interface SeasonalTrailerYardScoreFactor {
    fun describe(): String
    fun adjust(item: SeasonalTrailerYardScore, value: Double): Double
}

data class CategorySeasonalTrailerYardScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTrailerYardScoreFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: SeasonalTrailerYardScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SeasonalTrailerYardScoreStartsAtOffsetAdjuster(var weight: Double) : SeasonalTrailerYardScoreFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: SeasonalTrailerYardScore, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class CapSeasonalTrailerYardScoreAdjuster(var cap: Double) : SeasonalTrailerYardScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTrailerYardScore, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal trailer yard score steps in order and keeps a trace of each. */
data class SeasonalTrailerYardScoreEngine(
    var steps: MutableList<SeasonalTrailerYardScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalTrailerYardScore): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTrailerYardScoreFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTrailerYardScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerYardScoreEngine(): SeasonalTrailerYardScoreEngine {
    return SeasonalTrailerYardScoreEngine(
        steps = mutableListOf<SeasonalTrailerYardScoreFactor>(
            CategorySeasonalTrailerYardScoreAdjuster(
                mutableMapOf<String, Double>("central" to 0.9, "north" to 1.15),
            ),
            SeasonalTrailerYardScoreStartsAtOffsetAdjuster(0.83),
            CapSeasonalTrailerYardScoreAdjuster(5940.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 16043.3,
    )
}
