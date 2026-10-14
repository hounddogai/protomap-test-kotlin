package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict violation policy calculation. */
interface StrictViolationPolicyFactor {
    fun describe(): String
    fun adjust(item: StrictViolationPolicy, value: Double): Double
}

data class CapStrictViolationPolicyAdjuster(var cap: Double) : StrictViolationPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictViolationPolicy, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictViolationPolicyAdjuster(var step: Double) : StrictViolationPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictViolationPolicy, value: Double): Double = roundTo(value, step)
}

data class TieredStrictViolationPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictViolationPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictViolationPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict violation policy steps in order and keeps a trace of each. */
data class StrictViolationPolicyEngine(
    var steps: MutableList<StrictViolationPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictViolationPolicy): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictViolationPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictViolationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictViolationPolicyEngine(): StrictViolationPolicyEngine {
    return StrictViolationPolicyEngine(
        steps = mutableListOf<StrictViolationPolicyFactor>(
            CapStrictViolationPolicyAdjuster(9023.0),
            RoundingStrictViolationPolicyAdjuster(0.1),
            TieredStrictViolationPolicyAdjuster(
                mutableListOf<Double>(1651.0, 4703.0, 6584.0, 7210.0),
                mutableListOf<Double>(0.88, 0.97, 1.08, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 3064.2,
    )
}
