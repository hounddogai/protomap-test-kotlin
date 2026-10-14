package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive utilization plan calculation. */
interface AdaptiveUtilizationPlanAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveUtilizationPlan, value: Double): Double
}

data class MinimumAdaptiveUtilizationPlanAdjuster(var minimum: Double) : AdaptiveUtilizationPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveUtilizationPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveUtilizationPlanAdjuster(var step: Double) : AdaptiveUtilizationPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveUtilizationPlan, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveUtilizationPlanAdjuster(var cap: Double) : AdaptiveUtilizationPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveUtilizationPlan, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveUtilizationPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveUtilizationPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveUtilizationPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive utilization plan steps in order and keeps a trace of each. */
data class AdaptiveUtilizationPlanEngine(
    var steps: MutableList<AdaptiveUtilizationPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveUtilizationPlan): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveUtilizationPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveUtilizationPlanEngine(): AdaptiveUtilizationPlanEngine {
    return AdaptiveUtilizationPlanEngine(
        steps = mutableListOf<AdaptiveUtilizationPlanAdjuster>(
            MinimumAdaptiveUtilizationPlanAdjuster(28.3),
            RoundingAdaptiveUtilizationPlanAdjuster(0.25),
            CapAdaptiveUtilizationPlanAdjuster(39703.0),
            TieredAdaptiveUtilizationPlanAdjuster(
                mutableListOf<Double>(4932.0, 6006.0, 8241.0),
                mutableListOf<Double>(1.05, 0.85, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 97897.4,
    )
}
