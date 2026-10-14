package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted service plan target calculation. */
interface WeightedServicePlanTargetStep {
    fun describe(): String
    fun adjust(item: WeightedServicePlanTarget, value: Double): Double
}

data class TieredWeightedServicePlanTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedServicePlanTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedServicePlanTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapWeightedServicePlanTargetAdjuster(var cap: Double) : WeightedServicePlanTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedServicePlanTarget, value: Double): Double = minOf(value, cap)
}

data class PercentWeightedServicePlanTargetAdjuster(var percent: Double) : WeightedServicePlanTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedServicePlanTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedServicePlanTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedServicePlanTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedServicePlanTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted service plan target steps in order and keeps a trace of each. */
data class WeightedServicePlanTargetEngine(
    var steps: MutableList<WeightedServicePlanTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedServicePlanTarget): Double {
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

    fun evaluateAll(items: MutableList<WeightedServicePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedServicePlanTargetEngine(flags: Registry): WeightedServicePlanTargetEngine {
    return WeightedServicePlanTargetEngine(
        steps = mutableListOf<WeightedServicePlanTargetStep>(
            TieredWeightedServicePlanTargetAdjuster(
                mutableListOf<Double>(551.0, 989.0, 3928.0, 7410.0),
                mutableListOf<Double>(1.17, 1.08, 0.99, 1.02),
            ),
            CapWeightedServicePlanTargetAdjuster(42599.0),
            PercentWeightedServicePlanTargetAdjuster(16.1),
            FlaggedWeightedServicePlanTargetAdjuster(flags, "weighted-service-plan-target-fallback", 5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 29313.0,
    )
}
