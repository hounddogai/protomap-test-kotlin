package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback inspection policy calculation. */
interface FallbackInspectionPolicyFactor {
    fun describe(): String
    fun adjust(item: FallbackInspectionPolicy, value: Double): Double
}

data class FlaggedFallbackInspectionPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackInspectionPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackInspectionPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FallbackInspectionPolicySequenceOffsetAdjuster(var weight: Double) : FallbackInspectionPolicyFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: FallbackInspectionPolicy, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class ChannelFallbackInspectionPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackInspectionPolicyFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: FallbackInspectionPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFallbackInspectionPolicyAdjuster(var step: Double) : FallbackInspectionPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackInspectionPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback inspection policy steps in order and keeps a trace of each. */
data class FallbackInspectionPolicyEngine(
    var steps: MutableList<FallbackInspectionPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackInspectionPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackInspectionPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackInspectionPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackInspectionPolicyEngine(flags: Registry): FallbackInspectionPolicyEngine {
    return FallbackInspectionPolicyEngine(
        steps = mutableListOf<FallbackInspectionPolicyFactor>(
            FlaggedFallbackInspectionPolicyAdjuster(flags, "fallback-inspection-policy-seasonal", 2.0),
            FallbackInspectionPolicySequenceOffsetAdjuster(1.6),
            ChannelFallbackInspectionPolicyAdjuster(mutableMapOf<String, Double>("central" to 1.25)),
            RoundingFallbackInspectionPolicyAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 80163.1,
    )
}
