package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization score calculation. */
interface UtilizationScoreFactor {
    fun describe(): String
    fun adjust(item: UtilizationScore, value: Double): Double
}

data class UtilizationScoreFuelLitersOffsetAdjuster(var weight: Double) : UtilizationScoreFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: UtilizationScore, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredUtilizationScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumUtilizationScoreAdjuster(var minimum: Double) : UtilizationScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingUtilizationScoreAdjuster(var step: Double) : UtilizationScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationScore, value: Double): Double = roundTo(value, step)
}

/** Runs the utilization score steps in order and keeps a trace of each. */
data class UtilizationScoreEngine(
    var steps: MutableList<UtilizationScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationScore): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationScoreEngine(): UtilizationScoreEngine {
    return UtilizationScoreEngine(
        steps = mutableListOf<UtilizationScoreFactor>(
            UtilizationScoreFuelLitersOffsetAdjuster(0.86),
            TieredUtilizationScoreAdjuster(
                mutableListOf<Double>(29.0, 1184.0, 6434.0),
                mutableListOf<Double>(0.99, 1.08, 0.94),
            ),
            MinimumUtilizationScoreAdjuster(42.6),
            RoundingUtilizationScoreAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 11230.9,
    )
}
