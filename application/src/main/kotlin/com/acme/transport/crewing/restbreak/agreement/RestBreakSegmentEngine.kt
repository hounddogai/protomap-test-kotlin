package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break segment calculation. */
interface RestBreakSegmentFactor {
    fun describe(): String
    fun adjust(item: RestBreakSegment, value: Double): Double
}

data class FlaggedRestBreakSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelRestBreakSegmentAdjuster(var factors: MutableMap<String, Double>) : RestBreakSegmentFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RestBreakSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRestBreakSegmentAdjuster(var cap: Double) : RestBreakSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakSegment, value: Double): Double = minOf(value, cap)
}

data class MinimumRestBreakSegmentAdjuster(var minimum: Double) : RestBreakSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRestBreakSegmentAdjuster(var step: Double) : RestBreakSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakSegment, value: Double): Double = roundTo(value, step)
}

data class RestBreakSegmentMinimumOffsetAdjuster(var weight: Double) : RestBreakSegmentFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: RestBreakSegment, value: Double): Double = value + item.minimum * weight
}

/** Runs the rest break segment steps in order and keeps a trace of each. */
data class RestBreakSegmentEngine(
    var steps: MutableList<RestBreakSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakSegment): Double {
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

    fun evaluateAll(items: MutableList<RestBreakSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakSegmentEngine(flags: Registry): RestBreakSegmentEngine {
    return RestBreakSegmentEngine(
        steps = mutableListOf<RestBreakSegmentFactor>(
            FlaggedRestBreakSegmentAdjuster(flags, "rest-break-segment-scheduled", 2.8),
            LabelRestBreakSegmentAdjuster(
                mutableMapOf<String, Double>("central" to 0.89, "north" to 1.02, "retail" to 1.36),
            ),
            CapRestBreakSegmentAdjuster(46868.0),
            MinimumRestBreakSegmentAdjuster(18.1),
            RoundingRestBreakSegmentAdjuster(1.0),
            RestBreakSegmentMinimumOffsetAdjuster(0.18),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 53918.8,
    )
}
