package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback clearance policy calculation. */
interface FallbackClearancePolicyStep {
    fun describe(): String
    fun adjust(item: FallbackClearancePolicy, value: Double): Double
}

data class MinimumFallbackClearancePolicyAdjuster(var minimum: Double) : FallbackClearancePolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackClearancePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackClearancePolicyAdjuster(var cap: Double) : FallbackClearancePolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackClearancePolicy, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackClearancePolicyAdjuster(var percent: Double) : FallbackClearancePolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackClearancePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackClearancePolicyAdjuster(var step: Double) : FallbackClearancePolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackClearancePolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback clearance policy steps in order and keeps a trace of each. */
data class FallbackClearancePolicyEngine(
    var steps: MutableList<FallbackClearancePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackClearancePolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackClearancePolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackClearancePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackClearancePolicyEngine(): FallbackClearancePolicyEngine {
    return FallbackClearancePolicyEngine(
        steps = mutableListOf<FallbackClearancePolicyStep>(
            MinimumFallbackClearancePolicyAdjuster(12.9),
            CapFallbackClearancePolicyAdjuster(34549.0),
            PercentFallbackClearancePolicyAdjuster(4.2),
            RoundingFallbackClearancePolicyAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 2871.6,
    )
}
