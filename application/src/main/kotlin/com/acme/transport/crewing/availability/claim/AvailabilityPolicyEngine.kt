package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the availability policy calculation. */
interface AvailabilityPolicyFactor {
    fun describe(): String
    fun adjust(item: AvailabilityPolicy, value: Double): Double
}

data class SegmentAvailabilityPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityPolicyFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: AvailabilityPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAvailabilityPolicyAdjuster(var minimum: Double) : AvailabilityPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAvailabilityPolicyAdjuster(var step: Double) : AvailabilityPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityPolicy, value: Double): Double = roundTo(value, step)
}

data class CapAvailabilityPolicyAdjuster(var cap: Double) : AvailabilityPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityPolicy, value: Double): Double = minOf(value, cap)
}

data class AvailabilityPolicySequenceOffsetAdjuster(var weight: Double) : AvailabilityPolicyFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: AvailabilityPolicy, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class FlaggedAvailabilityPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the availability policy steps in order and keeps a trace of each. */
data class AvailabilityPolicyEngine(
    var steps: MutableList<AvailabilityPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AvailabilityPolicy): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAvailabilityPolicyEngine(flags: Registry): AvailabilityPolicyEngine {
    return AvailabilityPolicyEngine(
        steps = mutableListOf<AvailabilityPolicyFactor>(
            SegmentAvailabilityPolicyAdjuster(mutableMapOf<String, Double>("north" to 1.06, "retail" to 1.0)),
            MinimumAvailabilityPolicyAdjuster(18.3),
            RoundingAvailabilityPolicyAdjuster(1.0),
            CapAvailabilityPolicyAdjuster(9917.0),
            AvailabilityPolicySequenceOffsetAdjuster(1.67),
            FlaggedAvailabilityPolicyAdjuster(flags, "availability-policy-fallback", 5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 34507.8,
    )
}
