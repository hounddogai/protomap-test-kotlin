package com.acme.transport.linehaul.capacity.capacitymilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped capacity policy calculation. */
interface CappedCapacityPolicyAdjuster {
    fun describe(): String
    fun adjust(item: CappedCapacityPolicy, value: Double): Double
}

data class CappedCapacityPolicyDistanceKmOffsetAdjuster(var weight: Double) : CappedCapacityPolicyAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: CappedCapacityPolicy, value: Double): Double = value + item.distanceKm * weight
}

data class PercentCappedCapacityPolicyAdjuster(var percent: Double) : CappedCapacityPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCapacityPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedCapacityPolicyAdjuster(var step: Double) : CappedCapacityPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCapacityPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedCapacityPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCapacityPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCapacityPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped capacity policy steps in order and keeps a trace of each. */
data class CappedCapacityPolicyEngine(
    var steps: MutableList<CappedCapacityPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedCapacityPolicy): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCapacityPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCapacityPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedCapacityPolicyEngine(flags: Registry): CappedCapacityPolicyEngine {
    return CappedCapacityPolicyEngine(
        steps = mutableListOf<CappedCapacityPolicyAdjuster>(
            CappedCapacityPolicyDistanceKmOffsetAdjuster(1.56),
            PercentCappedCapacityPolicyAdjuster(-12.1),
            RoundingCappedCapacityPolicyAdjuster(0.1),
            FlaggedCappedCapacityPolicyAdjuster(flags, "capped-capacity-policy-default", -4.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 23922.0,
    )
}
