package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive utilization policy calculation. */
interface AdaptiveUtilizationPolicyStep {
    fun describe(): String
    fun adjust(item: AdaptiveUtilizationPolicy, value: Double): Double
}

data class CountryAdaptiveUtilizationPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveUtilizationPolicyStep {
    override fun describe(): String = "by country"

    override fun adjust(item: AdaptiveUtilizationPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveUtilizationPolicyAdjuster(var step: Double) : AdaptiveUtilizationPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveUtilizationPolicy, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveUtilizationPolicyAdjuster(var minimum: Double) : AdaptiveUtilizationPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveUtilizationPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive utilization policy steps in order and keeps a trace of each. */
data class AdaptiveUtilizationPolicyEngine(
    var steps: MutableList<AdaptiveUtilizationPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveUtilizationPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveUtilizationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveUtilizationPolicyEngine(): AdaptiveUtilizationPolicyEngine {
    return AdaptiveUtilizationPolicyEngine(
        steps = mutableListOf<AdaptiveUtilizationPolicyStep>(
            CountryAdaptiveUtilizationPolicyAdjuster(
                mutableMapOf<String, Double>("retail" to 1.38, "east" to 0.83, "online" to 1.21),
            ),
            RoundingAdaptiveUtilizationPolicyAdjuster(0.5),
            MinimumAdaptiveUtilizationPolicyAdjuster(29.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 98124.8,
    )
}
