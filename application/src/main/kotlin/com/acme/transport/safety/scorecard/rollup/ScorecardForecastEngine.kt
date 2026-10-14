package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.flags.Registry

/** One step of the scorecard forecast calculation. */
interface ScorecardForecastAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardForecast, value: Double): Double
}

data class MinimumScorecardForecastAdjuster(var minimum: Double) : ScorecardForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScorecardForecastAdjuster(var cap: Double) : ScorecardForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardForecast, value: Double): Double = minOf(value, cap)
}

data class FlaggedScorecardForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScorecardForecastDiscountOffsetAdjuster(var weight: Double) : ScorecardForecastAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: ScorecardForecast, value: Double): Double = value + item.discount * weight
}

data class PercentScorecardForecastAdjuster(var percent: Double) : ScorecardForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scorecard forecast steps in order and keeps a trace of each. */
data class ScorecardForecastEngine(
    var steps: MutableList<ScorecardForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardForecast): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardForecastEngine(flags: Registry): ScorecardForecastEngine {
    return ScorecardForecastEngine(
        steps = mutableListOf<ScorecardForecastAdjuster>(
            MinimumScorecardForecastAdjuster(45.0),
            CapScorecardForecastAdjuster(21456.0),
            FlaggedScorecardForecastAdjuster(flags, "scorecard-forecast-regional", -6.8),
            ScorecardForecastDiscountOffsetAdjuster(0.42),
            PercentScorecardForecastAdjuster(-19.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 6754.7,
    )
}
