package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the lane plan segment calculation. */
interface LanePlanSegmentStep {
    fun describe(): String
    fun adjust(item: LanePlanSegment, value: Double): Double
}

data class RoundingLanePlanSegmentAdjuster(var step: Double) : LanePlanSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanSegment, value: Double): Double = roundTo(value, step)
}

data class TieredLanePlanSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLanePlanSegmentAdjuster(var percent: Double) : LanePlanSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLanePlanSegmentAdjuster(var minimum: Double) : LanePlanSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lane plan segment steps in order and keeps a trace of each. */
data class LanePlanSegmentEngine(
    var steps: MutableList<LanePlanSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanSegment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanSegmentEngine(): LanePlanSegmentEngine {
    return LanePlanSegmentEngine(
        steps = mutableListOf<LanePlanSegmentStep>(
            RoundingLanePlanSegmentAdjuster(1.0),
            TieredLanePlanSegmentAdjuster(
                mutableListOf<Double>(38.0, 3239.0, 3969.0, 8212.0),
                mutableListOf<Double>(1.19, 0.85, 0.81, 1.06),
            ),
            PercentLanePlanSegmentAdjuster(1.0),
            MinimumLanePlanSegmentAdjuster(11.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 17891.2,
    )
}
