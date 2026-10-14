package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched parcel forecast calculation. */
interface BatchedParcelForecastFactor {
    fun describe(): String
    fun adjust(item: BatchedParcelForecast, value: Double): Double
}

data class RoundingBatchedParcelForecastAdjuster(var step: Double) : BatchedParcelForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedParcelForecast, value: Double): Double = roundTo(value, step)
}

data class BatchedParcelForecastAmountOffsetAdjuster(var weight: Double) : BatchedParcelForecastFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: BatchedParcelForecast, value: Double): Double = value + item.amount * weight
}

data class MinimumBatchedParcelForecastAdjuster(var minimum: Double) : BatchedParcelForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedParcelForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredBatchedParcelForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedParcelForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedParcelForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedParcelForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedParcelForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedParcelForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleBatchedParcelForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedParcelForecastFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedParcelForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched parcel forecast steps in order and keeps a trace of each. */
data class BatchedParcelForecastEngine(
    var steps: MutableList<BatchedParcelForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedParcelForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedParcelForecastFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedParcelForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedParcelForecastEngine(flags: Registry): BatchedParcelForecastEngine {
    return BatchedParcelForecastEngine(
        steps = mutableListOf<BatchedParcelForecastFactor>(
            RoundingBatchedParcelForecastAdjuster(0.05),
            BatchedParcelForecastAmountOffsetAdjuster(1.08),
            MinimumBatchedParcelForecastAdjuster(30.2),
            TieredBatchedParcelForecastAdjuster(
                mutableListOf<Double>(5978.0, 8342.0, 9025.0),
                mutableListOf<Double>(1.1, 1.17, 1.18),
            ),
            FlaggedBatchedParcelForecastAdjuster(flags, "batched-parcel-forecast-tiered", -6.6),
            TitleBatchedParcelForecastAdjuster(mutableMapOf<String, Double>("south" to 1.15)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 67182.2,
    )
}
