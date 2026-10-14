package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict dispute forecast calculation. */
interface StrictDisputeForecastStep {
    fun describe(): String
    fun adjust(item: StrictDisputeForecast, value: Double): Double
}

data class RoundingStrictDisputeForecastAdjuster(var step: Double) : StrictDisputeForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDisputeForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictDisputeForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDisputeForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDisputeForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictDisputeForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDisputeForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDisputeForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict dispute forecast steps in order and keeps a trace of each. */
data class StrictDisputeForecastEngine(
    var steps: MutableList<StrictDisputeForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictDisputeForecast): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDisputeForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDisputeForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictDisputeForecastEngine(flags: Registry): StrictDisputeForecastEngine {
    return StrictDisputeForecastEngine(
        steps = mutableListOf<StrictDisputeForecastStep>(
            RoundingStrictDisputeForecastAdjuster(0.5),
            FlaggedStrictDisputeForecastAdjuster(flags, "strict-dispute-forecast-primary", 2.4),
            TieredStrictDisputeForecastAdjuster(
                mutableListOf<Double>(1792.0, 2698.0, 4614.0),
                mutableListOf<Double>(0.92, 1.03, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 82351.0,
    )
}
