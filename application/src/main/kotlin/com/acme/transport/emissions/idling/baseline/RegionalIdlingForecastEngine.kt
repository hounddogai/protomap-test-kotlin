package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.flags.Registry

/** One step of the regional idling forecast calculation. */
interface RegionalIdlingForecastStep {
    fun describe(): String
    fun adjust(item: RegionalIdlingForecast, value: Double): Double
}

data class FlaggedRegionalIdlingForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalIdlingForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalIdlingForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalIdlingForecastAdjuster(var percent: Double) : RegionalIdlingForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalIdlingForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleRegionalIdlingForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalIdlingForecastStep {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalIdlingForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional idling forecast steps in order and keeps a trace of each. */
data class RegionalIdlingForecastEngine(
    var steps: MutableList<RegionalIdlingForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalIdlingForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalIdlingForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalIdlingForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalIdlingForecastEngine(flags: Registry): RegionalIdlingForecastEngine {
    return RegionalIdlingForecastEngine(
        steps = mutableListOf<RegionalIdlingForecastStep>(
            FlaggedRegionalIdlingForecastAdjuster(flags, "regional-idling-forecast-default", 1.1),
            PercentRegionalIdlingForecastAdjuster(11.9),
            TitleRegionalIdlingForecastAdjuster(
                mutableMapOf<String, Double>("online" to 0.91, "retail" to 1.07, "south" to 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 83769.9,
    )
}
