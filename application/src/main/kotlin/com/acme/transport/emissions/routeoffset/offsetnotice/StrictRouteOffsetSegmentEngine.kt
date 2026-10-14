package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict route offset segment calculation. */
interface StrictRouteOffsetSegmentStep {
    fun describe(): String
    fun adjust(item: StrictRouteOffsetSegment, value: Double): Double
}

data class PercentStrictRouteOffsetSegmentAdjuster(var percent: Double) : StrictRouteOffsetSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRouteOffsetSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumStrictRouteOffsetSegmentAdjuster(var minimum: Double) : StrictRouteOffsetSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRouteOffsetSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictRouteOffsetSegmentAdjuster(var step: Double) : StrictRouteOffsetSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRouteOffsetSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the strict route offset segment steps in order and keeps a trace of each. */
data class StrictRouteOffsetSegmentEngine(
    var steps: MutableList<StrictRouteOffsetSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictRouteOffsetSegment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictRouteOffsetSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictRouteOffsetSegmentEngine(): StrictRouteOffsetSegmentEngine {
    return StrictRouteOffsetSegmentEngine(
        steps = mutableListOf<StrictRouteOffsetSegmentStep>(
            PercentStrictRouteOffsetSegmentAdjuster(-12.4),
            MinimumStrictRouteOffsetSegmentAdjuster(30.0),
            RoundingStrictRouteOffsetSegmentAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 3456.9,
    )
}
