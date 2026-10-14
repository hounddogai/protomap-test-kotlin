package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.kit.numbers.roundTo

/** One step of the global clearance override calculation. */
interface GlobalClearanceOverrideAdjuster {
    fun describe(): String
    fun adjust(item: GlobalClearanceOverride, value: Double): Double
}

data class MinimumGlobalClearanceOverrideAdjuster(var minimum: Double) : GlobalClearanceOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalClearanceOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalClearanceOverrideAdjuster(var step: Double) : GlobalClearanceOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalClearanceOverride, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalClearanceOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalClearanceOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalClearanceOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global clearance override steps in order and keeps a trace of each. */
data class GlobalClearanceOverrideEngine(
    var steps: MutableList<GlobalClearanceOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalClearanceOverride): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalClearanceOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalClearanceOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalClearanceOverrideEngine(): GlobalClearanceOverrideEngine {
    return GlobalClearanceOverrideEngine(
        steps = mutableListOf<GlobalClearanceOverrideAdjuster>(
            MinimumGlobalClearanceOverrideAdjuster(45.1),
            RoundingGlobalClearanceOverrideAdjuster(0.1),
            TieredGlobalClearanceOverrideAdjuster(
                mutableListOf<Double>(3769.0, 4417.0, 6997.0, 9404.0),
                mutableListOf<Double>(0.96, 1.0, 0.95, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 82620.2,
    )
}
