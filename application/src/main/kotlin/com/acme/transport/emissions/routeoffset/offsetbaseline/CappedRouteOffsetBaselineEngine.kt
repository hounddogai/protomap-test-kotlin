package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped route offset baseline calculation. */
interface CappedRouteOffsetBaselineModifier {
    fun describe(): String
    fun adjust(item: CappedRouteOffsetBaseline, value: Double): Double
}

data class CappedRouteOffsetBaselineThresholdOffsetAdjuster(
    var weight: Double,
) : CappedRouteOffsetBaselineModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CappedRouteOffsetBaseline, value: Double): Double = value + item.threshold * weight
}

data class RoundingCappedRouteOffsetBaselineAdjuster(var step: Double) : CappedRouteOffsetBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRouteOffsetBaseline, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedRouteOffsetBaselineAdjuster(var minimum: Double) : CappedRouteOffsetBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRouteOffsetBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameCappedRouteOffsetBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedRouteOffsetBaselineModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedRouteOffsetBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped route offset baseline steps in order and keeps a trace of each. */
data class CappedRouteOffsetBaselineEngine(
    var steps: MutableList<CappedRouteOffsetBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedRouteOffsetBaseline): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedRouteOffsetBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedRouteOffsetBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedRouteOffsetBaselineEngine(): CappedRouteOffsetBaselineEngine {
    return CappedRouteOffsetBaselineEngine(
        steps = mutableListOf<CappedRouteOffsetBaselineModifier>(
            CappedRouteOffsetBaselineThresholdOffsetAdjuster(0.18),
            RoundingCappedRouteOffsetBaselineAdjuster(0.25),
            MinimumCappedRouteOffsetBaselineAdjuster(16.8),
            NameCappedRouteOffsetBaselineAdjuster(mutableMapOf<String, Double>("central" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 99539.5,
    )
}
