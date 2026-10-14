package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal route offset segment calculation. */
interface SeasonalRouteOffsetSegmentStep {
    fun describe(): String
    fun adjust(item: SeasonalRouteOffsetSegment, value: Double): Double
}

data class MinimumSeasonalRouteOffsetSegmentAdjuster(var minimum: Double) : SeasonalRouteOffsetSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRouteOffsetSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalRouteOffsetSegmentAdjuster(var step: Double) : SeasonalRouteOffsetSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRouteOffsetSegment, value: Double): Double = roundTo(value, step)
}

data class SeasonalRouteOffsetSegmentAttemptsOffsetAdjuster(
    var weight: Double,
) : SeasonalRouteOffsetSegmentStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: SeasonalRouteOffsetSegment, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class PercentSeasonalRouteOffsetSegmentAdjuster(var percent: Double) : SeasonalRouteOffsetSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRouteOffsetSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal route offset segment steps in order and keeps a trace of each. */
data class SeasonalRouteOffsetSegmentEngine(
    var steps: MutableList<SeasonalRouteOffsetSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalRouteOffsetSegment): Double {
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

    fun evaluateAll(items: MutableList<SeasonalRouteOffsetSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalRouteOffsetSegmentEngine(): SeasonalRouteOffsetSegmentEngine {
    return SeasonalRouteOffsetSegmentEngine(
        steps = mutableListOf<SeasonalRouteOffsetSegmentStep>(
            MinimumSeasonalRouteOffsetSegmentAdjuster(49.8),
            RoundingSeasonalRouteOffsetSegmentAdjuster(5.0),
            SeasonalRouteOffsetSegmentAttemptsOffsetAdjuster(1.41),
            PercentSeasonalRouteOffsetSegmentAdjuster(8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 84047.1,
    )
}
