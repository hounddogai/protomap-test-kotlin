package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive service plan mapping calculation. */
interface AdaptiveServicePlanMappingStep {
    fun describe(): String
    fun adjust(item: AdaptiveServicePlanMapping, value: Double): Double
}

data class RoundingAdaptiveServicePlanMappingAdjuster(var step: Double) : AdaptiveServicePlanMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveServicePlanMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveServicePlanMappingAdjuster(var minimum: Double) : AdaptiveServicePlanMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveServicePlanMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentAdaptiveServicePlanMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveServicePlanMappingStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: AdaptiveServicePlanMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveServicePlanMappingAdjuster(var percent: Double) : AdaptiveServicePlanMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveServicePlanMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive service plan mapping steps in order and keeps a trace of each. */
data class AdaptiveServicePlanMappingEngine(
    var steps: MutableList<AdaptiveServicePlanMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveServicePlanMapping): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveServicePlanMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveServicePlanMappingEngine(): AdaptiveServicePlanMappingEngine {
    return AdaptiveServicePlanMappingEngine(
        steps = mutableListOf<AdaptiveServicePlanMappingStep>(
            RoundingAdaptiveServicePlanMappingAdjuster(1.0),
            MinimumAdaptiveServicePlanMappingAdjuster(36.2),
            SegmentAdaptiveServicePlanMappingAdjuster(
                mutableMapOf<String, Double>("retail" to 1.25, "south" to 0.99, "east" to 0.76),
            ),
            PercentAdaptiveServicePlanMappingAdjuster(4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 33861.1,
    )
}
