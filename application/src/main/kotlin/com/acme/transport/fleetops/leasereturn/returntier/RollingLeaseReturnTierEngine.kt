package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling lease return tier calculation. */
interface RollingLeaseReturnTierFactor {
    fun describe(): String
    fun adjust(item: RollingLeaseReturnTier, value: Double): Double
}

data class MinimumRollingLeaseReturnTierAdjuster(var minimum: Double) : RollingLeaseReturnTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingLeaseReturnTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingLeaseReturnTierScoreOffsetAdjuster(var weight: Double) : RollingLeaseReturnTierFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RollingLeaseReturnTier, value: Double): Double = value + item.score * weight
}

data class RoundingRollingLeaseReturnTierAdjuster(var step: Double) : RollingLeaseReturnTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingLeaseReturnTier, value: Double): Double = roundTo(value, step)
}

data class TieredRollingLeaseReturnTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingLeaseReturnTierFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingLeaseReturnTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling lease return tier steps in order and keeps a trace of each. */
data class RollingLeaseReturnTierEngine(
    var steps: MutableList<RollingLeaseReturnTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingLeaseReturnTier): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingLeaseReturnTierFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingLeaseReturnTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingLeaseReturnTierEngine(): RollingLeaseReturnTierEngine {
    return RollingLeaseReturnTierEngine(
        steps = mutableListOf<RollingLeaseReturnTierFactor>(
            MinimumRollingLeaseReturnTierAdjuster(35.3),
            RollingLeaseReturnTierScoreOffsetAdjuster(1.63),
            RoundingRollingLeaseReturnTierAdjuster(0.05),
            TieredRollingLeaseReturnTierAdjuster(
                mutableListOf<Double>(192.0, 799.0, 9854.0),
                mutableListOf<Double>(1.15, 1.05, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 1189.0,
    )
}
