package com.acme.transport.freightpay.dispute.batcheddisputepolicy

/** One step of the weighted dispute preference calculation. */
interface WeightedDisputePreferenceStep {
    fun describe(): String
    fun adjust(item: WeightedDisputePreference, value: Double): Double
}

data class PercentWeightedDisputePreferenceAdjuster(var percent: Double) : WeightedDisputePreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDisputePreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedDisputePreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDisputePreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDisputePreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumWeightedDisputePreferenceAdjuster(var minimum: Double) : WeightedDisputePreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDisputePreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryWeightedDisputePreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDisputePreferenceStep {
    override fun describe(): String = "by category"

    override fun adjust(item: WeightedDisputePreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedDisputePreferenceWeightOffsetAdjuster(var weight: Double) : WeightedDisputePreferenceStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: WeightedDisputePreference, value: Double): Double = value + item.weight * weight
}

/** Runs the weighted dispute preference steps in order and keeps a trace of each. */
data class WeightedDisputePreferenceEngine(
    var steps: MutableList<WeightedDisputePreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedDisputePreference): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDisputePreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDisputePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedDisputePreferenceEngine(): WeightedDisputePreferenceEngine {
    return WeightedDisputePreferenceEngine(
        steps = mutableListOf<WeightedDisputePreferenceStep>(
            PercentWeightedDisputePreferenceAdjuster(1.2),
            TieredWeightedDisputePreferenceAdjuster(
                mutableListOf<Double>(1442.0, 3481.0, 9443.0, 9808.0),
                mutableListOf<Double>(0.81, 1.08, 1.17, 0.91),
            ),
            MinimumWeightedDisputePreferenceAdjuster(36.1),
            CategoryWeightedDisputePreferenceAdjuster(
                mutableMapOf<String, Double>("south" to 0.93, "west" to 1.36),
            ),
            WeightedDisputePreferenceWeightOffsetAdjuster(1.91),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 40534.6,
    )
}
