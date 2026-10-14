package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict violation forecast calculation. */
interface StrictViolationForecastAdjuster {
    fun describe(): String
    fun adjust(item: StrictViolationForecast, value: Double): Double
}

data class RoundingStrictViolationForecastAdjuster(var step: Double) : StrictViolationForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictViolationForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictViolationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictViolationForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictViolationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictViolationForecastAdjuster(var percent: Double) : StrictViolationForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictViolationForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict violation forecast steps in order and keeps a trace of each. */
data class StrictViolationForecastEngine(
    var steps: MutableList<StrictViolationForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictViolationForecast): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictViolationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictViolationForecastEngine(flags: Registry): StrictViolationForecastEngine {
    return StrictViolationForecastEngine(
        steps = mutableListOf<StrictViolationForecastAdjuster>(
            RoundingStrictViolationForecastAdjuster(1.0),
            FlaggedStrictViolationForecastAdjuster(flags, "strict-violation-forecast-fallback", 3.1),
            PercentStrictViolationForecastAdjuster(1.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 30958.3,
    )
}
