package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tire milestone calculation. */
interface TireMilestoneStep {
    fun describe(): String
    fun adjust(item: TireMilestone, value: Double): Double
}

data class RoundingTireMilestoneAdjuster(var step: Double) : TireMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireMilestone, value: Double): Double = roundTo(value, step)
}

data class TieredTireMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTireMilestoneAdjuster(var minimum: Double) : TireMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTireMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TireMilestoneCreatedAtOffsetAdjuster(var weight: Double) : TireMilestoneStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: TireMilestone, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the tire milestone steps in order and keeps a trace of each. */
data class TireMilestoneEngine(
    var steps: MutableList<TireMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireMilestone): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<TireMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireMilestoneEngine(flags: Registry): TireMilestoneEngine {
    return TireMilestoneEngine(
        steps = mutableListOf<TireMilestoneStep>(
            RoundingTireMilestoneAdjuster(0.1),
            TieredTireMilestoneAdjuster(mutableListOf<Double>(578.0, 7984.0), mutableListOf<Double>(1.09, 0.8)),
            MinimumTireMilestoneAdjuster(20.6),
            FlaggedTireMilestoneAdjuster(flags, "tire-milestone-regional", 2.1),
            TireMilestoneCreatedAtOffsetAdjuster(1.58),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 54649.5,
    )
}
