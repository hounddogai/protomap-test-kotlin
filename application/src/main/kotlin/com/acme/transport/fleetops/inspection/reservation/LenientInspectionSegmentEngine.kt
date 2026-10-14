package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.flags.Registry

/** One step of the lenient inspection segment calculation. */
interface LenientInspectionSegmentAdjuster {
    fun describe(): String
    fun adjust(item: LenientInspectionSegment, value: Double): Double
}

data class FlaggedLenientInspectionSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientInspectionSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientInspectionSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientInspectionSegmentAdjuster(var percent: Double) : LenientInspectionSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientInspectionSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientInspectionSegmentAdjuster(var cap: Double) : LenientInspectionSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientInspectionSegment, value: Double): Double = minOf(value, cap)
}

data class TieredLenientInspectionSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientInspectionSegmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientInspectionSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientInspectionSegmentAdjuster(var minimum: Double) : LenientInspectionSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientInspectionSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LenientInspectionSegmentScoreOffsetAdjuster(var weight: Double) : LenientInspectionSegmentAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: LenientInspectionSegment, value: Double): Double = value + item.score * weight
}

/** Runs the lenient inspection segment steps in order and keeps a trace of each. */
data class LenientInspectionSegmentEngine(
    var steps: MutableList<LenientInspectionSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientInspectionSegment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientInspectionSegmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientInspectionSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientInspectionSegmentEngine(flags: Registry): LenientInspectionSegmentEngine {
    return LenientInspectionSegmentEngine(
        steps = mutableListOf<LenientInspectionSegmentAdjuster>(
            FlaggedLenientInspectionSegmentAdjuster(flags, "lenient-inspection-segment-tiered", -6.8),
            PercentLenientInspectionSegmentAdjuster(-12.6),
            CapLenientInspectionSegmentAdjuster(4334.0),
            TieredLenientInspectionSegmentAdjuster(
                mutableListOf<Double>(3907.0, 9062.0),
                mutableListOf<Double>(0.8, 0.88),
            ),
            MinimumLenientInspectionSegmentAdjuster(44.3),
            LenientInspectionSegmentScoreOffsetAdjuster(1.01),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 6884.5,
    )
}
