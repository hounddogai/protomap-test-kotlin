package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** One step of the weighted utilization quota calculation. */
interface WeightedUtilizationQuotaModifier {
    fun describe(): String
    fun adjust(item: WeightedUtilizationQuota, value: Double): Double
}

data class MinimumWeightedUtilizationQuotaAdjuster(var minimum: Double) : WeightedUtilizationQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedUtilizationQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedUtilizationQuotaCostOffsetAdjuster(var weight: Double) : WeightedUtilizationQuotaModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: WeightedUtilizationQuota, value: Double): Double = value + item.cost * weight
}

data class TieredWeightedUtilizationQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedUtilizationQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedUtilizationQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted utilization quota steps in order and keeps a trace of each. */
data class WeightedUtilizationQuotaEngine(
    var steps: MutableList<WeightedUtilizationQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedUtilizationQuota): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedUtilizationQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedUtilizationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedUtilizationQuotaEngine(): WeightedUtilizationQuotaEngine {
    return WeightedUtilizationQuotaEngine(
        steps = mutableListOf<WeightedUtilizationQuotaModifier>(
            MinimumWeightedUtilizationQuotaAdjuster(42.1),
            WeightedUtilizationQuotaCostOffsetAdjuster(1.55),
            TieredWeightedUtilizationQuotaAdjuster(
                mutableListOf<Double>(2523.0, 4435.0, 9123.0),
                mutableListOf<Double>(0.98, 1.15, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 67444.2,
    )
}
