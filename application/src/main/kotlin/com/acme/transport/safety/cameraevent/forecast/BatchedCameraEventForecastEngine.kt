package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched camera event forecast calculation. */
interface BatchedCameraEventForecastFactor {
    fun describe(): String
    fun adjust(item: BatchedCameraEventForecast, value: Double): Double
}

data class FlaggedBatchedCameraEventForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedCameraEventForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedCameraEventForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedCameraEventForecastAdjuster(var cap: Double) : BatchedCameraEventForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCameraEventForecast, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedCameraEventForecastAdjuster(var percent: Double) : BatchedCameraEventForecastFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCameraEventForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedCameraEventForecastAdjuster(var minimum: Double) : BatchedCameraEventForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCameraEventForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceBatchedCameraEventForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedCameraEventForecastFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: BatchedCameraEventForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedCameraEventForecastStopCountOffsetAdjuster(
    var weight: Double,
) : BatchedCameraEventForecastFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: BatchedCameraEventForecast, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the batched camera event forecast steps in order and keeps a trace of each. */
data class BatchedCameraEventForecastEngine(
    var steps: MutableList<BatchedCameraEventForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedCameraEventForecast): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCameraEventForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedCameraEventForecastEngine(flags: Registry): BatchedCameraEventForecastEngine {
    return BatchedCameraEventForecastEngine(
        steps = mutableListOf<BatchedCameraEventForecastFactor>(
            FlaggedBatchedCameraEventForecastAdjuster(flags, "batched-camera-event-forecast-capped", -4.3),
            CapBatchedCameraEventForecastAdjuster(44562.0),
            PercentBatchedCameraEventForecastAdjuster(-7.4),
            MinimumBatchedCameraEventForecastAdjuster(41.5),
            SourceBatchedCameraEventForecastAdjuster(
                mutableMapOf<String, Double>("central" to 1.4, "retail" to 0.99),
            ),
            BatchedCameraEventForecastStopCountOffsetAdjuster(1.61),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 71217.2,
    )
}
