package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual availability segment calculation. */
interface ManualAvailabilitySegmentFactor {
    fun describe(): String
    fun adjust(item: ManualAvailabilitySegment, value: Double): Double
}

data class FlaggedManualAvailabilitySegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAvailabilitySegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAvailabilitySegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualAvailabilitySegmentOdometerOffsetAdjuster(
    var weight: Double,
) : ManualAvailabilitySegmentFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: ManualAvailabilitySegment, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class TieredManualAvailabilitySegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAvailabilitySegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAvailabilitySegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualAvailabilitySegmentAdjuster(var percent: Double) : ManualAvailabilitySegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAvailabilitySegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualAvailabilitySegmentAdjuster(var minimum: Double) : ManualAvailabilitySegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAvailabilitySegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual availability segment steps in order and keeps a trace of each. */
data class ManualAvailabilitySegmentEngine(
    var steps: MutableList<ManualAvailabilitySegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualAvailabilitySegment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualAvailabilitySegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualAvailabilitySegmentEngine(flags: Registry): ManualAvailabilitySegmentEngine {
    return ManualAvailabilitySegmentEngine(
        steps = mutableListOf<ManualAvailabilitySegmentFactor>(
            FlaggedManualAvailabilitySegmentAdjuster(flags, "manual-availability-segment-legacy", 0.6),
            ManualAvailabilitySegmentOdometerOffsetAdjuster(0.51),
            TieredManualAvailabilitySegmentAdjuster(
                mutableListOf<Double>(120.0, 6030.0),
                mutableListOf<Double>(0.87, 1.14),
            ),
            PercentManualAvailabilitySegmentAdjuster(-16.3),
            MinimumManualAvailabilitySegmentAdjuster(23.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 91927.6,
    )
}
