package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient route offset estimate calculation. */
interface LenientRouteOffsetEstimateStep {
    fun describe(): String
    fun adjust(item: LenientRouteOffsetEstimate, value: Double): Double
}

data class SegmentLenientRouteOffsetEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientRouteOffsetEstimateStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: LenientRouteOffsetEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientRouteOffsetEstimateAdjuster(var minimum: Double) : LenientRouteOffsetEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRouteOffsetEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientRouteOffsetEstimateAdjuster(var cap: Double) : LenientRouteOffsetEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientRouteOffsetEstimate, value: Double): Double = minOf(value, cap)
}

data class LenientRouteOffsetEstimateMaximumOffsetAdjuster(
    var weight: Double,
) : LenientRouteOffsetEstimateStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: LenientRouteOffsetEstimate, value: Double): Double = value + item.maximum * weight
}

data class PercentLenientRouteOffsetEstimateAdjuster(var percent: Double) : LenientRouteOffsetEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRouteOffsetEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient route offset estimate steps in order and keeps a trace of each. */
data class LenientRouteOffsetEstimateEngine(
    var steps: MutableList<LenientRouteOffsetEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientRouteOffsetEstimate): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientRouteOffsetEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientRouteOffsetEstimateEngine(): LenientRouteOffsetEstimateEngine {
    return LenientRouteOffsetEstimateEngine(
        steps = mutableListOf<LenientRouteOffsetEstimateStep>(
            SegmentLenientRouteOffsetEstimateAdjuster(mutableMapOf<String, Double>("central" to 0.92)),
            MinimumLenientRouteOffsetEstimateAdjuster(0.9),
            CapLenientRouteOffsetEstimateAdjuster(19964.0),
            LenientRouteOffsetEstimateMaximumOffsetAdjuster(1.25),
            PercentLenientRouteOffsetEstimateAdjuster(-20.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 24202.4,
    )
}
