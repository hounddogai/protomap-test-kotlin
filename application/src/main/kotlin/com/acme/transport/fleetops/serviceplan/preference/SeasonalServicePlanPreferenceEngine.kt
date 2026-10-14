package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal service plan preference calculation. */
interface SeasonalServicePlanPreferenceStep {
    fun describe(): String
    fun adjust(item: SeasonalServicePlanPreference, value: Double): Double
}

data class PercentSeasonalServicePlanPreferenceAdjuster(
    var percent: Double,
) : SeasonalServicePlanPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalServicePlanPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSeasonalServicePlanPreferenceAdjuster(
    var minimum: Double,
) : SeasonalServicePlanPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalServicePlanPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalServicePlanPreferenceAdjuster(var cap: Double) : SeasonalServicePlanPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalServicePlanPreference, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalServicePlanPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalServicePlanPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalServicePlanPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalServicePlanPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalServicePlanPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalServicePlanPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal service plan preference steps in order and keeps a trace of each. */
data class SeasonalServicePlanPreferenceEngine(
    var steps: MutableList<SeasonalServicePlanPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalServicePlanPreference): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalServicePlanPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalServicePlanPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalServicePlanPreferenceEngine(flags: Registry): SeasonalServicePlanPreferenceEngine {
    return SeasonalServicePlanPreferenceEngine(
        steps = mutableListOf<SeasonalServicePlanPreferenceStep>(
            PercentSeasonalServicePlanPreferenceAdjuster(-4.1),
            MinimumSeasonalServicePlanPreferenceAdjuster(39.1),
            CapSeasonalServicePlanPreferenceAdjuster(36886.0),
            TieredSeasonalServicePlanPreferenceAdjuster(
                mutableListOf<Double>(445.0, 3483.0, 5797.0, 9361.0),
                mutableListOf<Double>(0.95, 0.88, 1.07, 0.94),
            ),
            FlaggedSeasonalServicePlanPreferenceAdjuster(
                flags,
                "seasonal-service-plan-preference-weighted",
                -2.5,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 51414.9,
    )
}
