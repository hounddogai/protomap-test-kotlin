package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the dispute forecast calculation. */
interface DisputeForecastModifier {
    fun describe(): String
    fun adjust(item: DisputeForecast, value: Double): Double
}

data class FlaggedDisputeForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDisputeForecastAdjuster(var minimum: Double) : DisputeForecastModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDisputeForecastAdjuster(var cap: Double) : DisputeForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeForecast, value: Double): Double = minOf(value, cap)
}

data class PercentDisputeForecastAdjuster(var percent: Double) : DisputeForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneDisputeForecastAdjuster(var factors: MutableMap<String, Double>) : DisputeForecastModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: DisputeForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDisputeForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the dispute forecast steps in order and keeps a trace of each. */
data class DisputeForecastEngine(
    var steps: MutableList<DisputeForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DisputeForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDisputeForecastEngine(flags: Registry): DisputeForecastEngine {
    return DisputeForecastEngine(
        steps = mutableListOf<DisputeForecastModifier>(
            FlaggedDisputeForecastAdjuster(flags, "dispute-forecast-batched", 0.9),
            MinimumDisputeForecastAdjuster(19.9),
            CapDisputeForecastAdjuster(44353.0),
            PercentDisputeForecastAdjuster(3.2),
            LaneDisputeForecastAdjuster(mutableMapOf<String, Double>("retail" to 1.27)),
            TieredDisputeForecastAdjuster(
                mutableListOf<Double>(3008.0, 6241.0, 8406.0, 9506.0),
                mutableListOf<Double>(0.93, 1.01, 0.91, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 53250.3,
    )
}
