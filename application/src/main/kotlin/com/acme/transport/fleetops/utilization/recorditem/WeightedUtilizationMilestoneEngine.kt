package com.acme.transport.fleetops.utilization.recorditem

/** One step of the weighted utilization milestone calculation. */
interface WeightedUtilizationMilestoneStep {
    fun describe(): String
    fun adjust(item: WeightedUtilizationMilestone, value: Double): Double
}

data class CapWeightedUtilizationMilestoneAdjuster(var cap: Double) : WeightedUtilizationMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedUtilizationMilestone, value: Double): Double = minOf(value, cap)
}

data class PercentWeightedUtilizationMilestoneAdjuster(var percent: Double) : WeightedUtilizationMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedUtilizationMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class WeightedUtilizationMilestoneVersionOffsetAdjuster(
    var weight: Double,
) : WeightedUtilizationMilestoneStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: WeightedUtilizationMilestone, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class MinimumWeightedUtilizationMilestoneAdjuster(var minimum: Double) : WeightedUtilizationMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedUtilizationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedUtilizationMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedUtilizationMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedUtilizationMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceWeightedUtilizationMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedUtilizationMilestoneStep {
    override fun describe(): String = "by source"

    override fun adjust(item: WeightedUtilizationMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted utilization milestone steps in order and keeps a trace of each. */
data class WeightedUtilizationMilestoneEngine(
    var steps: MutableList<WeightedUtilizationMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedUtilizationMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedUtilizationMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedUtilizationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedUtilizationMilestoneEngine(): WeightedUtilizationMilestoneEngine {
    return WeightedUtilizationMilestoneEngine(
        steps = mutableListOf<WeightedUtilizationMilestoneStep>(
            CapWeightedUtilizationMilestoneAdjuster(17386.0),
            PercentWeightedUtilizationMilestoneAdjuster(-18.4),
            WeightedUtilizationMilestoneVersionOffsetAdjuster(0.08),
            MinimumWeightedUtilizationMilestoneAdjuster(10.2),
            TieredWeightedUtilizationMilestoneAdjuster(
                mutableListOf<Double>(1847.0, 3804.0, 4097.0, 8050.0),
                mutableListOf<Double>(1.2, 1.13, 1.05, 1.14),
            ),
            SourceWeightedUtilizationMilestoneAdjuster(
                mutableMapOf<String, Double>("west" to 1.23, "retail" to 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 80482.7,
    )
}
