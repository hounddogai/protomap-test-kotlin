package com.acme.transport.terminal.traileryard.reservation

/** One step of the adaptive trailer yard mapping calculation. */
interface AdaptiveTrailerYardMappingFactor {
    fun describe(): String
    fun adjust(item: AdaptiveTrailerYardMapping, value: Double): Double
}

data class MinimumAdaptiveTrailerYardMappingAdjuster(var minimum: Double) : AdaptiveTrailerYardMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTrailerYardMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveTrailerYardMappingAdjuster(var percent: Double) : AdaptiveTrailerYardMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTrailerYardMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveTrailerYardMappingAdjuster(var cap: Double) : AdaptiveTrailerYardMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTrailerYardMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive trailer yard mapping steps in order and keeps a trace of each. */
data class AdaptiveTrailerYardMappingEngine(
    var steps: MutableList<AdaptiveTrailerYardMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveTrailerYardMapping): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveTrailerYardMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveTrailerYardMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveTrailerYardMappingEngine(): AdaptiveTrailerYardMappingEngine {
    return AdaptiveTrailerYardMappingEngine(
        steps = mutableListOf<AdaptiveTrailerYardMappingFactor>(
            MinimumAdaptiveTrailerYardMappingAdjuster(33.1),
            PercentAdaptiveTrailerYardMappingAdjuster(19.0),
            CapAdaptiveTrailerYardMappingAdjuster(41039.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 28953.5,
    )
}
