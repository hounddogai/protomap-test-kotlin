package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling coaching tier calculation. */
interface RollingCoachingTierAdjuster {
    fun describe(): String
    fun adjust(item: RollingCoachingTier, value: Double): Double
}

data class MinimumRollingCoachingTierAdjuster(var minimum: Double) : RollingCoachingTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingCoachingTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingCoachingTierStartsAtOffsetAdjuster(var weight: Double) : RollingCoachingTierAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RollingCoachingTier, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class RoundingRollingCoachingTierAdjuster(var step: Double) : RollingCoachingTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCoachingTier, value: Double): Double = roundTo(value, step)
}

data class CapRollingCoachingTierAdjuster(var cap: Double) : RollingCoachingTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCoachingTier, value: Double): Double = minOf(value, cap)
}

data class TieredRollingCoachingTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCoachingTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCoachingTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRollingCoachingTierAdjuster(var percent: Double) : RollingCoachingTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingCoachingTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling coaching tier steps in order and keeps a trace of each. */
data class RollingCoachingTierEngine(
    var steps: MutableList<RollingCoachingTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingCoachingTier): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingCoachingTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingCoachingTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingCoachingTierEngine(): RollingCoachingTierEngine {
    return RollingCoachingTierEngine(
        steps = mutableListOf<RollingCoachingTierAdjuster>(
            MinimumRollingCoachingTierAdjuster(34.7),
            RollingCoachingTierStartsAtOffsetAdjuster(1.81),
            RoundingRollingCoachingTierAdjuster(0.1),
            CapRollingCoachingTierAdjuster(20494.0),
            TieredRollingCoachingTierAdjuster(
                mutableListOf<Double>(3374.0, 3402.0, 3633.0, 6701.0),
                mutableListOf<Double>(0.98, 1.08, 1.18, 1.12),
            ),
            PercentRollingCoachingTierAdjuster(9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 67486.7,
    )
}
