package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback roster summary calculation. */
interface FallbackRosterSummaryModifier {
    fun describe(): String
    fun adjust(item: FallbackRosterSummary, value: Double): Double
}

data class RoundingFallbackRosterSummaryAdjuster(var step: Double) : FallbackRosterSummaryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRosterSummary, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackRosterSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRosterSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRosterSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFallbackRosterSummaryAdjuster(var minimum: Double) : FallbackRosterSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRosterSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackRosterSummaryAdjuster(var cap: Double) : FallbackRosterSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRosterSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback roster summary steps in order and keeps a trace of each. */
data class FallbackRosterSummaryEngine(
    var steps: MutableList<FallbackRosterSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRosterSummary): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRosterSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRosterSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRosterSummaryEngine(): FallbackRosterSummaryEngine {
    return FallbackRosterSummaryEngine(
        steps = mutableListOf<FallbackRosterSummaryModifier>(
            RoundingFallbackRosterSummaryAdjuster(5.0),
            TieredFallbackRosterSummaryAdjuster(
                mutableListOf<Double>(83.0, 1607.0, 5366.0),
                mutableListOf<Double>(1.01, 0.82, 0.96),
            ),
            MinimumFallbackRosterSummaryAdjuster(8.2),
            CapFallbackRosterSummaryAdjuster(20815.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 4928.7,
    )
}
