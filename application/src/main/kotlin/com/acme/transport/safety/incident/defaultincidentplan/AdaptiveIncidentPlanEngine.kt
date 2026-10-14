package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive incident plan calculation. */
interface AdaptiveIncidentPlanAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveIncidentPlan, value: Double): Double
}

data class ChannelAdaptiveIncidentPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveIncidentPlanAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: AdaptiveIncidentPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveIncidentPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveIncidentPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveIncidentPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAdaptiveIncidentPlanAdjuster(var percent: Double) : AdaptiveIncidentPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveIncidentPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveIncidentPlanAdjuster(var cap: Double) : AdaptiveIncidentPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveIncidentPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive incident plan steps in order and keeps a trace of each. */
data class AdaptiveIncidentPlanEngine(
    var steps: MutableList<AdaptiveIncidentPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveIncidentPlan): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveIncidentPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveIncidentPlanEngine(): AdaptiveIncidentPlanEngine {
    return AdaptiveIncidentPlanEngine(
        steps = mutableListOf<AdaptiveIncidentPlanAdjuster>(
            ChannelAdaptiveIncidentPlanAdjuster(mutableMapOf<String, Double>("east" to 0.76)),
            TieredAdaptiveIncidentPlanAdjuster(
                mutableListOf<Double>(3882.0, 6244.0, 9499.0),
                mutableListOf<Double>(0.93, 1.15, 1.04),
            ),
            PercentAdaptiveIncidentPlanAdjuster(2.9),
            CapAdaptiveIncidentPlanAdjuster(47450.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 6918.3,
    )
}
