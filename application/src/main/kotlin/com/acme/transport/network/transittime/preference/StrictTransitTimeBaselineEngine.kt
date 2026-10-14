package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.flags.Registry

/** One step of the strict transit time baseline calculation. */
interface StrictTransitTimeBaselineModifier {
    fun describe(): String
    fun adjust(item: StrictTransitTimeBaseline, value: Double): Double
}

data class PercentStrictTransitTimeBaselineAdjuster(var percent: Double) : StrictTransitTimeBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTransitTimeBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictTransitTimeBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTransitTimeBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTransitTimeBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictTransitTimeBaselineAdjuster(var minimum: Double) : StrictTransitTimeBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTransitTimeBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict transit time baseline steps in order and keeps a trace of each. */
data class StrictTransitTimeBaselineEngine(
    var steps: MutableList<StrictTransitTimeBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictTransitTimeBaseline): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTransitTimeBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTransitTimeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictTransitTimeBaselineEngine(flags: Registry): StrictTransitTimeBaselineEngine {
    return StrictTransitTimeBaselineEngine(
        steps = mutableListOf<StrictTransitTimeBaselineModifier>(
            PercentStrictTransitTimeBaselineAdjuster(6.0),
            FlaggedStrictTransitTimeBaselineAdjuster(flags, "strict-transit-time-baseline-strict", 4.3),
            MinimumStrictTransitTimeBaselineAdjuster(29.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 5745.2,
    )
}
