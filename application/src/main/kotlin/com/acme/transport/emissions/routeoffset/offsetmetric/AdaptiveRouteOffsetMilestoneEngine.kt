package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive route offset milestone calculation. */
interface AdaptiveRouteOffsetMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveRouteOffsetMilestone, value: Double): Double
}

data class PercentAdaptiveRouteOffsetMilestoneAdjuster(
    var percent: Double,
) : AdaptiveRouteOffsetMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveRouteOffsetMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveRouteOffsetMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveRouteOffsetMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveRouteOffsetMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveRouteOffsetMilestoneAdjuster(var cap: Double) : AdaptiveRouteOffsetMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveRouteOffsetMilestone, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveRouteOffsetMilestoneAdjuster(
    var step: Double,
) : AdaptiveRouteOffsetMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveRouteOffsetMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive route offset milestone steps in order and keeps a trace of each. */
data class AdaptiveRouteOffsetMilestoneEngine(
    var steps: MutableList<AdaptiveRouteOffsetMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveRouteOffsetMilestone): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveRouteOffsetMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveRouteOffsetMilestoneEngine(): AdaptiveRouteOffsetMilestoneEngine {
    return AdaptiveRouteOffsetMilestoneEngine(
        steps = mutableListOf<AdaptiveRouteOffsetMilestoneAdjuster>(
            PercentAdaptiveRouteOffsetMilestoneAdjuster(-7.1),
            TieredAdaptiveRouteOffsetMilestoneAdjuster(
                mutableListOf<Double>(2105.0, 6342.0, 7745.0, 9202.0),
                mutableListOf<Double>(1.06, 1.11, 1.09, 1.04),
            ),
            CapAdaptiveRouteOffsetMilestoneAdjuster(38942.0),
            RoundingAdaptiveRouteOffsetMilestoneAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 97020.8,
    )
}
