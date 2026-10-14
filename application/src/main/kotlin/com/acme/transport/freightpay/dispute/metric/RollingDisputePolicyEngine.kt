package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling dispute policy calculation. */
interface RollingDisputePolicyAdjuster {
    fun describe(): String
    fun adjust(item: RollingDisputePolicy, value: Double): Double
}

data class PercentRollingDisputePolicyAdjuster(var percent: Double) : RollingDisputePolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDisputePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingDisputePolicyAdjuster(var cap: Double) : RollingDisputePolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDisputePolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingDisputePolicyAdjuster(var minimum: Double) : RollingDisputePolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDisputePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingDisputePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDisputePolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDisputePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingDisputePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDisputePolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDisputePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling dispute policy steps in order and keeps a trace of each. */
data class RollingDisputePolicyEngine(
    var steps: MutableList<RollingDisputePolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDisputePolicy): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDisputePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDisputePolicyEngine(flags: Registry): RollingDisputePolicyEngine {
    return RollingDisputePolicyEngine(
        steps = mutableListOf<RollingDisputePolicyAdjuster>(
            PercentRollingDisputePolicyAdjuster(-7.0),
            CapRollingDisputePolicyAdjuster(23643.0),
            MinimumRollingDisputePolicyAdjuster(40.7),
            FlaggedRollingDisputePolicyAdjuster(flags, "rolling-dispute-policy-regional", 9.0),
            TieredRollingDisputePolicyAdjuster(
                mutableListOf<Double>(1634.0, 7178.0),
                mutableListOf<Double>(1.15, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 15238.3,
    )
}
