package com.acme.transport.network.zone.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the zone entry calculation. */
interface ZoneEntryAdjuster {
    fun describe(): String
    fun adjust(item: ZoneEntry, value: Double): Double
}

data class FlaggedZoneEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredZoneEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentZoneEntryAdjuster(var percent: Double) : ZoneEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapZoneEntryAdjuster(var cap: Double) : ZoneEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneEntry, value: Double): Double = minOf(value, cap)
}

data class RoundingZoneEntryAdjuster(var step: Double) : ZoneEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the zone entry steps in order and keeps a trace of each. */
data class ZoneEntryEngine(
    var steps: MutableList<ZoneEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneEntry): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneEntryEngine(flags: Registry): ZoneEntryEngine {
    return ZoneEntryEngine(
        steps = mutableListOf<ZoneEntryAdjuster>(
            FlaggedZoneEntryAdjuster(flags, "zone-entry-adaptive", -7.6),
            TieredZoneEntryAdjuster(mutableListOf<Double>(1102.0, 6157.0), mutableListOf<Double>(1.04, 1.12)),
            PercentZoneEntryAdjuster(18.9),
            CapZoneEntryAdjuster(32051.0),
            RoundingZoneEntryAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 58194.5,
    )
}
