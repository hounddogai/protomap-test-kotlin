package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.flags.Registry

/** One step of the tiered rest break preference calculation. */
interface TieredRestBreakPreferenceFactor {
    fun describe(): String
    fun adjust(item: TieredRestBreakPreference, value: Double): Double
}

data class MinimumTieredRestBreakPreferenceAdjuster(var minimum: Double) : TieredRestBreakPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredRestBreakPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredRestBreakPreferenceAdjuster(var cap: Double) : TieredRestBreakPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredRestBreakPreference, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredRestBreakPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredRestBreakPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredRestBreakPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredRestBreakPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredRestBreakPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredRestBreakPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TieredRestBreakPreferenceQuantityOffsetAdjuster(
    var weight: Double,
) : TieredRestBreakPreferenceFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: TieredRestBreakPreference, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the tiered rest break preference steps in order and keeps a trace of each. */
data class TieredRestBreakPreferenceEngine(
    var steps: MutableList<TieredRestBreakPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredRestBreakPreference): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredRestBreakPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredRestBreakPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredRestBreakPreferenceEngine(flags: Registry): TieredRestBreakPreferenceEngine {
    return TieredRestBreakPreferenceEngine(
        steps = mutableListOf<TieredRestBreakPreferenceFactor>(
            MinimumTieredRestBreakPreferenceAdjuster(27.7),
            CapTieredRestBreakPreferenceAdjuster(10851.0),
            FlaggedTieredRestBreakPreferenceAdjuster(flags, "tiered-rest-break-preference-lenient", 2.7),
            TieredTieredRestBreakPreferenceAdjuster(
                mutableListOf<Double>(379.0, 2456.0, 9746.0),
                mutableListOf<Double>(1.12, 1.09, 1.0),
            ),
            TieredRestBreakPreferenceQuantityOffsetAdjuster(0.98),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 84858.0,
    )
}
