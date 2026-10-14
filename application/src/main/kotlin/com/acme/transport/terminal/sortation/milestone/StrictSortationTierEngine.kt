package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict sortation tier calculation. */
interface StrictSortationTierModifier {
    fun describe(): String
    fun adjust(item: StrictSortationTier, value: Double): Double
}

data class MinimumStrictSortationTierAdjuster(var minimum: Double) : StrictSortationTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictSortationTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapStrictSortationTierAdjuster(var cap: Double) : StrictSortationTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictSortationTier, value: Double): Double = minOf(value, cap)
}

data class TieredStrictSortationTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictSortationTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictSortationTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleStrictSortationTierAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictSortationTierModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictSortationTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class StrictSortationTierQuantityOffsetAdjuster(var weight: Double) : StrictSortationTierModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: StrictSortationTier, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingStrictSortationTierAdjuster(var step: Double) : StrictSortationTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictSortationTier, value: Double): Double = roundTo(value, step)
}

/** Runs the strict sortation tier steps in order and keeps a trace of each. */
data class StrictSortationTierEngine(
    var steps: MutableList<StrictSortationTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictSortationTier): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictSortationTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictSortationTierEngine(): StrictSortationTierEngine {
    return StrictSortationTierEngine(
        steps = mutableListOf<StrictSortationTierModifier>(
            MinimumStrictSortationTierAdjuster(14.7),
            CapStrictSortationTierAdjuster(42547.0),
            TieredStrictSortationTierAdjuster(
                mutableListOf<Double>(1065.0, 3858.0),
                mutableListOf<Double>(0.84, 1.1),
            ),
            TitleStrictSortationTierAdjuster(mutableMapOf<String, Double>("east" to 0.72, "central" to 0.96)),
            StrictSortationTierQuantityOffsetAdjuster(1.91),
            RoundingStrictSortationTierAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 45615.9,
    )
}
