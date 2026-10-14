package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the default drop preference calculation. */
interface DefaultDropPreferenceModifier {
    fun describe(): String
    fun adjust(item: DefaultDropPreference, value: Double): Double
}

data class FlaggedDefaultDropPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultDropPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultDropPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultDropPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultDropPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultDropPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDefaultDropPreferenceAdjuster(var cap: Double) : DefaultDropPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDropPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the default drop preference steps in order and keeps a trace of each. */
data class DefaultDropPreferenceEngine(
    var steps: MutableList<DefaultDropPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultDropPreference): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDropPreferenceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDropPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultDropPreferenceEngine(flags: Registry): DefaultDropPreferenceEngine {
    return DefaultDropPreferenceEngine(
        steps = mutableListOf<DefaultDropPreferenceModifier>(
            FlaggedDefaultDropPreferenceAdjuster(flags, "default-drop-preference-fallback", 1.0),
            TieredDefaultDropPreferenceAdjuster(
                mutableListOf<Double>(5567.0, 9428.0),
                mutableListOf<Double>(1.0, 1.01),
            ),
            CapDefaultDropPreferenceAdjuster(45855.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 97324.9,
    )
}
