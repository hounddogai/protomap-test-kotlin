package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time preference calculation. */
interface TransitTimePreferenceFactor {
    fun describe(): String
    fun adjust(item: TransitTimePreference, value: Double): Double
}

data class FlaggedTransitTimePreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimePreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimePreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTransitTimePreferenceAdjuster(var step: Double) : TransitTimePreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimePreference, value: Double): Double = roundTo(value, step)
}

data class PercentTransitTimePreferenceAdjuster(var percent: Double) : TransitTimePreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimePreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the transit time preference steps in order and keeps a trace of each. */
data class TransitTimePreferenceEngine(
    var steps: MutableList<TransitTimePreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimePreference): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimePreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimePreferenceEngine(flags: Registry): TransitTimePreferenceEngine {
    return TransitTimePreferenceEngine(
        steps = mutableListOf<TransitTimePreferenceFactor>(
            FlaggedTransitTimePreferenceAdjuster(flags, "transit-time-preference-primary", -2.7),
            RoundingTransitTimePreferenceAdjuster(0.25),
            PercentTransitTimePreferenceAdjuster(5.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 94164.4,
    )
}
