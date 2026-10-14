package com.acme.transport.crewing.restbreak.assignment

/** One step of the rest break override calculation. */
interface RestBreakOverrideModifier {
    fun describe(): String
    fun adjust(item: RestBreakOverride, value: Double): Double
}

data class MinimumRestBreakOverrideAdjuster(var minimum: Double) : RestBreakOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRestBreakOverrideAdjuster(var percent: Double) : RestBreakOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRestBreakOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rest break override steps in order and keeps a trace of each. */
data class RestBreakOverrideEngine(
    var steps: MutableList<RestBreakOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RestBreakOverride): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRestBreakOverrideEngine(): RestBreakOverrideEngine {
    return RestBreakOverrideEngine(
        steps = mutableListOf<RestBreakOverrideModifier>(
            MinimumRestBreakOverrideAdjuster(26.8),
            PercentRestBreakOverrideAdjuster(7.1),
            TieredRestBreakOverrideAdjuster(
                mutableListOf<Double>(6523.0, 7623.0),
                mutableListOf<Double>(1.0, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 80223.8,
    )
}
