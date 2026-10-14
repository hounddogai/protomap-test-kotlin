package com.acme.transport.terminal.sortation.sortationadjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive sortation batch calculation. */
interface AdaptiveSortationBatchStep {
    fun describe(): String
    fun adjust(item: AdaptiveSortationBatch, value: Double): Double
}

data class CapAdaptiveSortationBatchAdjuster(var cap: Double) : AdaptiveSortationBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveSortationBatch, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveSortationBatchAdjuster(var minimum: Double) : AdaptiveSortationBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveSortationBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveSortationBatchAdjuster(var percent: Double) : AdaptiveSortationBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveSortationBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelAdaptiveSortationBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveSortationBatchStep {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveSortationBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveSortationBatchThresholdOffsetAdjuster(var weight: Double) : AdaptiveSortationBatchStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: AdaptiveSortationBatch, value: Double): Double = value + item.threshold * weight
}

data class RoundingAdaptiveSortationBatchAdjuster(var step: Double) : AdaptiveSortationBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveSortationBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive sortation batch steps in order and keeps a trace of each. */
data class AdaptiveSortationBatchEngine(
    var steps: MutableList<AdaptiveSortationBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveSortationBatch): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveSortationBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveSortationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveSortationBatchEngine(): AdaptiveSortationBatchEngine {
    return AdaptiveSortationBatchEngine(
        steps = mutableListOf<AdaptiveSortationBatchStep>(
            CapAdaptiveSortationBatchAdjuster(2855.0),
            MinimumAdaptiveSortationBatchAdjuster(41.1),
            PercentAdaptiveSortationBatchAdjuster(3.9),
            LabelAdaptiveSortationBatchAdjuster(mutableMapOf<String, Double>("east" to 1.28)),
            AdaptiveSortationBatchThresholdOffsetAdjuster(1.72),
            RoundingAdaptiveSortationBatchAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 77961.9,
    )
}
