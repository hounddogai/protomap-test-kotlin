package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global coaching baseline calculation. */
interface GlobalCoachingBaselineStep {
    fun describe(): String
    fun adjust(item: GlobalCoachingBaseline, value: Double): Double
}

data class TieredGlobalCoachingBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCoachingBaselineStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCoachingBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingGlobalCoachingBaselineAdjuster(var step: Double) : GlobalCoachingBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCoachingBaseline, value: Double): Double = roundTo(value, step)
}

data class CapGlobalCoachingBaselineAdjuster(var cap: Double) : GlobalCoachingBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCoachingBaseline, value: Double): Double = minOf(value, cap)
}

data class PercentGlobalCoachingBaselineAdjuster(var percent: Double) : GlobalCoachingBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCoachingBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalCoachingBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCoachingBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCoachingBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalCoachingBaselineAdjuster(var minimum: Double) : GlobalCoachingBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCoachingBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global coaching baseline steps in order and keeps a trace of each. */
data class GlobalCoachingBaselineEngine(
    var steps: MutableList<GlobalCoachingBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalCoachingBaseline): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCoachingBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCoachingBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalCoachingBaselineEngine(flags: Registry): GlobalCoachingBaselineEngine {
    return GlobalCoachingBaselineEngine(
        steps = mutableListOf<GlobalCoachingBaselineStep>(
            TieredGlobalCoachingBaselineAdjuster(
                mutableListOf<Double>(3438.0, 5387.0, 8607.0, 9320.0),
                mutableListOf<Double>(0.93, 0.86, 1.19, 0.95),
            ),
            RoundingGlobalCoachingBaselineAdjuster(0.05),
            CapGlobalCoachingBaselineAdjuster(18449.0),
            PercentGlobalCoachingBaselineAdjuster(10.6),
            FlaggedGlobalCoachingBaselineAdjuster(flags, "global-coaching-baseline-default", -4.3),
            MinimumGlobalCoachingBaselineAdjuster(29.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 76551.7,
    )
}
