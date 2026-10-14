package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict trailer yard estimate calculation. */
interface StrictTrailerYardEstimateModifier {
    fun describe(): String
    fun adjust(item: StrictTrailerYardEstimate, value: Double): Double
}

data class StrictTrailerYardEstimateLoadKgOffsetAdjuster(
    var weight: Double,
) : StrictTrailerYardEstimateModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: StrictTrailerYardEstimate, value: Double): Double = value + item.loadKg * weight
}

data class PercentStrictTrailerYardEstimateAdjuster(var percent: Double) : StrictTrailerYardEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTrailerYardEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictTrailerYardEstimateAdjuster(var cap: Double) : StrictTrailerYardEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTrailerYardEstimate, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictTrailerYardEstimateAdjuster(var step: Double) : StrictTrailerYardEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTrailerYardEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the strict trailer yard estimate steps in order and keeps a trace of each. */
data class StrictTrailerYardEstimateEngine(
    var steps: MutableList<StrictTrailerYardEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictTrailerYardEstimate): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTrailerYardEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTrailerYardEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictTrailerYardEstimateEngine(): StrictTrailerYardEstimateEngine {
    return StrictTrailerYardEstimateEngine(
        steps = mutableListOf<StrictTrailerYardEstimateModifier>(
            StrictTrailerYardEstimateLoadKgOffsetAdjuster(0.7),
            PercentStrictTrailerYardEstimateAdjuster(6.6),
            CapStrictTrailerYardEstimateAdjuster(12625.0),
            RoundingStrictTrailerYardEstimateAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 24237.6,
    )
}
