package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard segment calculation. */
interface ScorecardSegmentAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardSegment, value: Double): Double
}

data class RoundingScorecardSegmentAdjuster(var step: Double) : ScorecardSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardSegment, value: Double): Double = roundTo(value, step)
}

data class MinimumScorecardSegmentAdjuster(var minimum: Double) : ScorecardSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScorecardSegmentEtaMinutesOffsetAdjuster(var weight: Double) : ScorecardSegmentAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: ScorecardSegment, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class CapScorecardSegmentAdjuster(var cap: Double) : ScorecardSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardSegment, value: Double): Double = minOf(value, cap)
}

data class LaneScorecardSegmentAdjuster(var factors: MutableMap<String, Double>) : ScorecardSegmentAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScorecardSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scorecard segment steps in order and keeps a trace of each. */
data class ScorecardSegmentEngine(
    var steps: MutableList<ScorecardSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScorecardSegment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardSegmentAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScorecardSegmentEngine(): ScorecardSegmentEngine {
    return ScorecardSegmentEngine(
        steps = mutableListOf<ScorecardSegmentAdjuster>(
            RoundingScorecardSegmentAdjuster(0.05),
            MinimumScorecardSegmentAdjuster(35.1),
            ScorecardSegmentEtaMinutesOffsetAdjuster(0.11),
            CapScorecardSegmentAdjuster(19452.0),
            LaneScorecardSegmentAdjuster(mutableMapOf<String, Double>("central" to 1.24)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 55816.7,
    )
}
