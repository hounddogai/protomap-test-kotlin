package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal incident override calculation. */
interface SeasonalIncidentOverrideStep {
    fun describe(): String
    fun adjust(item: SeasonalIncidentOverride, value: Double): Double
}

data class PercentSeasonalIncidentOverrideAdjuster(var percent: Double) : SeasonalIncidentOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalIncidentOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalIncidentOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalIncidentOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalIncidentOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalIncidentOverrideAdjuster(var cap: Double) : SeasonalIncidentOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalIncidentOverride, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalIncidentOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalIncidentOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalIncidentOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSeasonalIncidentOverrideAdjuster(var step: Double) : SeasonalIncidentOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalIncidentOverride, value: Double): Double = roundTo(value, step)
}

data class SeasonalIncidentOverridePalletsOffsetAdjuster(var weight: Double) : SeasonalIncidentOverrideStep {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: SeasonalIncidentOverride, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

/** Runs the seasonal incident override steps in order and keeps a trace of each. */
data class SeasonalIncidentOverrideEngine(
    var steps: MutableList<SeasonalIncidentOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalIncidentOverride): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalIncidentOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalIncidentOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalIncidentOverrideEngine(flags: Registry): SeasonalIncidentOverrideEngine {
    return SeasonalIncidentOverrideEngine(
        steps = mutableListOf<SeasonalIncidentOverrideStep>(
            PercentSeasonalIncidentOverrideAdjuster(9.8),
            FlaggedSeasonalIncidentOverrideAdjuster(flags, "seasonal-incident-override-strict", 8.1),
            CapSeasonalIncidentOverrideAdjuster(16538.0),
            TieredSeasonalIncidentOverrideAdjuster(
                mutableListOf<Double>(166.0, 4805.0),
                mutableListOf<Double>(0.9, 0.98),
            ),
            RoundingSeasonalIncidentOverrideAdjuster(0.25),
            SeasonalIncidentOverridePalletsOffsetAdjuster(1.37),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 76631.4,
    )
}
