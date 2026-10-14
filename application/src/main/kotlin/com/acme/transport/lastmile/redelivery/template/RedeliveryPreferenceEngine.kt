package com.acme.transport.lastmile.redelivery.template

/** One step of the redelivery preference calculation. */
interface RedeliveryPreferenceStep {
    fun describe(): String
    fun adjust(item: RedeliveryPreference, value: Double): Double
}

data class MinimumRedeliveryPreferenceAdjuster(var minimum: Double) : RedeliveryPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRedeliveryPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RedeliveryPreferenceRatioOffsetAdjuster(var weight: Double) : RedeliveryPreferenceStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RedeliveryPreference, value: Double): Double = value + item.ratio * weight
}

/** Runs the redelivery preference steps in order and keeps a trace of each. */
data class RedeliveryPreferenceEngine(
    var steps: MutableList<RedeliveryPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RedeliveryPreference): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRedeliveryPreferenceEngine(): RedeliveryPreferenceEngine {
    return RedeliveryPreferenceEngine(
        steps = mutableListOf<RedeliveryPreferenceStep>(
            MinimumRedeliveryPreferenceAdjuster(22.4),
            TieredRedeliveryPreferenceAdjuster(
                mutableListOf<Double>(3901.0, 6688.0, 7269.0),
                mutableListOf<Double>(0.97, 1.12, 1.14),
            ),
            RedeliveryPreferenceRatioOffsetAdjuster(0.81),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 17383.0,
    )
}
