package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched sortation forecast calculation. */
interface BatchedSortationForecastStep {
    fun describe(): String
    fun adjust(item: BatchedSortationForecast, value: Double): Double
}

data class FlaggedBatchedSortationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedSortationForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedSortationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedSortationForecastAdjuster(var step: Double) : BatchedSortationForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedSortationForecast, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedSortationForecastAdjuster(var minimum: Double) : BatchedSortationForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedSortationForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched sortation forecast steps in order and keeps a trace of each. */
data class BatchedSortationForecastEngine(
    var steps: MutableList<BatchedSortationForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedSortationForecast): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedSortationForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedSortationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedSortationForecastEngine(flags: Registry): BatchedSortationForecastEngine {
    return BatchedSortationForecastEngine(
        steps = mutableListOf<BatchedSortationForecastStep>(
            FlaggedBatchedSortationForecastAdjuster(flags, "batched-sortation-forecast-fallback", -4.8),
            RoundingBatchedSortationForecastAdjuster(0.1),
            MinimumBatchedSortationForecastAdjuster(47.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 89571.4,
    )
}
