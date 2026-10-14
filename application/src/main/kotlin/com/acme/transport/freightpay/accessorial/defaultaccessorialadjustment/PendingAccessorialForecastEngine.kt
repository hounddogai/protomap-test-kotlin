package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending accessorial forecast calculation. */
interface PendingAccessorialForecastStep {
    fun describe(): String
    fun adjust(item: PendingAccessorialForecast, value: Double): Double
}

data class PercentPendingAccessorialForecastAdjuster(var percent: Double) : PendingAccessorialForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingAccessorialForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPendingAccessorialForecastAdjuster(var step: Double) : PendingAccessorialForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAccessorialForecast, value: Double): Double = roundTo(value, step)
}

data class TieredPendingAccessorialForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingAccessorialForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingAccessorialForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPendingAccessorialForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingAccessorialForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingAccessorialForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending accessorial forecast steps in order and keeps a trace of each. */
data class PendingAccessorialForecastEngine(
    var steps: MutableList<PendingAccessorialForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingAccessorialForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingAccessorialForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingAccessorialForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingAccessorialForecastEngine(flags: Registry): PendingAccessorialForecastEngine {
    return PendingAccessorialForecastEngine(
        steps = mutableListOf<PendingAccessorialForecastStep>(
            PercentPendingAccessorialForecastAdjuster(19.2),
            RoundingPendingAccessorialForecastAdjuster(0.1),
            TieredPendingAccessorialForecastAdjuster(
                mutableListOf<Double>(2714.0, 4219.0, 9906.0),
                mutableListOf<Double>(0.97, 0.84, 0.9),
            ),
            FlaggedPendingAccessorialForecastAdjuster(flags, "pending-accessorial-forecast-strict", 7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 69038.2,
    )
}
