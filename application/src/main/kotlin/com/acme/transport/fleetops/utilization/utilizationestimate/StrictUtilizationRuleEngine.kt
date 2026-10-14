package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict utilization rule calculation. */
interface StrictUtilizationRuleStep {
    fun describe(): String
    fun adjust(item: StrictUtilizationRule, value: Double): Double
}

data class TieredStrictUtilizationRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictUtilizationRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictUtilizationRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictUtilizationRuleAdjuster(var step: Double) : StrictUtilizationRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictUtilizationRule, value: Double): Double = roundTo(value, step)
}

data class CapStrictUtilizationRuleAdjuster(var cap: Double) : StrictUtilizationRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictUtilizationRule, value: Double): Double = minOf(value, cap)
}

/** Runs the strict utilization rule steps in order and keeps a trace of each. */
data class StrictUtilizationRuleEngine(
    var steps: MutableList<StrictUtilizationRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictUtilizationRule): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictUtilizationRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictUtilizationRuleEngine(): StrictUtilizationRuleEngine {
    return StrictUtilizationRuleEngine(
        steps = mutableListOf<StrictUtilizationRuleStep>(
            TieredStrictUtilizationRuleAdjuster(
                mutableListOf<Double>(3504.0, 4436.0, 6727.0, 6949.0),
                mutableListOf<Double>(1.0, 0.95, 0.82, 0.86),
            ),
            RoundingStrictUtilizationRuleAdjuster(0.05),
            CapStrictUtilizationRuleAdjuster(12854.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 55875.7,
    )
}
