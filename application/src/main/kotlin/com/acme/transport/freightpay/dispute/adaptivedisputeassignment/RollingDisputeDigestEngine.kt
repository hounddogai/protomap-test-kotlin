package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling dispute digest calculation. */
interface RollingDisputeDigestStep {
    fun describe(): String
    fun adjust(item: RollingDisputeDigest, value: Double): Double
}

data class FlaggedRollingDisputeDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDisputeDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDisputeDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingDisputeDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDisputeDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDisputeDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingDisputeDigestAdjuster(var minimum: Double) : RollingDisputeDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDisputeDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling dispute digest steps in order and keeps a trace of each. */
data class RollingDisputeDigestEngine(
    var steps: MutableList<RollingDisputeDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingDisputeDigest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDisputeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingDisputeDigestEngine(flags: Registry): RollingDisputeDigestEngine {
    return RollingDisputeDigestEngine(
        steps = mutableListOf<RollingDisputeDigestStep>(
            FlaggedRollingDisputeDigestAdjuster(flags, "rolling-dispute-digest-lenient", 9.4),
            TieredRollingDisputeDigestAdjuster(
                mutableListOf<Double>(2016.0, 5844.0, 7003.0),
                mutableListOf<Double>(1.05, 0.99, 0.93),
            ),
            MinimumRollingDisputeDigestAdjuster(9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 52852.1,
    )
}
