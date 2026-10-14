package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling attempt tier calculation. */
interface RollingAttemptTierAdjuster {
    fun describe(): String
    fun adjust(item: RollingAttemptTier, value: Double): Double
}

data class TieredRollingAttemptTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingAttemptTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingAttemptTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRollingAttemptTierAdjuster(var percent: Double) : RollingAttemptTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingAttemptTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingAttemptTierAdjuster(var step: Double) : RollingAttemptTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAttemptTier, value: Double): Double = roundTo(value, step)
}

data class CapRollingAttemptTierAdjuster(var cap: Double) : RollingAttemptTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingAttemptTier, value: Double): Double = minOf(value, cap)
}

data class RollingAttemptTierAmountOffsetAdjuster(var weight: Double) : RollingAttemptTierAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RollingAttemptTier, value: Double): Double = value + item.amount * weight
}

/** Runs the rolling attempt tier steps in order and keeps a trace of each. */
data class RollingAttemptTierEngine(
    var steps: MutableList<RollingAttemptTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingAttemptTier): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingAttemptTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingAttemptTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingAttemptTierEngine(): RollingAttemptTierEngine {
    return RollingAttemptTierEngine(
        steps = mutableListOf<RollingAttemptTierAdjuster>(
            TieredRollingAttemptTierAdjuster(
                mutableListOf<Double>(767.0, 1112.0, 5101.0),
                mutableListOf<Double>(1.15, 0.95, 1.12),
            ),
            PercentRollingAttemptTierAdjuster(13.8),
            RoundingRollingAttemptTierAdjuster(0.5),
            CapRollingAttemptTierAdjuster(25278.0),
            RollingAttemptTierAmountOffsetAdjuster(1.24),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 68704.4,
    )
}
