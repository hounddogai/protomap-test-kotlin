package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.flags.Registry

/** One step of the fallback inspection segment calculation. */
interface FallbackInspectionSegmentModifier {
    fun describe(): String
    fun adjust(item: FallbackInspectionSegment, value: Double): Double
}

data class FallbackInspectionSegmentEndsAtOffsetAdjuster(
    var weight: Double,
) : FallbackInspectionSegmentModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: FallbackInspectionSegment, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class PercentFallbackInspectionSegmentAdjuster(var percent: Double) : FallbackInspectionSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackInspectionSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackInspectionSegmentAdjuster(var cap: Double) : FallbackInspectionSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackInspectionSegment, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackInspectionSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackInspectionSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackInspectionSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackInspectionSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackInspectionSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackInspectionSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback inspection segment steps in order and keeps a trace of each. */
data class FallbackInspectionSegmentEngine(
    var steps: MutableList<FallbackInspectionSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackInspectionSegment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackInspectionSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackInspectionSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackInspectionSegmentEngine(flags: Registry): FallbackInspectionSegmentEngine {
    return FallbackInspectionSegmentEngine(
        steps = mutableListOf<FallbackInspectionSegmentModifier>(
            FallbackInspectionSegmentEndsAtOffsetAdjuster(1.52),
            PercentFallbackInspectionSegmentAdjuster(-17.7),
            CapFallbackInspectionSegmentAdjuster(21498.0),
            TieredFallbackInspectionSegmentAdjuster(
                mutableListOf<Double>(6689.0, 9303.0, 9431.0),
                mutableListOf<Double>(0.99, 0.99, 1.16),
            ),
            FlaggedFallbackInspectionSegmentAdjuster(flags, "fallback-inspection-segment-adaptive", 6.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 82281.5,
    )
}
