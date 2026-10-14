package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback trailer yard variance calculation. */
interface FallbackTrailerYardVarianceStep {
    fun describe(): String
    fun adjust(item: FallbackTrailerYardVariance, value: Double): Double
}

data class MinimumFallbackTrailerYardVarianceAdjuster(var minimum: Double) : FallbackTrailerYardVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTrailerYardVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackTrailerYardVarianceAdjuster(var step: Double) : FallbackTrailerYardVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackTrailerYardVariance, value: Double): Double = roundTo(value, step)
}

data class LaneFallbackTrailerYardVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackTrailerYardVarianceStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: FallbackTrailerYardVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackTrailerYardVarianceVersionOffsetAdjuster(
    var weight: Double,
) : FallbackTrailerYardVarianceStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: FallbackTrailerYardVariance, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

/** Runs the fallback trailer yard variance steps in order and keeps a trace of each. */
data class FallbackTrailerYardVarianceEngine(
    var steps: MutableList<FallbackTrailerYardVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackTrailerYardVariance): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackTrailerYardVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackTrailerYardVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackTrailerYardVarianceEngine(): FallbackTrailerYardVarianceEngine {
    return FallbackTrailerYardVarianceEngine(
        steps = mutableListOf<FallbackTrailerYardVarianceStep>(
            MinimumFallbackTrailerYardVarianceAdjuster(47.3),
            RoundingFallbackTrailerYardVarianceAdjuster(0.25),
            LaneFallbackTrailerYardVarianceAdjuster(
                mutableMapOf<String, Double>("west" to 1.16, "south" to 1.11, "central" to 0.7),
            ),
            FallbackTrailerYardVarianceVersionOffsetAdjuster(1.96),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 69572.3,
    )
}
