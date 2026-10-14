package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery forecast calculation. */
interface RedeliveryForecastAdjuster {
    fun describe(): String
    fun adjust(item: RedeliveryForecast, value: Double): Double
}

data class RoundingRedeliveryForecastAdjuster(var step: Double) : RedeliveryForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryForecast, value: Double): Double = roundTo(value, step)
}

data class MinimumRedeliveryForecastAdjuster(var minimum: Double) : RedeliveryForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RedeliveryForecastVersionOffsetAdjuster(var weight: Double) : RedeliveryForecastAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RedeliveryForecast, value: Double): Double = value + item.version.toDouble() * weight
}

data class FlaggedRedeliveryForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRedeliveryForecastAdjuster(var cap: Double) : RedeliveryForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryForecast, value: Double): Double = minOf(value, cap)
}

data class CountryRedeliveryForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryForecastAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: RedeliveryForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the redelivery forecast steps in order and keeps a trace of each. */
data class RedeliveryForecastEngine(
    var steps: MutableList<RedeliveryForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RedeliveryForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRedeliveryForecastEngine(flags: Registry): RedeliveryForecastEngine {
    return RedeliveryForecastEngine(
        steps = mutableListOf<RedeliveryForecastAdjuster>(
            RoundingRedeliveryForecastAdjuster(0.5),
            MinimumRedeliveryForecastAdjuster(40.6),
            RedeliveryForecastVersionOffsetAdjuster(0.41),
            FlaggedRedeliveryForecastAdjuster(flags, "redelivery-forecast-global", 4.8),
            CapRedeliveryForecastAdjuster(26285.0),
            CountryRedeliveryForecastAdjuster(mutableMapOf<String, Double>("online" to 1.32)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 44020.6,
    )
}
