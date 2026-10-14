package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.flags.Registry

/** One step of the regional lease return policy calculation. */
interface RegionalLeaseReturnPolicyStep {
    fun describe(): String
    fun adjust(item: RegionalLeaseReturnPolicy, value: Double): Double
}

data class FlaggedRegionalLeaseReturnPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalLeaseReturnPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalLeaseReturnPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalLeaseReturnPolicyAdjuster(var cap: Double) : RegionalLeaseReturnPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalLeaseReturnPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalLeaseReturnPolicyAdjuster(var minimum: Double) : RegionalLeaseReturnPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalLeaseReturnPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalLeaseReturnPolicyScoreOffsetAdjuster(var weight: Double) : RegionalLeaseReturnPolicyStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RegionalLeaseReturnPolicy, value: Double): Double = value + item.score * weight
}

data class SourceRegionalLeaseReturnPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalLeaseReturnPolicyStep {
    override fun describe(): String = "by source"

    override fun adjust(item: RegionalLeaseReturnPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalLeaseReturnPolicyAdjuster(var percent: Double) : RegionalLeaseReturnPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalLeaseReturnPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional lease return policy steps in order and keeps a trace of each. */
data class RegionalLeaseReturnPolicyEngine(
    var steps: MutableList<RegionalLeaseReturnPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalLeaseReturnPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalLeaseReturnPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalLeaseReturnPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalLeaseReturnPolicyEngine(flags: Registry): RegionalLeaseReturnPolicyEngine {
    return RegionalLeaseReturnPolicyEngine(
        steps = mutableListOf<RegionalLeaseReturnPolicyStep>(
            FlaggedRegionalLeaseReturnPolicyAdjuster(flags, "regional-lease-return-policy-seasonal", -7.4),
            CapRegionalLeaseReturnPolicyAdjuster(45119.0),
            MinimumRegionalLeaseReturnPolicyAdjuster(48.8),
            RegionalLeaseReturnPolicyScoreOffsetAdjuster(1.79),
            SourceRegionalLeaseReturnPolicyAdjuster(
                mutableMapOf<String, Double>("west" to 0.94, "central" to 0.9, "north" to 1.17),
            ),
            PercentRegionalLeaseReturnPolicyAdjuster(-2.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 72989.2,
    )
}
