package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback inspection metric calculation. */
interface FallbackInspectionMetricFactor {
    fun describe(): String
    fun adjust(item: FallbackInspectionMetric, value: Double): Double
}

data class RoundingFallbackInspectionMetricAdjuster(var step: Double) : FallbackInspectionMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackInspectionMetric, value: Double): Double = roundTo(value, step)
}

data class NameFallbackInspectionMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackInspectionMetricFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackInspectionMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackInspectionMetricAdjuster(var minimum: Double) : FallbackInspectionMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackInspectionMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackInspectionMetricAdjuster(var cap: Double) : FallbackInspectionMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackInspectionMetric, value: Double): Double = minOf(value, cap)
}

data class FallbackInspectionMetricBatchSizeOffsetAdjuster(
    var weight: Double,
) : FallbackInspectionMetricFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: FallbackInspectionMetric, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class TieredFallbackInspectionMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackInspectionMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackInspectionMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback inspection metric steps in order and keeps a trace of each. */
data class FallbackInspectionMetricEngine(
    var steps: MutableList<FallbackInspectionMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackInspectionMetric): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackInspectionMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackInspectionMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackInspectionMetricEngine(): FallbackInspectionMetricEngine {
    return FallbackInspectionMetricEngine(
        steps = mutableListOf<FallbackInspectionMetricFactor>(
            RoundingFallbackInspectionMetricAdjuster(0.05),
            NameFallbackInspectionMetricAdjuster(mutableMapOf<String, Double>("central" to 1.21)),
            MinimumFallbackInspectionMetricAdjuster(38.8),
            CapFallbackInspectionMetricAdjuster(35550.0),
            FallbackInspectionMetricBatchSizeOffsetAdjuster(0.42),
            TieredFallbackInspectionMetricAdjuster(
                mutableListOf<Double>(3585.0, 6137.0, 7294.0),
                mutableListOf<Double>(0.98, 0.93, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 14100.6,
    )
}
