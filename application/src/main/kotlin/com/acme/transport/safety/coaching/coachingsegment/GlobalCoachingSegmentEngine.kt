package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global coaching segment calculation. */
interface GlobalCoachingSegmentModifier {
    fun describe(): String
    fun adjust(item: GlobalCoachingSegment, value: Double): Double
}

data class CapGlobalCoachingSegmentAdjuster(var cap: Double) : GlobalCoachingSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCoachingSegment, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalCoachingSegmentAdjuster(var step: Double) : GlobalCoachingSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCoachingSegment, value: Double): Double = roundTo(value, step)
}

data class GlobalCoachingSegmentRevisionOffsetAdjuster(var weight: Double) : GlobalCoachingSegmentModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: GlobalCoachingSegment, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class TieredGlobalCoachingSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCoachingSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCoachingSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneGlobalCoachingSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalCoachingSegmentModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: GlobalCoachingSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalCoachingSegmentAdjuster(var percent: Double) : GlobalCoachingSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCoachingSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global coaching segment steps in order and keeps a trace of each. */
data class GlobalCoachingSegmentEngine(
    var steps: MutableList<GlobalCoachingSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalCoachingSegment): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalCoachingSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalCoachingSegmentEngine(): GlobalCoachingSegmentEngine {
    return GlobalCoachingSegmentEngine(
        steps = mutableListOf<GlobalCoachingSegmentModifier>(
            CapGlobalCoachingSegmentAdjuster(44126.0),
            RoundingGlobalCoachingSegmentAdjuster(0.5),
            GlobalCoachingSegmentRevisionOffsetAdjuster(1.91),
            TieredGlobalCoachingSegmentAdjuster(
                mutableListOf<Double>(2361.0, 4200.0),
                mutableListOf<Double>(1.18, 1.16),
            ),
            LaneGlobalCoachingSegmentAdjuster(
                mutableMapOf<String, Double>("south" to 0.99, "east" to 1.06, "retail" to 1.36),
            ),
            PercentGlobalCoachingSegmentAdjuster(19.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 21030.1,
    )
}
