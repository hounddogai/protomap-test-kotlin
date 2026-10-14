package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched lease return forecast calculation. */
interface BatchedLeaseReturnForecastFactor {
    fun describe(): String
    fun adjust(item: BatchedLeaseReturnForecast, value: Double): Double
}

data class CapBatchedLeaseReturnForecastAdjuster(var cap: Double) : BatchedLeaseReturnForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedLeaseReturnForecast, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedLeaseReturnForecastAdjuster(var minimum: Double) : BatchedLeaseReturnForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedLeaseReturnForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedLeaseReturnForecastAdjuster(var step: Double) : BatchedLeaseReturnForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedLeaseReturnForecast, value: Double): Double = roundTo(value, step)
}

data class BatchedLeaseReturnForecastDiscountOffsetAdjuster(
    var weight: Double,
) : BatchedLeaseReturnForecastFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: BatchedLeaseReturnForecast, value: Double): Double = value + item.discount * weight
}

data class FlaggedBatchedLeaseReturnForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedLeaseReturnForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedLeaseReturnForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedLeaseReturnForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedLeaseReturnForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedLeaseReturnForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched lease return forecast steps in order and keeps a trace of each. */
data class BatchedLeaseReturnForecastEngine(
    var steps: MutableList<BatchedLeaseReturnForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedLeaseReturnForecast): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedLeaseReturnForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedLeaseReturnForecastEngine(flags: Registry): BatchedLeaseReturnForecastEngine {
    return BatchedLeaseReturnForecastEngine(
        steps = mutableListOf<BatchedLeaseReturnForecastFactor>(
            CapBatchedLeaseReturnForecastAdjuster(30309.0),
            MinimumBatchedLeaseReturnForecastAdjuster(33.0),
            RoundingBatchedLeaseReturnForecastAdjuster(5.0),
            BatchedLeaseReturnForecastDiscountOffsetAdjuster(1.66),
            FlaggedBatchedLeaseReturnForecastAdjuster(flags, "batched-lease-return-forecast-capped", 0.2),
            TieredBatchedLeaseReturnForecastAdjuster(
                mutableListOf<Double>(1790.0, 6272.0, 7474.0, 9611.0),
                mutableListOf<Double>(0.8, 0.81, 1.18, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 56607.1,
    )
}
