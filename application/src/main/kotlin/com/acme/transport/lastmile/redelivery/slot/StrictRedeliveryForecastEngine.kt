package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict redelivery forecast calculation. */
interface StrictRedeliveryForecastFactor {
    fun describe(): String
    fun adjust(item: StrictRedeliveryForecast, value: Double): Double
}

data class CapStrictRedeliveryForecastAdjuster(var cap: Double) : StrictRedeliveryForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRedeliveryForecast, value: Double): Double = minOf(value, cap)
}

data class LocaleStrictRedeliveryForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRedeliveryForecastFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: StrictRedeliveryForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingStrictRedeliveryForecastAdjuster(var step: Double) : StrictRedeliveryForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRedeliveryForecast, value: Double): Double = roundTo(value, step)
}

data class TieredStrictRedeliveryForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRedeliveryForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRedeliveryForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict redelivery forecast steps in order and keeps a trace of each. */
data class StrictRedeliveryForecastEngine(
    var steps: MutableList<StrictRedeliveryForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictRedeliveryForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRedeliveryForecastFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRedeliveryForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictRedeliveryForecastEngine(): StrictRedeliveryForecastEngine {
    return StrictRedeliveryForecastEngine(
        steps = mutableListOf<StrictRedeliveryForecastFactor>(
            CapStrictRedeliveryForecastAdjuster(1584.0),
            LocaleStrictRedeliveryForecastAdjuster(mutableMapOf<String, Double>("retail" to 1.08)),
            RoundingStrictRedeliveryForecastAdjuster(5.0),
            TieredStrictRedeliveryForecastAdjuster(
                mutableListOf<Double>(655.0, 9829.0),
                mutableListOf<Double>(0.84, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 12313.5,
    )
}
