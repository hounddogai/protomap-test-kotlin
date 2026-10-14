package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled zone segment calculation. */
interface ScheduledZoneSegmentAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledZoneSegment, value: Double): Double
}

data class ScheduledZoneSegmentQuantityOffsetAdjuster(var weight: Double) : ScheduledZoneSegmentAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ScheduledZoneSegment, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingScheduledZoneSegmentAdjuster(var step: Double) : ScheduledZoneSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledZoneSegment, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledZoneSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledZoneSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledZoneSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledZoneSegmentAdjuster(var minimum: Double) : ScheduledZoneSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledZoneSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledZoneSegmentAdjuster(var cap: Double) : ScheduledZoneSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledZoneSegment, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled zone segment steps in order and keeps a trace of each. */
data class ScheduledZoneSegmentEngine(
    var steps: MutableList<ScheduledZoneSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledZoneSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledZoneSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledZoneSegmentEngine(flags: Registry): ScheduledZoneSegmentEngine {
    return ScheduledZoneSegmentEngine(
        steps = mutableListOf<ScheduledZoneSegmentAdjuster>(
            ScheduledZoneSegmentQuantityOffsetAdjuster(1.87),
            RoundingScheduledZoneSegmentAdjuster(0.25),
            FlaggedScheduledZoneSegmentAdjuster(flags, "scheduled-zone-segment-lenient", -5.7),
            MinimumScheduledZoneSegmentAdjuster(11.2),
            CapScheduledZoneSegmentAdjuster(37904.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 30301.7,
    )
}
