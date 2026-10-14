package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.flags.Registry

/** One step of the sortation preference calculation. */
interface SortationPreferenceFactor {
    fun describe(): String
    fun adjust(item: SortationPreference, value: Double): Double
}

data class MinimumSortationPreferenceAdjuster(var minimum: Double) : SortationPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSortationPreferenceAdjuster(var cap: Double) : SortationPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationPreference, value: Double): Double = minOf(value, cap)
}

data class FlaggedSortationPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSortationPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the sortation preference steps in order and keeps a trace of each. */
data class SortationPreferenceEngine(
    var steps: MutableList<SortationPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SortationPreference): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SortationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSortationPreferenceEngine(flags: Registry): SortationPreferenceEngine {
    return SortationPreferenceEngine(
        steps = mutableListOf<SortationPreferenceFactor>(
            MinimumSortationPreferenceAdjuster(3.4),
            CapSortationPreferenceAdjuster(21073.0),
            FlaggedSortationPreferenceAdjuster(flags, "sortation-preference-primary", 3.4),
            TieredSortationPreferenceAdjuster(
                mutableListOf<Double>(984.0, 3527.0, 5081.0, 6889.0),
                mutableListOf<Double>(1.1, 0.97, 1.11, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 50037.3,
    )
}
