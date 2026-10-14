package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident forecast calculation. */
interface IncidentForecastStep {
    fun describe(): String
    fun adjust(item: IncidentForecast, value: Double): Double
}

data class PercentIncidentForecastAdjuster(var percent: Double) : IncidentForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class IncidentForecastLoadKgOffsetAdjuster(var weight: Double) : IncidentForecastStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: IncidentForecast, value: Double): Double = value + item.loadKg * weight
}

data class MinimumIncidentForecastAdjuster(var minimum: Double) : IncidentForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingIncidentForecastAdjuster(var step: Double) : IncidentForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentForecast, value: Double): Double = roundTo(value, step)
}

data class DepotIncidentForecastAdjuster(var factors: MutableMap<String, Double>) : IncidentForecastStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: IncidentForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the incident forecast steps in order and keeps a trace of each. */
data class IncidentForecastEngine(
    var steps: MutableList<IncidentForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IncidentForecast): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIncidentForecastEngine(): IncidentForecastEngine {
    return IncidentForecastEngine(
        steps = mutableListOf<IncidentForecastStep>(
            PercentIncidentForecastAdjuster(-11.8),
            IncidentForecastLoadKgOffsetAdjuster(0.49),
            MinimumIncidentForecastAdjuster(14.4),
            RoundingIncidentForecastAdjuster(0.5),
            DepotIncidentForecastAdjuster(mutableMapOf<String, Double>("west" to 1.26, "online" to 1.13)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 12081.3,
    )
}
