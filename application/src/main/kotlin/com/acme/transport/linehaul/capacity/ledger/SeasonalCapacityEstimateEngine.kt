package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal capacity estimate calculation. */
interface SeasonalCapacityEstimateModifier {
    fun describe(): String
    fun adjust(item: SeasonalCapacityEstimate, value: Double): Double
}

data class CapSeasonalCapacityEstimateAdjuster(var cap: Double) : SeasonalCapacityEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCapacityEstimate, value: Double): Double = minOf(value, cap)
}

data class FlaggedSeasonalCapacityEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalCapacityEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalCapacityEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalCapacityEstimateAdjuster(var minimum: Double) : SeasonalCapacityEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalCapacityEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal capacity estimate steps in order and keeps a trace of each. */
data class SeasonalCapacityEstimateEngine(
    var steps: MutableList<SeasonalCapacityEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalCapacityEstimate): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalCapacityEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalCapacityEstimateEngine(flags: Registry): SeasonalCapacityEstimateEngine {
    return SeasonalCapacityEstimateEngine(
        steps = mutableListOf<SeasonalCapacityEstimateModifier>(
            CapSeasonalCapacityEstimateAdjuster(40948.0),
            FlaggedSeasonalCapacityEstimateAdjuster(flags, "seasonal-capacity-estimate-primary", 6.2),
            MinimumSeasonalCapacityEstimateAdjuster(2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 25838.4,
    )
}
