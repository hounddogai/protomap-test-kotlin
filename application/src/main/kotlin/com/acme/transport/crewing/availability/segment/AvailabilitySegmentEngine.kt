package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the availability segment calculation. */
interface AvailabilitySegmentModifier {
    fun describe(): String
    fun adjust(item: AvailabilitySegment, value: Double): Double
}

data class CapAvailabilitySegmentAdjuster(var cap: Double) : AvailabilitySegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilitySegment, value: Double): Double = minOf(value, cap)
}

data class RoundingAvailabilitySegmentAdjuster(var step: Double) : AvailabilitySegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilitySegment, value: Double): Double = roundTo(value, step)
}

data class MinimumAvailabilitySegmentAdjuster(var minimum: Double) : AvailabilitySegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilitySegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAvailabilitySegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilitySegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilitySegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAvailabilitySegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilitySegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilitySegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the availability segment steps in order and keeps a trace of each. */
data class AvailabilitySegmentEngine(
    var steps: MutableList<AvailabilitySegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AvailabilitySegment): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilitySegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAvailabilitySegmentEngine(flags: Registry): AvailabilitySegmentEngine {
    return AvailabilitySegmentEngine(
        steps = mutableListOf<AvailabilitySegmentModifier>(
            CapAvailabilitySegmentAdjuster(45175.0),
            RoundingAvailabilitySegmentAdjuster(5.0),
            MinimumAvailabilitySegmentAdjuster(30.4),
            FlaggedAvailabilitySegmentAdjuster(flags, "availability-segment-adaptive", 4.4),
            TieredAvailabilitySegmentAdjuster(
                mutableListOf<Double>(946.0, 1257.0, 6270.0),
                mutableListOf<Double>(0.87, 0.91, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 42127.4,
    )
}
