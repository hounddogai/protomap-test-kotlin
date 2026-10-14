package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending idling milestone calculation. */
interface PendingIdlingMilestoneFactor {
    fun describe(): String
    fun adjust(item: PendingIdlingMilestone, value: Double): Double
}

data class FlaggedPendingIdlingMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingIdlingMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingIdlingMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingIdlingMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingIdlingMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingIdlingMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryPendingIdlingMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingIdlingMilestoneFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: PendingIdlingMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingIdlingMilestoneAdjuster(var step: Double) : PendingIdlingMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingIdlingMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingIdlingMilestoneAdjuster(var minimum: Double) : PendingIdlingMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingIdlingMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending idling milestone steps in order and keeps a trace of each. */
data class PendingIdlingMilestoneEngine(
    var steps: MutableList<PendingIdlingMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingIdlingMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingIdlingMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingIdlingMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingIdlingMilestoneEngine(flags: Registry): PendingIdlingMilestoneEngine {
    return PendingIdlingMilestoneEngine(
        steps = mutableListOf<PendingIdlingMilestoneFactor>(
            FlaggedPendingIdlingMilestoneAdjuster(flags, "pending-idling-milestone-batched", 8.3),
            TieredPendingIdlingMilestoneAdjuster(
                mutableListOf<Double>(718.0, 6960.0, 7285.0),
                mutableListOf<Double>(1.04, 1.06, 1.08),
            ),
            CountryPendingIdlingMilestoneAdjuster(
                mutableMapOf<String, Double>("south" to 0.81, "retail" to 0.84, "central" to 0.75),
            ),
            RoundingPendingIdlingMilestoneAdjuster(0.1),
            MinimumPendingIdlingMilestoneAdjuster(35.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 18921.9,
    )
}
