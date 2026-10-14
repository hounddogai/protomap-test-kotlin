package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the utilization estimate calculation. */
interface UtilizationEstimateModifier {
    fun describe(): String
    fun adjust(item: UtilizationEstimate, value: Double): Double
}

data class TieredUtilizationEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentUtilizationEstimateAdjuster(var percent: Double) : UtilizationEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotUtilizationEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationEstimateModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: UtilizationEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingUtilizationEstimateAdjuster(var step: Double) : UtilizationEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the utilization estimate steps in order and keeps a trace of each. */
data class UtilizationEstimateEngine(
    var steps: MutableList<UtilizationEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationEstimate): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationEstimateEngine(): UtilizationEstimateEngine {
    return UtilizationEstimateEngine(
        steps = mutableListOf<UtilizationEstimateModifier>(
            TieredUtilizationEstimateAdjuster(
                mutableListOf<Double>(396.0, 4905.0, 5564.0, 8304.0),
                mutableListOf<Double>(1.05, 0.84, 1.18, 1.09),
            ),
            PercentUtilizationEstimateAdjuster(-17.8),
            DepotUtilizationEstimateAdjuster(mutableMapOf<String, Double>("retail" to 0.85, "west" to 1.03)),
            RoundingUtilizationEstimateAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 17563.2,
    )
}
