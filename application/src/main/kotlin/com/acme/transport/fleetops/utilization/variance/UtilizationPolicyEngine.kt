package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the utilization policy calculation. */
interface UtilizationPolicyFactor {
    fun describe(): String
    fun adjust(item: UtilizationPolicy, value: Double): Double
}

data class FlaggedUtilizationPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentUtilizationPolicyAdjuster(var percent: Double) : UtilizationPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumUtilizationPolicyAdjuster(var minimum: Double) : UtilizationPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapUtilizationPolicyAdjuster(var cap: Double) : UtilizationPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationPolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the utilization policy steps in order and keeps a trace of each. */
data class UtilizationPolicyEngine(
    var steps: MutableList<UtilizationPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationPolicy): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationPolicyEngine(flags: Registry): UtilizationPolicyEngine {
    return UtilizationPolicyEngine(
        steps = mutableListOf<UtilizationPolicyFactor>(
            FlaggedUtilizationPolicyAdjuster(flags, "utilization-policy-strict", -2.8),
            PercentUtilizationPolicyAdjuster(-9.4),
            MinimumUtilizationPolicyAdjuster(3.7),
            CapUtilizationPolicyAdjuster(3268.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 16454.0,
    )
}
