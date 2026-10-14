package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary redelivery preference calculation. */
interface PrimaryRedeliveryPreferenceStep {
    fun describe(): String
    fun adjust(item: PrimaryRedeliveryPreference, value: Double): Double
}

data class NamePrimaryRedeliveryPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryRedeliveryPreferenceStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryRedeliveryPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPrimaryRedeliveryPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryRedeliveryPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryRedeliveryPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryRedeliveryPreferenceAdjuster(var step: Double) : PrimaryRedeliveryPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryRedeliveryPreference, value: Double): Double = roundTo(value, step)
}

data class PercentPrimaryRedeliveryPreferenceAdjuster(var percent: Double) : PrimaryRedeliveryPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryRedeliveryPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary redelivery preference steps in order and keeps a trace of each. */
data class PrimaryRedeliveryPreferenceEngine(
    var steps: MutableList<PrimaryRedeliveryPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryRedeliveryPreference): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryRedeliveryPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryRedeliveryPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryRedeliveryPreferenceEngine(): PrimaryRedeliveryPreferenceEngine {
    return PrimaryRedeliveryPreferenceEngine(
        steps = mutableListOf<PrimaryRedeliveryPreferenceStep>(
            NamePrimaryRedeliveryPreferenceAdjuster(mutableMapOf<String, Double>("central" to 0.74)),
            TieredPrimaryRedeliveryPreferenceAdjuster(
                mutableListOf<Double>(1963.0, 3905.0, 4658.0),
                mutableListOf<Double>(0.86, 0.91, 1.15),
            ),
            RoundingPrimaryRedeliveryPreferenceAdjuster(0.1),
            PercentPrimaryRedeliveryPreferenceAdjuster(16.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 38740.2,
    )
}
