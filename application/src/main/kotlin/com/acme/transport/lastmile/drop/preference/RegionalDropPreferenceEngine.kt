package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional drop preference calculation. */
interface RegionalDropPreferenceFactor {
    fun describe(): String
    fun adjust(item: RegionalDropPreference, value: Double): Double
}

data class RoundingRegionalDropPreferenceAdjuster(var step: Double) : RegionalDropPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDropPreference, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalDropPreferenceAdjuster(var percent: Double) : RegionalDropPreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDropPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalDropPreferenceAdjuster(var minimum: Double) : RegionalDropPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDropPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalDropPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDropPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDropPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalDropPreferenceAdjuster(var cap: Double) : RegionalDropPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDropPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the regional drop preference steps in order and keeps a trace of each. */
data class RegionalDropPreferenceEngine(
    var steps: MutableList<RegionalDropPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalDropPreference): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalDropPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalDropPreferenceEngine(flags: Registry): RegionalDropPreferenceEngine {
    return RegionalDropPreferenceEngine(
        steps = mutableListOf<RegionalDropPreferenceFactor>(
            RoundingRegionalDropPreferenceAdjuster(5.0),
            PercentRegionalDropPreferenceAdjuster(20.3),
            MinimumRegionalDropPreferenceAdjuster(26.4),
            FlaggedRegionalDropPreferenceAdjuster(flags, "regional-drop-preference-weighted", -3.6),
            CapRegionalDropPreferenceAdjuster(41158.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 21740.0,
    )
}
