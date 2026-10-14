package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global idling rollup calculation. */
interface GlobalIdlingRollupAdjuster {
    fun describe(): String
    fun adjust(item: GlobalIdlingRollup, value: Double): Double
}

data class MinimumGlobalIdlingRollupAdjuster(var minimum: Double) : GlobalIdlingRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalIdlingRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalIdlingRollupAdjuster(var step: Double) : GlobalIdlingRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalIdlingRollup, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalIdlingRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalIdlingRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalIdlingRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global idling rollup steps in order and keeps a trace of each. */
data class GlobalIdlingRollupEngine(
    var steps: MutableList<GlobalIdlingRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalIdlingRollup): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalIdlingRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalIdlingRollupEngine(): GlobalIdlingRollupEngine {
    return GlobalIdlingRollupEngine(
        steps = mutableListOf<GlobalIdlingRollupAdjuster>(
            MinimumGlobalIdlingRollupAdjuster(36.0),
            RoundingGlobalIdlingRollupAdjuster(0.1),
            TieredGlobalIdlingRollupAdjuster(
                mutableListOf<Double>(343.0, 2122.0, 3169.0),
                mutableListOf<Double>(1.01, 1.17, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 91726.2,
    )
}
