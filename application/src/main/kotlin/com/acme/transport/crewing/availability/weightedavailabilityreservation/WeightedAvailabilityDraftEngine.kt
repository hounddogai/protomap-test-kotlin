package com.acme.transport.crewing.availability.weightedavailabilityreservation

/** One step of the weighted availability draft calculation. */
interface WeightedAvailabilityDraftModifier {
    fun describe(): String
    fun adjust(item: WeightedAvailabilityDraft, value: Double): Double
}

data class PercentWeightedAvailabilityDraftAdjuster(var percent: Double) : WeightedAvailabilityDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAvailabilityDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneWeightedAvailabilityDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedAvailabilityDraftModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: WeightedAvailabilityDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedAvailabilityDraftBudgetOffsetAdjuster(
    var weight: Double,
) : WeightedAvailabilityDraftModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: WeightedAvailabilityDraft, value: Double): Double = value + item.budget * weight
}

data class MinimumWeightedAvailabilityDraftAdjuster(var minimum: Double) : WeightedAvailabilityDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAvailabilityDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedAvailabilityDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedAvailabilityDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedAvailabilityDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted availability draft steps in order and keeps a trace of each. */
data class WeightedAvailabilityDraftEngine(
    var steps: MutableList<WeightedAvailabilityDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedAvailabilityDraft): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAvailabilityDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAvailabilityDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedAvailabilityDraftEngine(): WeightedAvailabilityDraftEngine {
    return WeightedAvailabilityDraftEngine(
        steps = mutableListOf<WeightedAvailabilityDraftModifier>(
            PercentWeightedAvailabilityDraftAdjuster(19.0),
            LaneWeightedAvailabilityDraftAdjuster(mutableMapOf<String, Double>("west" to 1.22)),
            WeightedAvailabilityDraftBudgetOffsetAdjuster(1.86),
            MinimumWeightedAvailabilityDraftAdjuster(6.4),
            TieredWeightedAvailabilityDraftAdjuster(
                mutableListOf<Double>(1740.0, 2847.0, 7438.0),
                mutableListOf<Double>(1.12, 0.84, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 83157.8,
    )
}
