package com.acme.transport.network.hub.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient hub baseline calculation. */
interface LenientHubBaselineModifier {
    fun describe(): String
    fun adjust(item: LenientHubBaseline, value: Double): Double
}

data class CapLenientHubBaselineAdjuster(var cap: Double) : LenientHubBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientHubBaseline, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientHubBaselineAdjuster(var minimum: Double) : LenientHubBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientHubBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLenientHubBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientHubBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientHubBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLenientHubBaselineAdjuster(var step: Double) : LenientHubBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientHubBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient hub baseline steps in order and keeps a trace of each. */
data class LenientHubBaselineEngine(
    var steps: MutableList<LenientHubBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientHubBaseline): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientHubBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientHubBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientHubBaselineEngine(): LenientHubBaselineEngine {
    return LenientHubBaselineEngine(
        steps = mutableListOf<LenientHubBaselineModifier>(
            CapLenientHubBaselineAdjuster(36575.0),
            MinimumLenientHubBaselineAdjuster(30.9),
            TieredLenientHubBaselineAdjuster(
                mutableListOf<Double>(1560.0, 2562.0, 6249.0),
                mutableListOf<Double>(0.93, 1.01, 0.87),
            ),
            RoundingLenientHubBaselineAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 23463.6,
    )
}
