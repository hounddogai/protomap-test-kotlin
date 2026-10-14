package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched carbon report policy calculation. */
interface BatchedCarbonReportPolicyAdjuster {
    fun describe(): String
    fun adjust(item: BatchedCarbonReportPolicy, value: Double): Double
}

data class MinimumBatchedCarbonReportPolicyAdjuster(var minimum: Double) : BatchedCarbonReportPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCarbonReportPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelBatchedCarbonReportPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedCarbonReportPolicyAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedCarbonReportPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBatchedCarbonReportPolicyAdjuster(var step: Double) : BatchedCarbonReportPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCarbonReportPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the batched carbon report policy steps in order and keeps a trace of each. */
data class BatchedCarbonReportPolicyEngine(
    var steps: MutableList<BatchedCarbonReportPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedCarbonReportPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedCarbonReportPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedCarbonReportPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedCarbonReportPolicyEngine(): BatchedCarbonReportPolicyEngine {
    return BatchedCarbonReportPolicyEngine(
        steps = mutableListOf<BatchedCarbonReportPolicyAdjuster>(
            MinimumBatchedCarbonReportPolicyAdjuster(49.2),
            LabelBatchedCarbonReportPolicyAdjuster(mutableMapOf<String, Double>("west" to 1.12)),
            RoundingBatchedCarbonReportPolicyAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 94597.6,
    )
}
