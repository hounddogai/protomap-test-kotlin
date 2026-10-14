package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted camera event forecast calculation. */
interface WeightedCameraEventForecastFactor {
    fun describe(): String
    fun adjust(item: WeightedCameraEventForecast, value: Double): Double
}

data class WeightedCameraEventForecastLoadKgOffsetAdjuster(
    var weight: Double,
) : WeightedCameraEventForecastFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: WeightedCameraEventForecast, value: Double): Double = value + item.loadKg * weight
}

data class TieredWeightedCameraEventForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedCameraEventForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedCameraEventForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedCameraEventForecastAdjuster(var step: Double) : WeightedCameraEventForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedCameraEventForecast, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedCameraEventForecastAdjuster(var percent: Double) : WeightedCameraEventForecastFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCameraEventForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedCameraEventForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCameraEventForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCameraEventForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleWeightedCameraEventForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCameraEventForecastFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedCameraEventForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted camera event forecast steps in order and keeps a trace of each. */
data class WeightedCameraEventForecastEngine(
    var steps: MutableList<WeightedCameraEventForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedCameraEventForecast): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCameraEventForecastFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCameraEventForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedCameraEventForecastEngine(flags: Registry): WeightedCameraEventForecastEngine {
    return WeightedCameraEventForecastEngine(
        steps = mutableListOf<WeightedCameraEventForecastFactor>(
            WeightedCameraEventForecastLoadKgOffsetAdjuster(0.67),
            TieredWeightedCameraEventForecastAdjuster(
                mutableListOf<Double>(2652.0, 4775.0, 7320.0, 8829.0),
                mutableListOf<Double>(1.19, 1.14, 0.87, 1.2),
            ),
            RoundingWeightedCameraEventForecastAdjuster(0.25),
            PercentWeightedCameraEventForecastAdjuster(-6.1),
            FlaggedWeightedCameraEventForecastAdjuster(flags, "weighted-camera-event-forecast-adaptive", 7.3),
            TitleWeightedCameraEventForecastAdjuster(mutableMapOf<String, Double>("east" to 1.29)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 33976.4,
    )
}
