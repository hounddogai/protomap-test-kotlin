package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the violation policy calculation. */
interface ViolationPolicyAdjuster {
    fun describe(): String
    fun adjust(item: ViolationPolicy, value: Double): Double
}

data class TitleViolationPolicyAdjuster(var factors: MutableMap<String, Double>) : ViolationPolicyAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ViolationPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingViolationPolicyAdjuster(var step: Double) : ViolationPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationPolicy, value: Double): Double = roundTo(value, step)
}

data class MinimumViolationPolicyAdjuster(var minimum: Double) : ViolationPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapViolationPolicyAdjuster(var cap: Double) : ViolationPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationPolicy, value: Double): Double = minOf(value, cap)
}

data class TieredViolationPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentViolationPolicyAdjuster(var percent: Double) : ViolationPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the violation policy steps in order and keeps a trace of each. */
data class ViolationPolicyEngine(
    var steps: MutableList<ViolationPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ViolationPolicy): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultViolationPolicyEngine(): ViolationPolicyEngine {
    return ViolationPolicyEngine(
        steps = mutableListOf<ViolationPolicyAdjuster>(
            TitleViolationPolicyAdjuster(
                mutableMapOf<String, Double>("east" to 0.92, "central" to 0.84, "retail" to 0.73),
            ),
            RoundingViolationPolicyAdjuster(1.0),
            MinimumViolationPolicyAdjuster(10.0),
            CapViolationPolicyAdjuster(48296.0),
            TieredViolationPolicyAdjuster(
                mutableListOf<Double>(218.0, 1103.0, 1259.0),
                mutableListOf<Double>(1.16, 1.03, 0.98),
            ),
            PercentViolationPolicyAdjuster(-1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 36367.8,
    )
}
