package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback service plan profile calculation. */
interface FallbackServicePlanProfileAdjuster {
    fun describe(): String
    fun adjust(item: FallbackServicePlanProfile, value: Double): Double
}

data class CapFallbackServicePlanProfileAdjuster(var cap: Double) : FallbackServicePlanProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackServicePlanProfile, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackServicePlanProfileAdjuster(var percent: Double) : FallbackServicePlanProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackServicePlanProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelFallbackServicePlanProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackServicePlanProfileAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackServicePlanProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback service plan profile steps in order and keeps a trace of each. */
data class FallbackServicePlanProfileEngine(
    var steps: MutableList<FallbackServicePlanProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackServicePlanProfile): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackServicePlanProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackServicePlanProfileEngine(): FallbackServicePlanProfileEngine {
    return FallbackServicePlanProfileEngine(
        steps = mutableListOf<FallbackServicePlanProfileAdjuster>(
            CapFallbackServicePlanProfileAdjuster(33495.0),
            PercentFallbackServicePlanProfileAdjuster(2.8),
            LabelFallbackServicePlanProfileAdjuster(mutableMapOf<String, Double>("south" to 1.11)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 78173.7,
    )
}
