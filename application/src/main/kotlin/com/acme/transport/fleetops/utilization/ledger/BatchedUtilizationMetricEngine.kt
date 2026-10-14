package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched utilization metric calculation. */
interface BatchedUtilizationMetricStep {
    fun describe(): String
    fun adjust(item: BatchedUtilizationMetric, value: Double): Double
}

data class TieredBatchedUtilizationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedUtilizationMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedUtilizationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedUtilizationMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedUtilizationMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedUtilizationMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedUtilizationMetricAdjuster(var step: Double) : BatchedUtilizationMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedUtilizationMetric, value: Double): Double = roundTo(value, step)
}

data class BatchedUtilizationMetricPalletsOffsetAdjuster(var weight: Double) : BatchedUtilizationMetricStep {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: BatchedUtilizationMetric, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class MinimumBatchedUtilizationMetricAdjuster(var minimum: Double) : BatchedUtilizationMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedUtilizationMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched utilization metric steps in order and keeps a trace of each. */
data class BatchedUtilizationMetricEngine(
    var steps: MutableList<BatchedUtilizationMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedUtilizationMetric): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedUtilizationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedUtilizationMetricEngine(flags: Registry): BatchedUtilizationMetricEngine {
    return BatchedUtilizationMetricEngine(
        steps = mutableListOf<BatchedUtilizationMetricStep>(
            TieredBatchedUtilizationMetricAdjuster(
                mutableListOf<Double>(260.0, 5818.0, 6663.0),
                mutableListOf<Double>(1.14, 1.1, 1.05),
            ),
            FlaggedBatchedUtilizationMetricAdjuster(flags, "batched-utilization-metric-batched", -0.5),
            RoundingBatchedUtilizationMetricAdjuster(0.5),
            BatchedUtilizationMetricPalletsOffsetAdjuster(1.24),
            MinimumBatchedUtilizationMetricAdjuster(4.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 67187.6,
    )
}
