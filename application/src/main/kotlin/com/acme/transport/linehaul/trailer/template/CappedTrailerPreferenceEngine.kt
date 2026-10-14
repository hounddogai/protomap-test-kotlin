package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.flags.Registry

/** One step of the capped trailer preference calculation. */
interface CappedTrailerPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: CappedTrailerPreference, value: Double): Double
}

data class FlaggedCappedTrailerPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTrailerPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTrailerPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedTrailerPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTrailerPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTrailerPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedTrailerPreferenceAdjuster(var minimum: Double) : CappedTrailerPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTrailerPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedTrailerPreferenceAdjuster(var cap: Double) : CappedTrailerPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTrailerPreference, value: Double): Double = minOf(value, cap)
}

data class CappedTrailerPreferenceLoadKgOffsetAdjuster(var weight: Double) : CappedTrailerPreferenceAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: CappedTrailerPreference, value: Double): Double = value + item.loadKg * weight
}

data class RegionCappedTrailerPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTrailerPreferenceAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: CappedTrailerPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped trailer preference steps in order and keeps a trace of each. */
data class CappedTrailerPreferenceEngine(
    var steps: MutableList<CappedTrailerPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedTrailerPreference): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedTrailerPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedTrailerPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedTrailerPreferenceEngine(flags: Registry): CappedTrailerPreferenceEngine {
    return CappedTrailerPreferenceEngine(
        steps = mutableListOf<CappedTrailerPreferenceAdjuster>(
            FlaggedCappedTrailerPreferenceAdjuster(flags, "capped-trailer-preference-global", 5.9),
            TieredCappedTrailerPreferenceAdjuster(
                mutableListOf<Double>(3651.0, 5670.0, 6749.0, 9329.0),
                mutableListOf<Double>(1.01, 1.0, 0.97, 1.06),
            ),
            MinimumCappedTrailerPreferenceAdjuster(3.8),
            CapCappedTrailerPreferenceAdjuster(13518.0),
            CappedTrailerPreferenceLoadKgOffsetAdjuster(0.37),
            RegionCappedTrailerPreferenceAdjuster(
                mutableMapOf<String, Double>("retail" to 0.77, "south" to 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 6070.2,
    )
}
