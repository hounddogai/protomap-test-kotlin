package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional clearance profile calculation. */
interface RegionalClearanceProfileFactor {
    fun describe(): String
    fun adjust(item: RegionalClearanceProfile, value: Double): Double
}

data class RoundingRegionalClearanceProfileAdjuster(var step: Double) : RegionalClearanceProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalClearanceProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalClearanceProfileAdjuster(var minimum: Double) : RegionalClearanceProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalClearanceProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalClearanceProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalClearanceProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalClearanceProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryRegionalClearanceProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalClearanceProfileFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: RegionalClearanceProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalClearanceProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalClearanceProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalClearanceProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalClearanceProfileMaximumOffsetAdjuster(var weight: Double) : RegionalClearanceProfileFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: RegionalClearanceProfile, value: Double): Double = value + item.maximum * weight
}

/** Runs the regional clearance profile steps in order and keeps a trace of each. */
data class RegionalClearanceProfileEngine(
    var steps: MutableList<RegionalClearanceProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalClearanceProfile): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalClearanceProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalClearanceProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalClearanceProfileEngine(flags: Registry): RegionalClearanceProfileEngine {
    return RegionalClearanceProfileEngine(
        steps = mutableListOf<RegionalClearanceProfileFactor>(
            RoundingRegionalClearanceProfileAdjuster(5.0),
            MinimumRegionalClearanceProfileAdjuster(40.6),
            TieredRegionalClearanceProfileAdjuster(
                mutableListOf<Double>(1550.0, 1731.0, 7087.0, 8813.0),
                mutableListOf<Double>(1.2, 0.95, 1.01, 1.03),
            ),
            CountryRegionalClearanceProfileAdjuster(
                mutableMapOf<String, Double>("south" to 1.39, "east" to 1.29),
            ),
            FlaggedRegionalClearanceProfileAdjuster(flags, "regional-clearance-profile-scheduled", -2.9),
            RegionalClearanceProfileMaximumOffsetAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 44487.3,
    )
}
