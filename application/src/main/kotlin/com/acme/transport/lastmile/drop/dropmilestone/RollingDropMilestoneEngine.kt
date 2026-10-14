package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling drop milestone calculation. */
interface RollingDropMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: RollingDropMilestone, value: Double): Double
}

data class LocaleRollingDropMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDropMilestoneAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: RollingDropMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingDropMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDropMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDropMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingDropMilestoneVersionOffsetAdjuster(var weight: Double) : RollingDropMilestoneAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RollingDropMilestone, value: Double): Double = value + item.version.toDouble() * weight
}

data class CapRollingDropMilestoneAdjuster(var cap: Double) : RollingDropMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDropMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingDropMilestoneAdjuster(var minimum: Double) : RollingDropMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDropMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRollingDropMilestoneAdjuster(var step: Double) : RollingDropMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDropMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling drop milestone steps in order and keeps a trace of each. */
data class RollingDropMilestoneEngine(
    var steps: MutableList<RollingDropMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDropMilestone): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDropMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDropMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDropMilestoneEngine(): RollingDropMilestoneEngine {
    return RollingDropMilestoneEngine(
        steps = mutableListOf<RollingDropMilestoneAdjuster>(
            LocaleRollingDropMilestoneAdjuster(mutableMapOf<String, Double>("central" to 0.81)),
            TieredRollingDropMilestoneAdjuster(
                mutableListOf<Double>(1224.0, 2509.0, 3018.0, 7625.0),
                mutableListOf<Double>(0.9, 1.02, 1.11, 0.88),
            ),
            RollingDropMilestoneVersionOffsetAdjuster(0.28),
            CapRollingDropMilestoneAdjuster(22785.0),
            MinimumRollingDropMilestoneAdjuster(34.8),
            RoundingRollingDropMilestoneAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 14187.6,
    )
}
