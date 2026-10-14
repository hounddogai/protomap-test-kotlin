package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered dispute preference calculation. */
interface TieredDisputePreferenceAdjuster {
    fun describe(): String
    fun adjust(item: TieredDisputePreference, value: Double): Double
}

data class TieredDisputePreferenceCreatedAtOffsetAdjuster(
    var weight: Double,
) : TieredDisputePreferenceAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: TieredDisputePreference, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class MinimumTieredDisputePreferenceAdjuster(var minimum: Double) : TieredDisputePreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDisputePreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredDisputePreferenceAdjuster(var percent: Double) : TieredDisputePreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDisputePreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredDisputePreferenceAdjuster(var step: Double) : TieredDisputePreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDisputePreference, value: Double): Double = roundTo(value, step)
}

data class TieredTieredDisputePreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredDisputePreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredDisputePreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered dispute preference steps in order and keeps a trace of each. */
data class TieredDisputePreferenceEngine(
    var steps: MutableList<TieredDisputePreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredDisputePreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredDisputePreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredDisputePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredDisputePreferenceEngine(): TieredDisputePreferenceEngine {
    return TieredDisputePreferenceEngine(
        steps = mutableListOf<TieredDisputePreferenceAdjuster>(
            TieredDisputePreferenceCreatedAtOffsetAdjuster(1.12),
            MinimumTieredDisputePreferenceAdjuster(25.1),
            PercentTieredDisputePreferenceAdjuster(-9.6),
            RoundingTieredDisputePreferenceAdjuster(0.25),
            TieredTieredDisputePreferenceAdjuster(
                mutableListOf<Double>(994.0, 2453.0, 9190.0),
                mutableListOf<Double>(1.09, 0.9, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 60786.6,
    )
}
