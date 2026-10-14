package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the idling manifest calculation. */
interface IdlingManifestFactor {
    fun describe(): String
    fun adjust(item: IdlingManifest, value: Double): Double
}

data class MinimumIdlingManifestAdjuster(var minimum: Double) : IdlingManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedIdlingManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredIdlingManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentIdlingManifestAdjuster(var percent: Double) : IdlingManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleIdlingManifestAdjuster(var factors: MutableMap<String, Double>) : IdlingManifestFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: IdlingManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class IdlingManifestWindowDaysOffsetAdjuster(var weight: Double) : IdlingManifestFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: IdlingManifest, value: Double): Double = value + item.windowDays.toDouble() * weight
}

/** Runs the idling manifest steps in order and keeps a trace of each. */
data class IdlingManifestEngine(
    var steps: MutableList<IdlingManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingManifest): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingManifestEngine(flags: Registry): IdlingManifestEngine {
    return IdlingManifestEngine(
        steps = mutableListOf<IdlingManifestFactor>(
            MinimumIdlingManifestAdjuster(33.7),
            FlaggedIdlingManifestAdjuster(flags, "idling-manifest-default", -1.8),
            TieredIdlingManifestAdjuster(
                mutableListOf<Double>(3009.0, 3234.0),
                mutableListOf<Double>(0.85, 1.15),
            ),
            PercentIdlingManifestAdjuster(-12.4),
            LocaleIdlingManifestAdjuster(mutableMapOf<String, Double>("central" to 0.82, "retail" to 1.13)),
            IdlingManifestWindowDaysOffsetAdjuster(0.55),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 96930.5,
    )
}
