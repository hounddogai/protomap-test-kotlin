package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break preference calculation. */
interface RestBreakPreferenceStep {
    fun describe(): String
    fun adjust(item: RestBreakPreference, value: Double): Double
}

data class CapRestBreakPreferenceAdjuster(var cap: Double) : RestBreakPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakPreference, value: Double): Double = minOf(value, cap)
}

data class RoundingRestBreakPreferenceAdjuster(var step: Double) : RestBreakPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakPreference, value: Double): Double = roundTo(value, step)
}

data class PercentRestBreakPreferenceAdjuster(var percent: Double) : RestBreakPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRestBreakPreferenceAdjuster(var minimum: Double) : RestBreakPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rest break preference steps in order and keeps a trace of each. */
data class RestBreakPreferenceEngine(
    var steps: MutableList<RestBreakPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakPreferenceEngine(): RestBreakPreferenceEngine {
    return RestBreakPreferenceEngine(
        steps = mutableListOf<RestBreakPreferenceStep>(
            CapRestBreakPreferenceAdjuster(43806.0),
            RoundingRestBreakPreferenceAdjuster(0.5),
            PercentRestBreakPreferenceAdjuster(-18.3),
            MinimumRestBreakPreferenceAdjuster(3.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 98011.6,
    )
}
