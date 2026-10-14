package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching segment calculation. */
interface CoachingSegmentFactor {
    fun describe(): String
    fun adjust(item: CoachingSegment, value: Double): Double
}

data class MinimumCoachingSegmentAdjuster(var minimum: Double) : CoachingSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCoachingSegmentAdjuster(var step: Double) : CoachingSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingSegment, value: Double): Double = roundTo(value, step)
}

data class TieredCoachingSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCoachingSegmentAdjuster(var percent: Double) : CoachingSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleCoachingSegmentAdjuster(var factors: MutableMap<String, Double>) : CoachingSegmentFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: CoachingSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCoachingSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the coaching segment steps in order and keeps a trace of each. */
data class CoachingSegmentEngine(
    var steps: MutableList<CoachingSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CoachingSegment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCoachingSegmentEngine(flags: Registry): CoachingSegmentEngine {
    return CoachingSegmentEngine(
        steps = mutableListOf<CoachingSegmentFactor>(
            MinimumCoachingSegmentAdjuster(34.8),
            RoundingCoachingSegmentAdjuster(0.25),
            TieredCoachingSegmentAdjuster(
                mutableListOf<Double>(2552.0, 3367.0, 7122.0, 8999.0),
                mutableListOf<Double>(0.89, 1.16, 0.98, 0.97),
            ),
            PercentCoachingSegmentAdjuster(-13.7),
            TitleCoachingSegmentAdjuster(mutableMapOf<String, Double>("south" to 0.86, "online" to 1.4)),
            FlaggedCoachingSegmentAdjuster(flags, "coaching-segment-seasonal", -1.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 67727.5,
    )
}
