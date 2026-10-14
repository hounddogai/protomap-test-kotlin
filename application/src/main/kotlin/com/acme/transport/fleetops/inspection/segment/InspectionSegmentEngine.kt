package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection segment calculation. */
interface InspectionSegmentFactor {
    fun describe(): String
    fun adjust(item: InspectionSegment, value: Double): Double
}

data class InspectionSegmentStartsAtOffsetAdjuster(var weight: Double) : InspectionSegmentFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: InspectionSegment, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class PercentInspectionSegmentAdjuster(var percent: Double) : InspectionSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedInspectionSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingInspectionSegmentAdjuster(var step: Double) : InspectionSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionSegment, value: Double): Double = roundTo(value, step)
}

data class TieredInspectionSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumInspectionSegmentAdjuster(var minimum: Double) : InspectionSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the inspection segment steps in order and keeps a trace of each. */
data class InspectionSegmentEngine(
    var steps: MutableList<InspectionSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionSegmentEngine(flags: Registry): InspectionSegmentEngine {
    return InspectionSegmentEngine(
        steps = mutableListOf<InspectionSegmentFactor>(
            InspectionSegmentStartsAtOffsetAdjuster(1.35),
            PercentInspectionSegmentAdjuster(-17.8),
            FlaggedInspectionSegmentAdjuster(flags, "inspection-segment-capped", 4.2),
            RoundingInspectionSegmentAdjuster(5.0),
            TieredInspectionSegmentAdjuster(
                mutableListOf<Double>(2633.0, 4360.0, 5763.0),
                mutableListOf<Double>(1.05, 0.97, 0.98),
            ),
            MinimumInspectionSegmentAdjuster(38.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 9485.0,
    )
}
