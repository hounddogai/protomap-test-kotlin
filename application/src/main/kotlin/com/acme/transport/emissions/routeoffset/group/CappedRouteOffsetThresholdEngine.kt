package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped route offset threshold calculation. */
interface CappedRouteOffsetThresholdAdjuster {
    fun describe(): String
    fun adjust(item: CappedRouteOffsetThreshold, value: Double): Double
}

data class CapCappedRouteOffsetThresholdAdjuster(var cap: Double) : CappedRouteOffsetThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedRouteOffsetThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentCappedRouteOffsetThresholdAdjuster(var percent: Double) : CappedRouteOffsetThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedRouteOffsetThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCappedRouteOffsetThresholdAdjuster(var minimum: Double) : CappedRouteOffsetThresholdAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRouteOffsetThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped route offset threshold steps in order and keeps a trace of each. */
data class CappedRouteOffsetThresholdEngine(
    var steps: MutableList<CappedRouteOffsetThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedRouteOffsetThreshold): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedRouteOffsetThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedRouteOffsetThresholdEngine(): CappedRouteOffsetThresholdEngine {
    return CappedRouteOffsetThresholdEngine(
        steps = mutableListOf<CappedRouteOffsetThresholdAdjuster>(
            CapCappedRouteOffsetThresholdAdjuster(34754.0),
            PercentCappedRouteOffsetThresholdAdjuster(8.7),
            MinimumCappedRouteOffsetThresholdAdjuster(21.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 87733.5,
    )
}
