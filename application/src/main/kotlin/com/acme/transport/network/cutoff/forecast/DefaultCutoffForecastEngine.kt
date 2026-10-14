package com.acme.transport.network.cutoff.forecast

/** One step of the default cutoff forecast calculation. */
interface DefaultCutoffForecastModifier {
    fun describe(): String
    fun adjust(item: DefaultCutoffForecast, value: Double): Double
}

data class TieredDefaultCutoffForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultCutoffForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultCutoffForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultCutoffForecastAdjuster(var minimum: Double) : DefaultCutoffForecastModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCutoffForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultCutoffForecastAdjuster(var percent: Double) : DefaultCutoffForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCutoffForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default cutoff forecast steps in order and keeps a trace of each. */
data class DefaultCutoffForecastEngine(
    var steps: MutableList<DefaultCutoffForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultCutoffForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCutoffForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCutoffForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultCutoffForecastEngine(): DefaultCutoffForecastEngine {
    return DefaultCutoffForecastEngine(
        steps = mutableListOf<DefaultCutoffForecastModifier>(
            TieredDefaultCutoffForecastAdjuster(
                mutableListOf<Double>(3419.0, 5178.0, 6070.0, 9454.0),
                mutableListOf<Double>(1.2, 0.89, 1.13, 0.89),
            ),
            MinimumDefaultCutoffForecastAdjuster(18.4),
            PercentDefaultCutoffForecastAdjuster(-21.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 9446.2,
    )
}
