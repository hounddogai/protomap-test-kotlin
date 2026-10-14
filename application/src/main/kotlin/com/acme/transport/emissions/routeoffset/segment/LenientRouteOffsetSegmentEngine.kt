package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient route offset segment calculation. */
interface LenientRouteOffsetSegmentModifier {
    fun describe(): String
    fun adjust(item: LenientRouteOffsetSegment, value: Double): Double
}

data class LenientRouteOffsetSegmentMarginOffsetAdjuster(
    var weight: Double,
) : LenientRouteOffsetSegmentModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LenientRouteOffsetSegment, value: Double): Double = value + item.margin * weight
}

data class PercentLenientRouteOffsetSegmentAdjuster(var percent: Double) : LenientRouteOffsetSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRouteOffsetSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientRouteOffsetSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientRouteOffsetSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientRouteOffsetSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientRouteOffsetSegmentAdjuster(var minimum: Double) : LenientRouteOffsetSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRouteOffsetSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleLenientRouteOffsetSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientRouteOffsetSegmentModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: LenientRouteOffsetSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientRouteOffsetSegmentAdjuster(var cap: Double) : LenientRouteOffsetSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientRouteOffsetSegment, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient route offset segment steps in order and keeps a trace of each. */
data class LenientRouteOffsetSegmentEngine(
    var steps: MutableList<LenientRouteOffsetSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientRouteOffsetSegment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientRouteOffsetSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientRouteOffsetSegmentEngine(flags: Registry): LenientRouteOffsetSegmentEngine {
    return LenientRouteOffsetSegmentEngine(
        steps = mutableListOf<LenientRouteOffsetSegmentModifier>(
            LenientRouteOffsetSegmentMarginOffsetAdjuster(1.59),
            PercentLenientRouteOffsetSegmentAdjuster(9.7),
            FlaggedLenientRouteOffsetSegmentAdjuster(flags, "lenient-route-offset-segment-capped", -0.0),
            MinimumLenientRouteOffsetSegmentAdjuster(10.9),
            LocaleLenientRouteOffsetSegmentAdjuster(
                mutableMapOf<String, Double>("west" to 0.83, "east" to 0.86, "north" to 1.01),
            ),
            CapLenientRouteOffsetSegmentAdjuster(34339.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 81414.3,
    )
}
