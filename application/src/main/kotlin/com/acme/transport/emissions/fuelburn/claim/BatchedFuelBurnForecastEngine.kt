package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched fuel burn forecast calculation. */
interface BatchedFuelBurnForecastFactor {
    fun describe(): String
    fun adjust(item: BatchedFuelBurnForecast, value: Double): Double
}

data class PercentBatchedFuelBurnForecastAdjuster(var percent: Double) : BatchedFuelBurnForecastFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedFuelBurnForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedFuelBurnForecastAdjuster(var step: Double) : BatchedFuelBurnForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedFuelBurnForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedFuelBurnForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedFuelBurnForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedFuelBurnForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedFuelBurnForecastWindowDaysOffsetAdjuster(var weight: Double) : BatchedFuelBurnForecastFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: BatchedFuelBurnForecast, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the batched fuel burn forecast steps in order and keeps a trace of each. */
data class BatchedFuelBurnForecastEngine(
    var steps: MutableList<BatchedFuelBurnForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedFuelBurnForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedFuelBurnForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedFuelBurnForecastEngine(flags: Registry): BatchedFuelBurnForecastEngine {
    return BatchedFuelBurnForecastEngine(
        steps = mutableListOf<BatchedFuelBurnForecastFactor>(
            PercentBatchedFuelBurnForecastAdjuster(-11.3),
            RoundingBatchedFuelBurnForecastAdjuster(0.05),
            FlaggedBatchedFuelBurnForecastAdjuster(flags, "batched-fuel-burn-forecast-scheduled", -2.2),
            BatchedFuelBurnForecastWindowDaysOffsetAdjuster(0.04),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 18067.8,
    )
}
