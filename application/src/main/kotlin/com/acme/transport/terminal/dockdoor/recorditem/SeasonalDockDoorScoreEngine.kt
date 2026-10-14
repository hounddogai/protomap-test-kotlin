package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal dock door score calculation. */
interface SeasonalDockDoorScoreStep {
    fun describe(): String
    fun adjust(item: SeasonalDockDoorScore, value: Double): Double
}

data class TieredSeasonalDockDoorScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDockDoorScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDockDoorScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSeasonalDockDoorScoreAdjuster(var step: Double) : SeasonalDockDoorScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDockDoorScore, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalDockDoorScoreAdjuster(var percent: Double) : SeasonalDockDoorScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDockDoorScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SeasonalDockDoorScoreCreatedAtOffsetAdjuster(var weight: Double) : SeasonalDockDoorScoreStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: SeasonalDockDoorScore, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

/** Runs the seasonal dock door score steps in order and keeps a trace of each. */
data class SeasonalDockDoorScoreEngine(
    var steps: MutableList<SeasonalDockDoorScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalDockDoorScore): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalDockDoorScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalDockDoorScoreEngine(): SeasonalDockDoorScoreEngine {
    return SeasonalDockDoorScoreEngine(
        steps = mutableListOf<SeasonalDockDoorScoreStep>(
            TieredSeasonalDockDoorScoreAdjuster(
                mutableListOf<Double>(2036.0, 9392.0),
                mutableListOf<Double>(0.83, 0.95),
            ),
            RoundingSeasonalDockDoorScoreAdjuster(5.0),
            PercentSeasonalDockDoorScoreAdjuster(-12.6),
            SeasonalDockDoorScoreCreatedAtOffsetAdjuster(0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 19786.5,
    )
}
