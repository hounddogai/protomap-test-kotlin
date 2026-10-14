package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual accessorial forecast calculation. */
interface ManualAccessorialForecastAdjuster {
    fun describe(): String
    fun adjust(item: ManualAccessorialForecast, value: Double): Double
}

data class PercentManualAccessorialForecastAdjuster(var percent: Double) : ManualAccessorialForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAccessorialForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualAccessorialForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAccessorialForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAccessorialForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualAccessorialForecastRatioOffsetAdjuster(
    var weight: Double,
) : ManualAccessorialForecastAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ManualAccessorialForecast, value: Double): Double = value + item.ratio * weight
}

data class CapManualAccessorialForecastAdjuster(var cap: Double) : ManualAccessorialForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAccessorialForecast, value: Double): Double = minOf(value, cap)
}

data class TieredManualAccessorialForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAccessorialForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAccessorialForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingManualAccessorialForecastAdjuster(var step: Double) : ManualAccessorialForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAccessorialForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the manual accessorial forecast steps in order and keeps a trace of each. */
data class ManualAccessorialForecastEngine(
    var steps: MutableList<ManualAccessorialForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualAccessorialForecast): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualAccessorialForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualAccessorialForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualAccessorialForecastEngine(flags: Registry): ManualAccessorialForecastEngine {
    return ManualAccessorialForecastEngine(
        steps = mutableListOf<ManualAccessorialForecastAdjuster>(
            PercentManualAccessorialForecastAdjuster(-18.5),
            FlaggedManualAccessorialForecastAdjuster(flags, "manual-accessorial-forecast-weighted", -2.8),
            ManualAccessorialForecastRatioOffsetAdjuster(1.41),
            CapManualAccessorialForecastAdjuster(47095.0),
            TieredManualAccessorialForecastAdjuster(
                mutableListOf<Double>(1575.0, 2466.0, 4876.0),
                mutableListOf<Double>(1.18, 0.91, 0.98),
            ),
            RoundingManualAccessorialForecastAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 43199.4,
    )
}
