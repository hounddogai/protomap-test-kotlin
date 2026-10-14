package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling dispute adjustment calculation. */
interface RollingDisputeAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: RollingDisputeAdjustment, value: Double): Double
}

data class RoundingRollingDisputeAdjustmentAdjuster(var step: Double) : RollingDisputeAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDisputeAdjustment, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingDisputeAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDisputeAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDisputeAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRollingDisputeAdjustmentAdjuster(var minimum: Double) : RollingDisputeAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDisputeAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingDisputeAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDisputeAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDisputeAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelRollingDisputeAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDisputeAdjustmentAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: RollingDisputeAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingDisputeAdjustmentCostOffsetAdjuster(var weight: Double) : RollingDisputeAdjustmentAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: RollingDisputeAdjustment, value: Double): Double = value + item.cost * weight
}

/** Runs the rolling dispute adjustment steps in order and keeps a trace of each. */
data class RollingDisputeAdjustmentEngine(
    var steps: MutableList<RollingDisputeAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingDisputeAdjustment): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDisputeAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDisputeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingDisputeAdjustmentEngine(flags: Registry): RollingDisputeAdjustmentEngine {
    return RollingDisputeAdjustmentEngine(
        steps = mutableListOf<RollingDisputeAdjustmentAdjuster>(
            RoundingRollingDisputeAdjustmentAdjuster(1.0),
            FlaggedRollingDisputeAdjustmentAdjuster(flags, "rolling-dispute-adjustment-capped", 1.9),
            MinimumRollingDisputeAdjustmentAdjuster(42.9),
            TieredRollingDisputeAdjustmentAdjuster(
                mutableListOf<Double>(3596.0, 7194.0),
                mutableListOf<Double>(1.02, 1.18),
            ),
            ChannelRollingDisputeAdjustmentAdjuster(mutableMapOf<String, Double>("online" to 1.33)),
            RollingDisputeAdjustmentCostOffsetAdjuster(0.74),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 84162.5,
    )
}
