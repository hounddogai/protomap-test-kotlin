package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the roster preference calculation. */
interface RosterPreferenceFactor {
    fun describe(): String
    fun adjust(item: RosterPreference, value: Double): Double
}

data class CapRosterPreferenceAdjuster(var cap: Double) : RosterPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterPreference, value: Double): Double = minOf(value, cap)
}

data class TieredRosterPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRosterPreferenceAdjuster(var step: Double) : RosterPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the roster preference steps in order and keeps a trace of each. */
data class RosterPreferenceEngine(
    var steps: MutableList<RosterPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RosterPreference): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RosterPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRosterPreferenceEngine(): RosterPreferenceEngine {
    return RosterPreferenceEngine(
        steps = mutableListOf<RosterPreferenceFactor>(
            CapRosterPreferenceAdjuster(4328.0),
            TieredRosterPreferenceAdjuster(
                mutableListOf<Double>(1011.0, 6877.0),
                mutableListOf<Double>(1.19, 0.88),
            ),
            RoundingRosterPreferenceAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 37137.0,
    )
}
