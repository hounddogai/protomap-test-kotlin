package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal cross dock score calculation. */
interface SeasonalCrossDockScoreAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalCrossDockScore, value: Double): Double
}

data class CapSeasonalCrossDockScoreAdjuster(var cap: Double) : SeasonalCrossDockScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCrossDockScore, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalCrossDockScoreAdjuster(var step: Double) : SeasonalCrossDockScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalCrossDockScore, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalCrossDockScoreAdjuster(var minimum: Double) : SeasonalCrossDockScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalCrossDockScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalCrossDockScoreLoadKgOffsetAdjuster(var weight: Double) : SeasonalCrossDockScoreAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: SeasonalCrossDockScore, value: Double): Double = value + item.loadKg * weight
}

/** Runs the seasonal cross dock score steps in order and keeps a trace of each. */
data class SeasonalCrossDockScoreEngine(
    var steps: MutableList<SeasonalCrossDockScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalCrossDockScore): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCrossDockScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCrossDockScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalCrossDockScoreEngine(): SeasonalCrossDockScoreEngine {
    return SeasonalCrossDockScoreEngine(
        steps = mutableListOf<SeasonalCrossDockScoreAdjuster>(
            CapSeasonalCrossDockScoreAdjuster(18990.0),
            RoundingSeasonalCrossDockScoreAdjuster(0.05),
            MinimumSeasonalCrossDockScoreAdjuster(38.4),
            SeasonalCrossDockScoreLoadKgOffsetAdjuster(1.98),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 74512.2,
    )
}
