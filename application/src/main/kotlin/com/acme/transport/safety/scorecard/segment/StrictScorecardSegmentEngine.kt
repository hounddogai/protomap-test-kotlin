package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict scorecard segment calculation. */
interface StrictScorecardSegmentModifier {
    fun describe(): String
    fun adjust(item: StrictScorecardSegment, value: Double): Double
}

data class MinimumStrictScorecardSegmentAdjuster(var minimum: Double) : StrictScorecardSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictScorecardSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictScorecardSegmentSequenceOffsetAdjuster(var weight: Double) : StrictScorecardSegmentModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: StrictScorecardSegment, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class PercentStrictScorecardSegmentAdjuster(var percent: Double) : StrictScorecardSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictScorecardSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingStrictScorecardSegmentAdjuster(var step: Double) : StrictScorecardSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictScorecardSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the strict scorecard segment steps in order and keeps a trace of each. */
data class StrictScorecardSegmentEngine(
    var steps: MutableList<StrictScorecardSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictScorecardSegment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictScorecardSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictScorecardSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictScorecardSegmentEngine(): StrictScorecardSegmentEngine {
    return StrictScorecardSegmentEngine(
        steps = mutableListOf<StrictScorecardSegmentModifier>(
            MinimumStrictScorecardSegmentAdjuster(10.0),
            StrictScorecardSegmentSequenceOffsetAdjuster(1.8),
            PercentStrictScorecardSegmentAdjuster(-15.6),
            RoundingStrictScorecardSegmentAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 12768.7,
    )
}
