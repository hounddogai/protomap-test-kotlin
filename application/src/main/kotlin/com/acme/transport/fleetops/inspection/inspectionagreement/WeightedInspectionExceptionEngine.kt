package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted inspection exception calculation. */
interface WeightedInspectionExceptionStep {
    fun describe(): String
    fun adjust(item: WeightedInspectionException, value: Double): Double
}

data class LaneWeightedInspectionExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedInspectionExceptionStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: WeightedInspectionException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedInspectionExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedInspectionExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedInspectionException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedInspectionExceptionAdjuster(var step: Double) : WeightedInspectionExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedInspectionException, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted inspection exception steps in order and keeps a trace of each. */
data class WeightedInspectionExceptionEngine(
    var steps: MutableList<WeightedInspectionExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedInspectionException): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedInspectionExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedInspectionException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedInspectionExceptionEngine(): WeightedInspectionExceptionEngine {
    return WeightedInspectionExceptionEngine(
        steps = mutableListOf<WeightedInspectionExceptionStep>(
            LaneWeightedInspectionExceptionAdjuster(
                mutableMapOf<String, Double>("retail" to 0.84, "online" to 0.99),
            ),
            TieredWeightedInspectionExceptionAdjuster(
                mutableListOf<Double>(7773.0, 7917.0),
                mutableListOf<Double>(1.13, 1.05),
            ),
            RoundingWeightedInspectionExceptionAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 30875.6,
    )
}
