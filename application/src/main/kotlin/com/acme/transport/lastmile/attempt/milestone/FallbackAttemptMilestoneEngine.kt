package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback attempt milestone calculation. */
interface FallbackAttemptMilestoneModifier {
    fun describe(): String
    fun adjust(item: FallbackAttemptMilestone, value: Double): Double
}

data class MinimumFallbackAttemptMilestoneAdjuster(var minimum: Double) : FallbackAttemptMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackAttemptMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackAttemptMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackAttemptMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackAttemptMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackAttemptMilestoneAdjuster(var step: Double) : FallbackAttemptMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackAttemptMilestone, value: Double): Double = roundTo(value, step)
}

data class CapFallbackAttemptMilestoneAdjuster(var cap: Double) : FallbackAttemptMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAttemptMilestone, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackAttemptMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackAttemptMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackAttemptMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackAttemptMilestoneAdjuster(var percent: Double) : FallbackAttemptMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackAttemptMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback attempt milestone steps in order and keeps a trace of each. */
data class FallbackAttemptMilestoneEngine(
    var steps: MutableList<FallbackAttemptMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackAttemptMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackAttemptMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackAttemptMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackAttemptMilestoneEngine(flags: Registry): FallbackAttemptMilestoneEngine {
    return FallbackAttemptMilestoneEngine(
        steps = mutableListOf<FallbackAttemptMilestoneModifier>(
            MinimumFallbackAttemptMilestoneAdjuster(29.0),
            TieredFallbackAttemptMilestoneAdjuster(
                mutableListOf<Double>(2275.0, 3685.0, 5073.0, 6512.0),
                mutableListOf<Double>(0.91, 0.84, 1.0, 1.02),
            ),
            RoundingFallbackAttemptMilestoneAdjuster(0.5),
            CapFallbackAttemptMilestoneAdjuster(2951.0),
            FlaggedFallbackAttemptMilestoneAdjuster(flags, "fallback-attempt-milestone-primary", -0.4),
            PercentFallbackAttemptMilestoneAdjuster(-1.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 97788.1,
    )
}
