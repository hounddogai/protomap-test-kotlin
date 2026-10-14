package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy incident preference calculation. */
interface LegacyIncidentPreferenceStep {
    fun describe(): String
    fun adjust(item: LegacyIncidentPreference, value: Double): Double
}

data class LegacyIncidentPreferenceOdometerOffsetAdjuster(var weight: Double) : LegacyIncidentPreferenceStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: LegacyIncidentPreference, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class PercentLegacyIncidentPreferenceAdjuster(var percent: Double) : LegacyIncidentPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyIncidentPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyIncidentPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyIncidentPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyIncidentPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyIncidentPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyIncidentPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyIncidentPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyIncidentPreferenceAdjuster(var step: Double) : LegacyIncidentPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyIncidentPreference, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacyIncidentPreferenceAdjuster(var minimum: Double) : LegacyIncidentPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyIncidentPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy incident preference steps in order and keeps a trace of each. */
data class LegacyIncidentPreferenceEngine(
    var steps: MutableList<LegacyIncidentPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyIncidentPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyIncidentPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyIncidentPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyIncidentPreferenceEngine(flags: Registry): LegacyIncidentPreferenceEngine {
    return LegacyIncidentPreferenceEngine(
        steps = mutableListOf<LegacyIncidentPreferenceStep>(
            LegacyIncidentPreferenceOdometerOffsetAdjuster(0.86),
            PercentLegacyIncidentPreferenceAdjuster(-9.9),
            FlaggedLegacyIncidentPreferenceAdjuster(flags, "legacy-incident-preference-primary", 9.6),
            TieredLegacyIncidentPreferenceAdjuster(
                mutableListOf<Double>(3761.0, 7699.0),
                mutableListOf<Double>(1.06, 0.99),
            ),
            RoundingLegacyIncidentPreferenceAdjuster(0.1),
            MinimumLegacyIncidentPreferenceAdjuster(18.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 38178.6,
    )
}
