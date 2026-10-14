package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor preference calculation. */
interface EmissionFactorPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorPreference, value: Double): Double
}

data class FlaggedEmissionFactorPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionEmissionFactorPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorPreferenceAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: EmissionFactorPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapEmissionFactorPreferenceAdjuster(var cap: Double) : EmissionFactorPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorPreference, value: Double): Double = minOf(value, cap)
}

data class TieredEmissionFactorPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingEmissionFactorPreferenceAdjuster(var step: Double) : EmissionFactorPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorPreference, value: Double): Double = roundTo(value, step)
}

data class PercentEmissionFactorPreferenceAdjuster(var percent: Double) : EmissionFactorPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the emission factor preference steps in order and keeps a trace of each. */
data class EmissionFactorPreferenceEngine(
    var steps: MutableList<EmissionFactorPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EmissionFactorPreference): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEmissionFactorPreferenceEngine(flags: Registry): EmissionFactorPreferenceEngine {
    return EmissionFactorPreferenceEngine(
        steps = mutableListOf<EmissionFactorPreferenceAdjuster>(
            FlaggedEmissionFactorPreferenceAdjuster(flags, "emission-factor-preference-rolling", 0.4),
            RegionEmissionFactorPreferenceAdjuster(
                mutableMapOf<String, Double>("south" to 1.15, "west" to 0.83),
            ),
            CapEmissionFactorPreferenceAdjuster(19096.0),
            TieredEmissionFactorPreferenceAdjuster(
                mutableListOf<Double>(3676.0, 5558.0, 8481.0),
                mutableListOf<Double>(1.15, 0.99, 1.04),
            ),
            RoundingEmissionFactorPreferenceAdjuster(0.5),
            PercentEmissionFactorPreferenceAdjuster(2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 28898.3,
    )
}
