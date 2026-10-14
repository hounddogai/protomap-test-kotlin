package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling node tier calculation. */
interface RollingNodeTierAdjuster {
    fun describe(): String
    fun adjust(item: RollingNodeTier, value: Double): Double
}

data class CapRollingNodeTierAdjuster(var cap: Double) : RollingNodeTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingNodeTier, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingNodeTierAdjuster(var step: Double) : RollingNodeTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingNodeTier, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingNodeTierAdjuster(var minimum: Double) : RollingNodeTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingNodeTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingNodeTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingNodeTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingNodeTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentRollingNodeTierAdjuster(var factors: MutableMap<String, Double>) : RollingNodeTierAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: RollingNodeTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingNodeTierAttemptsOffsetAdjuster(var weight: Double) : RollingNodeTierAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RollingNodeTier, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the rolling node tier steps in order and keeps a trace of each. */
data class RollingNodeTierEngine(
    var steps: MutableList<RollingNodeTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingNodeTier): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingNodeTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingNodeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingNodeTierEngine(flags: Registry): RollingNodeTierEngine {
    return RollingNodeTierEngine(
        steps = mutableListOf<RollingNodeTierAdjuster>(
            CapRollingNodeTierAdjuster(9688.0),
            RoundingRollingNodeTierAdjuster(0.05),
            MinimumRollingNodeTierAdjuster(6.4),
            FlaggedRollingNodeTierAdjuster(flags, "rolling-node-tier-rolling", -0.3),
            SegmentRollingNodeTierAdjuster(mutableMapOf<String, Double>("south" to 1.24)),
            RollingNodeTierAttemptsOffsetAdjuster(0.49),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 11352.1,
    )
}
