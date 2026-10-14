package com.acme.transport.network.zone.segment

import com.acme.transport.kit.flags.Registry

/** One step of the zone segment calculation. */
interface ZoneSegmentAdjuster {
    fun describe(): String
    fun adjust(item: ZoneSegment, value: Double): Double
}

data class CapZoneSegmentAdjuster(var cap: Double) : ZoneSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneSegment, value: Double): Double = minOf(value, cap)
}

data class TieredZoneSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneSegmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedZoneSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentZoneSegmentAdjuster(var percent: Double) : ZoneSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the zone segment steps in order and keeps a trace of each. */
data class ZoneSegmentEngine(
    var steps: MutableList<ZoneSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ZoneSegment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneSegmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultZoneSegmentEngine(flags: Registry): ZoneSegmentEngine {
    return ZoneSegmentEngine(
        steps = mutableListOf<ZoneSegmentAdjuster>(
            CapZoneSegmentAdjuster(28345.0),
            TieredZoneSegmentAdjuster(mutableListOf<Double>(4624.0, 8029.0), mutableListOf<Double>(1.04, 0.93)),
            FlaggedZoneSegmentAdjuster(flags, "zone-segment-strict", -8.5),
            PercentZoneSegmentAdjuster(5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 59934.4,
    )
}
