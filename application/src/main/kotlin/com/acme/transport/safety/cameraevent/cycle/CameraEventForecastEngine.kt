package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the camera event forecast calculation. */
interface CameraEventForecastAdjuster {
    fun describe(): String
    fun adjust(item: CameraEventForecast, value: Double): Double
}

data class FlaggedCameraEventForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CameraEventForecastDiscountOffsetAdjuster(var weight: Double) : CameraEventForecastAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: CameraEventForecast, value: Double): Double = value + item.discount * weight
}

data class LabelCameraEventForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : CameraEventForecastAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: CameraEventForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCameraEventForecastAdjuster(var percent: Double) : CameraEventForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCameraEventForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the camera event forecast steps in order and keeps a trace of each. */
data class CameraEventForecastEngine(
    var steps: MutableList<CameraEventForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventForecast): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventForecastEngine(flags: Registry): CameraEventForecastEngine {
    return CameraEventForecastEngine(
        steps = mutableListOf<CameraEventForecastAdjuster>(
            FlaggedCameraEventForecastAdjuster(flags, "camera-event-forecast-manual", 7.2),
            CameraEventForecastDiscountOffsetAdjuster(0.79),
            LabelCameraEventForecastAdjuster(mutableMapOf<String, Double>("online" to 0.89)),
            PercentCameraEventForecastAdjuster(17.4),
            TieredCameraEventForecastAdjuster(
                mutableListOf<Double>(5478.0, 6312.0),
                mutableListOf<Double>(0.94, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 29190.7,
    )
}
