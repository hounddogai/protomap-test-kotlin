package com.acme.transport.network.zone.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy zone profile calculation. */
interface LegacyZoneProfileFactor {
    fun describe(): String
    fun adjust(item: LegacyZoneProfile, value: Double): Double
}

data class DepotLegacyZoneProfileAdjuster(var factors: MutableMap<String, Double>) : LegacyZoneProfileFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: LegacyZoneProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyZoneProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyZoneProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyZoneProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyZoneProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyZoneProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyZoneProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyZoneProfileAdjuster(var percent: Double) : LegacyZoneProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyZoneProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyZoneProfileAdjuster(var step: Double) : LegacyZoneProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyZoneProfile, value: Double): Double = roundTo(value, step)
}

data class CapLegacyZoneProfileAdjuster(var cap: Double) : LegacyZoneProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyZoneProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy zone profile steps in order and keeps a trace of each. */
data class LegacyZoneProfileEngine(
    var steps: MutableList<LegacyZoneProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyZoneProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyZoneProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyZoneProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyZoneProfileEngine(flags: Registry): LegacyZoneProfileEngine {
    return LegacyZoneProfileEngine(
        steps = mutableListOf<LegacyZoneProfileFactor>(
            DepotLegacyZoneProfileAdjuster(
                mutableMapOf<String, Double>("central" to 1.18, "east" to 1.21, "retail" to 1.3),
            ),
            FlaggedLegacyZoneProfileAdjuster(flags, "legacy-zone-profile-tiered", 1.1),
            TieredLegacyZoneProfileAdjuster(
                mutableListOf<Double>(1276.0, 7137.0, 8437.0),
                mutableListOf<Double>(0.93, 0.87, 1.09),
            ),
            PercentLegacyZoneProfileAdjuster(-14.3),
            RoundingLegacyZoneProfileAdjuster(0.25),
            CapLegacyZoneProfileAdjuster(49321.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 8077.5,
    )
}
