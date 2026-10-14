package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched hold variance calculation. */
interface BatchedHoldVarianceModifier {
    fun describe(): String
    fun adjust(item: BatchedHoldVariance, value: Double): Double
}

data class MinimumBatchedHoldVarianceAdjuster(var minimum: Double) : BatchedHoldVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedHoldVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedHoldVarianceAdjuster(var step: Double) : BatchedHoldVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedHoldVariance, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedHoldVarianceAdjuster(var percent: Double) : BatchedHoldVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedHoldVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedHoldVarianceCostOffsetAdjuster(var weight: Double) : BatchedHoldVarianceModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: BatchedHoldVariance, value: Double): Double = value + item.cost * weight
}

data class FlaggedBatchedHoldVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedHoldVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedHoldVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched hold variance steps in order and keeps a trace of each. */
data class BatchedHoldVarianceEngine(
    var steps: MutableList<BatchedHoldVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedHoldVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedHoldVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedHoldVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedHoldVarianceEngine(flags: Registry): BatchedHoldVarianceEngine {
    return BatchedHoldVarianceEngine(
        steps = mutableListOf<BatchedHoldVarianceModifier>(
            MinimumBatchedHoldVarianceAdjuster(10.2),
            RoundingBatchedHoldVarianceAdjuster(0.05),
            PercentBatchedHoldVarianceAdjuster(-7.2),
            BatchedHoldVarianceCostOffsetAdjuster(0.13),
            FlaggedBatchedHoldVarianceAdjuster(flags, "batched-hold-variance-legacy", 1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 82470.5,
    )
}
