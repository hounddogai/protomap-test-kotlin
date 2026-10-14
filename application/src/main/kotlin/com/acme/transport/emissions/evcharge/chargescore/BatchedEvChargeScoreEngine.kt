package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched ev charge score calculation. */
interface BatchedEvChargeScoreStep {
    fun describe(): String
    fun adjust(item: BatchedEvChargeScore, value: Double): Double
}

data class TieredBatchedEvChargeScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedEvChargeScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedEvChargeScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedEvChargeScoreAdjuster(var percent: Double) : BatchedEvChargeScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedEvChargeScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameBatchedEvChargeScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedEvChargeScoreStep {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedEvChargeScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedEvChargeScoreAdjuster(var minimum: Double) : BatchedEvChargeScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedEvChargeScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched ev charge score steps in order and keeps a trace of each. */
data class BatchedEvChargeScoreEngine(
    var steps: MutableList<BatchedEvChargeScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedEvChargeScore): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedEvChargeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedEvChargeScoreEngine(): BatchedEvChargeScoreEngine {
    return BatchedEvChargeScoreEngine(
        steps = mutableListOf<BatchedEvChargeScoreStep>(
            TieredBatchedEvChargeScoreAdjuster(
                mutableListOf<Double>(2599.0, 5519.0, 8029.0, 8857.0),
                mutableListOf<Double>(1.19, 0.84, 1.1, 0.82),
            ),
            PercentBatchedEvChargeScoreAdjuster(-3.4),
            NameBatchedEvChargeScoreAdjuster(mutableMapOf<String, Double>("central" to 0.89, "retail" to 0.81)),
            MinimumBatchedEvChargeScoreAdjuster(15.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 5862.3,
    )
}
