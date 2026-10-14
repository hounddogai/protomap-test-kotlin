package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled incident forecast calculation. */
interface ScheduledIncidentForecastStep {
    fun describe(): String
    fun adjust(item: ScheduledIncidentForecast, value: Double): Double
}

data class ScheduledIncidentForecastRetriesOffsetAdjuster(var weight: Double) : ScheduledIncidentForecastStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: ScheduledIncidentForecast, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class FlaggedScheduledIncidentForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledIncidentForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledIncidentForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledIncidentForecastAdjuster(var minimum: Double) : ScheduledIncidentForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledIncidentForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled incident forecast steps in order and keeps a trace of each. */
data class ScheduledIncidentForecastEngine(
    var steps: MutableList<ScheduledIncidentForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledIncidentForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledIncidentForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledIncidentForecastEngine(flags: Registry): ScheduledIncidentForecastEngine {
    return ScheduledIncidentForecastEngine(
        steps = mutableListOf<ScheduledIncidentForecastStep>(
            ScheduledIncidentForecastRetriesOffsetAdjuster(1.43),
            FlaggedScheduledIncidentForecastAdjuster(flags, "scheduled-incident-forecast-manual", -5.7),
            MinimumScheduledIncidentForecastAdjuster(11.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 54779.9,
    )
}
