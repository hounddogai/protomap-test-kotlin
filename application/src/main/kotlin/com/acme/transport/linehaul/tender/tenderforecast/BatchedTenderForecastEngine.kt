package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched tender forecast calculation. */
interface BatchedTenderForecastStep {
    fun describe(): String
    fun adjust(item: BatchedTenderForecast, value: Double): Double
}

data class PercentBatchedTenderForecastAdjuster(var percent: Double) : BatchedTenderForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTenderForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedTenderForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedTenderForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedTenderForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedTenderForecastStartsAtOffsetAdjuster(var weight: Double) : BatchedTenderForecastStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: BatchedTenderForecast, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class TieredBatchedTenderForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedTenderForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedTenderForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBatchedTenderForecastAdjuster(var step: Double) : BatchedTenderForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTenderForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the batched tender forecast steps in order and keeps a trace of each. */
data class BatchedTenderForecastEngine(
    var steps: MutableList<BatchedTenderForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedTenderForecast): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTenderForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTenderForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedTenderForecastEngine(flags: Registry): BatchedTenderForecastEngine {
    return BatchedTenderForecastEngine(
        steps = mutableListOf<BatchedTenderForecastStep>(
            PercentBatchedTenderForecastAdjuster(-12.2),
            FlaggedBatchedTenderForecastAdjuster(flags, "batched-tender-forecast-global", -7.1),
            BatchedTenderForecastStartsAtOffsetAdjuster(1.39),
            TieredBatchedTenderForecastAdjuster(
                mutableListOf<Double>(1642.0, 4713.0, 5419.0, 6841.0),
                mutableListOf<Double>(0.95, 1.19, 0.95, 1.1),
            ),
            RoundingBatchedTenderForecastAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 90087.6,
    )
}
