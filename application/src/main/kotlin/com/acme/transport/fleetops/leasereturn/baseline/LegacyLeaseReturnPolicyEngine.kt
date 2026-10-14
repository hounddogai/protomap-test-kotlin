package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy lease return policy calculation. */
interface LegacyLeaseReturnPolicyFactor {
    fun describe(): String
    fun adjust(item: LegacyLeaseReturnPolicy, value: Double): Double
}

data class MinimumLegacyLeaseReturnPolicyAdjuster(var minimum: Double) : LegacyLeaseReturnPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyLeaseReturnPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLegacyLeaseReturnPolicyAdjuster(var cap: Double) : LegacyLeaseReturnPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLeaseReturnPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyLeaseReturnPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLeaseReturnPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLeaseReturnPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyLeaseReturnPolicyAdjuster(var percent: Double) : LegacyLeaseReturnPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyLeaseReturnPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy lease return policy steps in order and keeps a trace of each. */
data class LegacyLeaseReturnPolicyEngine(
    var steps: MutableList<LegacyLeaseReturnPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyLeaseReturnPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyLeaseReturnPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyLeaseReturnPolicyEngine(flags: Registry): LegacyLeaseReturnPolicyEngine {
    return LegacyLeaseReturnPolicyEngine(
        steps = mutableListOf<LegacyLeaseReturnPolicyFactor>(
            MinimumLegacyLeaseReturnPolicyAdjuster(48.3),
            CapLegacyLeaseReturnPolicyAdjuster(17277.0),
            FlaggedLegacyLeaseReturnPolicyAdjuster(flags, "legacy-lease-return-policy-legacy", -7.2),
            PercentLegacyLeaseReturnPolicyAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 7802.7,
    )
}
