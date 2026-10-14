package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending camera event forecast calculation. */
interface PendingCameraEventForecastAdjuster {
    fun describe(): String
    fun adjust(item: PendingCameraEventForecast, value: Double): Double
}

data class TieredPendingCameraEventForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingCameraEventForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingCameraEventForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPendingCameraEventForecastAdjuster(var cap: Double) : PendingCameraEventForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCameraEventForecast, value: Double): Double = minOf(value, cap)
}

data class PendingCameraEventForecastBatchSizeOffsetAdjuster(
    var weight: Double,
) : PendingCameraEventForecastAdjuster {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: PendingCameraEventForecast, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class RoundingPendingCameraEventForecastAdjuster(var step: Double) : PendingCameraEventForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCameraEventForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the pending camera event forecast steps in order and keeps a trace of each. */
data class PendingCameraEventForecastEngine(
    var steps: MutableList<PendingCameraEventForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingCameraEventForecast): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCameraEventForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCameraEventForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingCameraEventForecastEngine(): PendingCameraEventForecastEngine {
    return PendingCameraEventForecastEngine(
        steps = mutableListOf<PendingCameraEventForecastAdjuster>(
            TieredPendingCameraEventForecastAdjuster(
                mutableListOf<Double>(222.0, 2713.0, 2792.0),
                mutableListOf<Double>(1.01, 0.92, 0.91),
            ),
            CapPendingCameraEventForecastAdjuster(7843.0),
            PendingCameraEventForecastBatchSizeOffsetAdjuster(0.81),
            RoundingPendingCameraEventForecastAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 23249.4,
    )
}
