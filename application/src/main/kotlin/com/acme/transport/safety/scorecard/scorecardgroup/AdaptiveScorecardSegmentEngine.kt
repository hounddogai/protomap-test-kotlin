package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive scorecard segment calculation. */
interface AdaptiveScorecardSegmentStep {
    fun describe(): String
    fun adjust(item: AdaptiveScorecardSegment, value: Double): Double
}

data class CapAdaptiveScorecardSegmentAdjuster(var cap: Double) : AdaptiveScorecardSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveScorecardSegment, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveScorecardSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveScorecardSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveScorecardSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveScorecardSegmentAdjuster(var minimum: Double) : AdaptiveScorecardSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveScorecardSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive scorecard segment steps in order and keeps a trace of each. */
data class AdaptiveScorecardSegmentEngine(
    var steps: MutableList<AdaptiveScorecardSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveScorecardSegment): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveScorecardSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveScorecardSegmentEngine(): AdaptiveScorecardSegmentEngine {
    return AdaptiveScorecardSegmentEngine(
        steps = mutableListOf<AdaptiveScorecardSegmentStep>(
            CapAdaptiveScorecardSegmentAdjuster(7292.0),
            TieredAdaptiveScorecardSegmentAdjuster(
                mutableListOf<Double>(5628.0, 7845.0, 8184.0),
                mutableListOf<Double>(0.93, 0.96, 1.15),
            ),
            MinimumAdaptiveScorecardSegmentAdjuster(32.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 38901.3,
    )
}
