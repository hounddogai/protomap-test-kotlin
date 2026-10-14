package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer segment calculation. */
interface TrailerSegmentModifier {
    fun describe(): String
    fun adjust(item: TrailerSegment, value: Double): Double
}

data class FlaggedTrailerSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTrailerSegmentAdjuster(var cap: Double) : TrailerSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerSegment, value: Double): Double = minOf(value, cap)
}

data class MinimumTrailerSegmentAdjuster(var minimum: Double) : TrailerSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTrailerSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTrailerSegmentAdjuster(var percent: Double) : TrailerSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the trailer segment steps in order and keeps a trace of each. */
data class TrailerSegmentEngine(
    var steps: MutableList<TrailerSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerSegment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerSegmentEngine(flags: Registry): TrailerSegmentEngine {
    return TrailerSegmentEngine(
        steps = mutableListOf<TrailerSegmentModifier>(
            FlaggedTrailerSegmentAdjuster(flags, "trailer-segment-global", -7.2),
            CapTrailerSegmentAdjuster(46951.0),
            MinimumTrailerSegmentAdjuster(36.2),
            TieredTrailerSegmentAdjuster(
                mutableListOf<Double>(5593.0, 7815.0, 8649.0),
                mutableListOf<Double>(0.94, 1.04, 1.08),
            ),
            PercentTrailerSegmentAdjuster(-23.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 53892.2,
    )
}
