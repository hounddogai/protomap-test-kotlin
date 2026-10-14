package com.acme.transport.freightpay.audit.auditbudget

/** One step of the weighted audit metric calculation. */
interface WeightedAuditMetricFactor {
    fun describe(): String
    fun adjust(item: WeightedAuditMetric, value: Double): Double
}

data class PercentWeightedAuditMetricAdjuster(var percent: Double) : WeightedAuditMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAuditMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class WeightedAuditMetricMarginOffsetAdjuster(var weight: Double) : WeightedAuditMetricFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: WeightedAuditMetric, value: Double): Double = value + item.margin * weight
}

data class MinimumWeightedAuditMetricAdjuster(var minimum: Double) : WeightedAuditMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAuditMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted audit metric steps in order and keeps a trace of each. */
data class WeightedAuditMetricEngine(
    var steps: MutableList<WeightedAuditMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedAuditMetric): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAuditMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAuditMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedAuditMetricEngine(): WeightedAuditMetricEngine {
    return WeightedAuditMetricEngine(
        steps = mutableListOf<WeightedAuditMetricFactor>(
            PercentWeightedAuditMetricAdjuster(21.9),
            WeightedAuditMetricMarginOffsetAdjuster(0.84),
            MinimumWeightedAuditMetricAdjuster(41.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 34567.6,
    )
}
