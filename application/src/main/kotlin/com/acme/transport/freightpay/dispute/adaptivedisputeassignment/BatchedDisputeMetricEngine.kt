package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

/** One step of the batched dispute metric calculation. */
interface BatchedDisputeMetricStep {
    fun describe(): String
    fun adjust(item: BatchedDisputeMetric, value: Double): Double
}

data class MinimumBatchedDisputeMetricAdjuster(var minimum: Double) : BatchedDisputeMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDisputeMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredBatchedDisputeMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDisputeMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDisputeMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedDisputeMetricAdjuster(var percent: Double) : BatchedDisputeMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDisputeMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched dispute metric steps in order and keeps a trace of each. */
data class BatchedDisputeMetricEngine(
    var steps: MutableList<BatchedDisputeMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedDisputeMetric): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDisputeMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedDisputeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedDisputeMetricEngine(): BatchedDisputeMetricEngine {
    return BatchedDisputeMetricEngine(
        steps = mutableListOf<BatchedDisputeMetricStep>(
            MinimumBatchedDisputeMetricAdjuster(30.6),
            TieredBatchedDisputeMetricAdjuster(
                mutableListOf<Double>(5725.0, 7620.0, 8471.0, 9729.0),
                mutableListOf<Double>(1.19, 1.03, 0.89, 0.81),
            ),
            PercentBatchedDisputeMetricAdjuster(23.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 27431.9,
    )
}
