package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.flags.Registry

/** One step of the regional trailer preference calculation. */
interface RegionalTrailerPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: RegionalTrailerPreference, value: Double): Double
}

data class RegionRegionalTrailerPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalTrailerPreferenceAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: RegionalTrailerPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalTrailerPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTrailerPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTrailerPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalTrailerPreferenceAdjuster(var percent: Double) : RegionalTrailerPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTrailerPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalTrailerPreferenceAdjuster(var minimum: Double) : RegionalTrailerPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTrailerPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalTrailerPreferencePalletsOffsetAdjuster(
    var weight: Double,
) : RegionalTrailerPreferenceAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: RegionalTrailerPreference, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class CapRegionalTrailerPreferenceAdjuster(var cap: Double) : RegionalTrailerPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalTrailerPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the regional trailer preference steps in order and keeps a trace of each. */
data class RegionalTrailerPreferenceEngine(
    var steps: MutableList<RegionalTrailerPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalTrailerPreference): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalTrailerPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalTrailerPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalTrailerPreferenceEngine(flags: Registry): RegionalTrailerPreferenceEngine {
    return RegionalTrailerPreferenceEngine(
        steps = mutableListOf<RegionalTrailerPreferenceAdjuster>(
            RegionRegionalTrailerPreferenceAdjuster(
                mutableMapOf<String, Double>("retail" to 1.07, "east" to 1.26),
            ),
            FlaggedRegionalTrailerPreferenceAdjuster(flags, "regional-trailer-preference-weighted", 9.8),
            PercentRegionalTrailerPreferenceAdjuster(-23.7),
            MinimumRegionalTrailerPreferenceAdjuster(48.5),
            RegionalTrailerPreferencePalletsOffsetAdjuster(1.95),
            CapRegionalTrailerPreferenceAdjuster(15512.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 25004.8,
    )
}
