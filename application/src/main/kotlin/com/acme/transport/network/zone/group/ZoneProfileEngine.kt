package com.acme.transport.network.zone.group

import com.acme.transport.kit.flags.Registry

/** One step of the zone profile calculation. */
interface ZoneProfileStep {
    fun describe(): String
    fun adjust(item: ZoneProfile, value: Double): Double
}

data class DepotZoneProfileAdjuster(var factors: MutableMap<String, Double>) : ZoneProfileStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: ZoneProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentZoneProfileAdjuster(var percent: Double) : ZoneProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredZoneProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumZoneProfileAdjuster(var minimum: Double) : ZoneProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapZoneProfileAdjuster(var cap: Double) : ZoneProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneProfile, value: Double): Double = minOf(value, cap)
}

data class FlaggedZoneProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the zone profile steps in order and keeps a trace of each. */
data class ZoneProfileEngine(
    var steps: MutableList<ZoneProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZoneProfile): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZoneProfileEngine(flags: Registry): ZoneProfileEngine {
    return ZoneProfileEngine(
        steps = mutableListOf<ZoneProfileStep>(
            DepotZoneProfileAdjuster(mutableMapOf<String, Double>("online" to 1.26, "south" to 1.02)),
            PercentZoneProfileAdjuster(-2.6),
            TieredZoneProfileAdjuster(
                mutableListOf<Double>(739.0, 6395.0, 8557.0),
                mutableListOf<Double>(1.19, 1.11, 0.84),
            ),
            MinimumZoneProfileAdjuster(43.8),
            CapZoneProfileAdjuster(2846.0),
            FlaggedZoneProfileAdjuster(flags, "zone-profile-rolling", -2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 82808.9,
    )
}
