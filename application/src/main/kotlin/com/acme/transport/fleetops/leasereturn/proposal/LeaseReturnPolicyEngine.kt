package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the lease return policy calculation. */
interface LeaseReturnPolicyStep {
    fun describe(): String
    fun adjust(item: LeaseReturnPolicy, value: Double): Double
}

data class TieredLeaseReturnPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LeaseReturnPolicyCreatedAtOffsetAdjuster(var weight: Double) : LeaseReturnPolicyStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: LeaseReturnPolicy, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class FlaggedLeaseReturnPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lease return policy steps in order and keeps a trace of each. */
data class LeaseReturnPolicyEngine(
    var steps: MutableList<LeaseReturnPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnPolicyEngine(flags: Registry): LeaseReturnPolicyEngine {
    return LeaseReturnPolicyEngine(
        steps = mutableListOf<LeaseReturnPolicyStep>(
            TieredLeaseReturnPolicyAdjuster(
                mutableListOf<Double>(4946.0, 6623.0, 7769.0, 9094.0),
                mutableListOf<Double>(0.82, 0.89, 1.07, 1.05),
            ),
            LeaseReturnPolicyCreatedAtOffsetAdjuster(0.62),
            FlaggedLeaseReturnPolicyAdjuster(flags, "lease-return-policy-regional", 9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 40338.0,
    )
}
