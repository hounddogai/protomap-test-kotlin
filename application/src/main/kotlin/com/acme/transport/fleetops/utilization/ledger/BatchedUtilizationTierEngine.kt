package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched utilization tier calculation. */
interface BatchedUtilizationTierModifier {
    fun describe(): String
    fun adjust(item: BatchedUtilizationTier, value: Double): Double
}

data class RoundingBatchedUtilizationTierAdjuster(var step: Double) : BatchedUtilizationTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedUtilizationTier, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedUtilizationTierAdjuster(var minimum: Double) : BatchedUtilizationTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedUtilizationTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredBatchedUtilizationTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedUtilizationTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedUtilizationTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelBatchedUtilizationTierAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedUtilizationTierModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedUtilizationTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched utilization tier steps in order and keeps a trace of each. */
data class BatchedUtilizationTierEngine(
    var steps: MutableList<BatchedUtilizationTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedUtilizationTier): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedUtilizationTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedUtilizationTierEngine(): BatchedUtilizationTierEngine {
    return BatchedUtilizationTierEngine(
        steps = mutableListOf<BatchedUtilizationTierModifier>(
            RoundingBatchedUtilizationTierAdjuster(0.25),
            MinimumBatchedUtilizationTierAdjuster(6.3),
            TieredBatchedUtilizationTierAdjuster(
                mutableListOf<Double>(3575.0, 7032.0, 8751.0),
                mutableListOf<Double>(1.13, 0.9, 0.86),
            ),
            LabelBatchedUtilizationTierAdjuster(mutableMapOf<String, Double>("online" to 0.73)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 44230.5,
    )
}
